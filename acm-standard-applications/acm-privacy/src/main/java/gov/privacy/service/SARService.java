package gov.privacy.service;

/*-
 * #%L
 * ACM Privacy: Subject Access Request
 * %%
 * Copyright (C) 2014 - 2020 ArkCase LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.auth.AuthenticationUtils;
import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmListObjectsFailedException;
import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.core.exceptions.AcmUpdateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.plugins.addressable.model.ContactMethod;
import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.casefile.dao.CaseFileDao;
import com.armedia.acm.plugins.casefile.model.CaseFile;
import com.armedia.acm.plugins.casefile.service.SaveCaseService;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;
import com.armedia.acm.plugins.ecm.model.AcmCmisObject;
import com.armedia.acm.plugins.ecm.model.AcmCmisObjectList;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.plugins.ecm.model.AcmFolder;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.plugins.objectassociation.model.ObjectAssociation;
import com.armedia.acm.plugins.person.model.Person;
import com.armedia.acm.plugins.person.model.PersonAssociation;
import com.armedia.acm.services.notification.service.NotificationSender;
import com.armedia.acm.services.pipeline.exception.PipelineProcessException;
import com.armedia.acm.services.search.service.ExecuteSolrQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.privacy.broker.SARFileBrokerClient;
import gov.privacy.dao.SARDao;
import gov.privacy.model.SARConfig;
import gov.privacy.model.SubjectAccessRequest;

/**
 * @author sasko.tanaskoski
 */
public class SARService
{

    private final Logger log = LogManager.getLogger(getClass());
    private SaveCaseService saveCaseService;
    private ResponseFolderCompressorService responseFolderCompressorService;
    private SARFileBrokerClient SARFileBrokerClient;
    private SARDao SARDao;
    private CaseFileDao caseFileDao;
    private EcmFileDao ecmFileDao;
    private EcmFileService ecmFileService;
    private NotificationSender notificationSender;
    private String originalRequestFolderNameFormat;
    private QueuesTimeToCompleteService queuesTimeToCompleteService;
    private SARConfigurationService SARConfigurationService;
    private ExecuteSolrQuery executeSolrQuery;
    private SARConfig SARConfig;

    @Transactional
    public CaseFile saveRequest(CaseFile in, Map<String, List<MultipartFile>> filesMap, Authentication auth, String ipAddress)
            throws AcmCreateObjectFailedException
    {
        log.trace("Saving SubjectAccessRequest with Request Number: [{}], Request title: [{}], Request ID: [{}]", in.getCaseNumber(), in.getTitle(),
                in.getId());

        // explicitly set modifier and modified to trigger transformer to reindex data
        // fixes problem when some child objects are changed (e.g participants) and solr document is not updated
        in.setModifier(AuthenticationUtils.getUsername());
        in.setModified(new Date());
        try
        {
            CaseFile saved = null;

            if (in != null && in instanceof SubjectAccessRequest)
            {
                SubjectAccessRequest subjectAccessRequest = (SubjectAccessRequest) in;

                if (subjectAccessRequest.getReceivedDate() == null)
                {
                    subjectAccessRequest.setReceivedDate(LocalDateTime.now());
                }

                setDefaultPhoneAndEmailIfAny(in);
                setDefaultAddressType(in);

                if (subjectAccessRequest.getId() == null && subjectAccessRequest.getSubject().getPerson().getDefaultEmail()
                        .equals(subjectAccessRequest.getOriginator().getPerson().getDefaultEmail()))
                {
                    PersonAssociation originatorAssociation = subjectAccessRequest.getOriginator();
                    PersonAssociation subjectAssociation = subjectAccessRequest.getSubject();

                    originatorAssociation.setPerson(subjectAccessRequest.getSubject().getPerson());
                    subjectAccessRequest.setOriginator(subjectAccessRequest.getSubject());

                    subjectAccessRequest.getPersonAssociations().clear();
                    subjectAccessRequest.getPersonAssociations().add(originatorAssociation);
                    subjectAccessRequest.getPersonAssociations().add(subjectAssociation);

                    if (filesMap != null && filesMap.containsKey("Requester Proof of Identity"))
                    {
                        filesMap.remove("Requester Proof of Identity");
                    }
                }
                saved = getSaveCaseService().saveCase(in, filesMap, auth, ipAddress);
            }
            return saved;
        }
        catch (PipelineProcessException | AcmUserActionFailedException | AcmObjectNotFoundException
                | AcmUpdateObjectFailedException | IOException e)
        {
            throw new AcmCreateObjectFailedException("SubjectAccessRequest", e.getMessage(), e);
        }
    }

    public Map<String, Long> getNextAvailableRequestsInQueue(Long queueId, String requestCreatedDate) throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date createdDate = sdf.parse(requestCreatedDate);
        List<SubjectAccessRequest> nextRequests = getSARDao().getNextAvailableRequestInQueue(queueId, createdDate);
        Map<String, Long> nextRequestAndRequestNumInfo = new HashMap<>();
        if (nextRequests == null || nextRequests.size() == 0)
        {
            nextRequestAndRequestNumInfo.put("availableRequests", 0L);
            return nextRequestAndRequestNumInfo;
        }
        Long fileId = getEcmFileService().findOldestFileByContainerAndFileType(nextRequests.get(0).getContainer().getId(), "Request Form")
                .getId();
        nextRequestAndRequestNumInfo.put("requestId", nextRequests.get(0).getId());
        nextRequestAndRequestNumInfo.put("requestFormId", fileId);
        nextRequestAndRequestNumInfo.put("availableRequests", (long) nextRequests.size());
        return nextRequestAndRequestNumInfo;
    }

    private void setDefaultPhoneAndEmailIfAny(CaseFile saved)
    {

        for (int j = 0; j < saved.getPersonAssociations().size(); j++)
        {
            Person person = saved.getPersonAssociations().get(j).getPerson();

            for (int i = 0; i < person.getContactMethods().size(); i++)
            {
                ContactMethod contact = person.getContactMethods().get(i);
                if (contact != null)
                {
                    if (contact.getType() == null)
                    {
                        if (i == 0)
                        {
                            contact.setType("phone");
                        }
                        else if (i == 1)
                        {
                            contact.setType("fax");
                        }
                        else
                        {
                            contact.setType("email");
                        }
                    }
                    String type = contact.getType();
                    String value = contact.getValue();
                    if (type.toLowerCase().equals("phone") && value != null && !value.isEmpty())
                    {
                        person.setDefaultPhone(contact);
                    }
                    else if (type.toLowerCase().equals("email") && value != null)
                    {
                        person.setDefaultEmail(contact);
                    }
                }
            }
        }
    }

    private void setDefaultAddressType(CaseFile saved)
    {
        for (int j = 0; j < saved.getPersonAssociations().size(); j++)
        {
            Person person = saved.getPersonAssociations().get(j).getPerson();
            if (person.getAddresses().size() != 0)
            {
                PostalAddress address = person.getAddresses().get(0);
                if (address.getType() == null)
                {
                    address.setType("Business");
                }
            }
        }

    }

    public CaseFile createReference(CaseFile in, CaseFile originalRequest)
    {
        ObjectAssociation oa = new ObjectAssociation();
        oa.setTargetId(originalRequest.getId());
        oa.setTargetName(originalRequest.getCaseNumber());
        oa.setTargetType(originalRequest.getObjectType());
        oa.setTargetTitle(originalRequest.getTitle());
        oa.setAssociationType("REFERENCE");
        oa.setStatus("ACTIVE");

        in.addChildObject(oa);

        return in;
    }

    private void copyOriginalRequestFiles(CaseFile saved, CaseFile originalRequest, Authentication auth)
            throws AcmCreateObjectFailedException, AcmUserActionFailedException, AcmListObjectsFailedException
    {

        AcmContainer container = getEcmFileService().getOrCreateContainer(originalRequest.getObjectType(), originalRequest.getId());
        AcmCmisObjectList files = getEcmFileService().allFilesForContainer(auth, container);

        AcmContainer containerCaseFile = getEcmFileService().getOrCreateContainer(saved.getObjectType(),
                saved.getId());

        String originalrequestFolderName = String.format(getOriginalRequestFolderNameFormat(),
                originalRequest.getCaseNumber());

        AcmFolder originalRootFolder = container.getFolder();
        originalRootFolder.setParentFolder(containerCaseFile.getFolder());
        originalRootFolder.setName(originalrequestFolderName);
        originalRootFolder.setStatus(EcmFileConstants.RECORD);

        setSubfoldersAsRecords(originalRootFolder);

        if (files != null && files.getChildren() != null)
        {
            for (AcmCmisObject file : files.getChildren())
            {
                EcmFile ecmFile = getEcmFileService().findById(file.getObjectId());
                ecmFile.setContainer(containerCaseFile);
                ecmFile.setStatus(EcmFileConstants.RECORD);

                getEcmFileDao().save(ecmFile);
            }
        }

    }

    private void setSubfoldersAsRecords(AcmFolder folder)
    {
        folder.getChildrenFolders().forEach(subfolder -> {
            subfolder.setStatus(EcmFileConstants.RECORD);
            setSubfoldersAsRecords(subfolder);
        });
    }

    /**
     * @return the saveCaseService
     */
    public SaveCaseService getSaveCaseService()
    {
        return saveCaseService;
    }

    /**
     * @param saveCaseService
     *            the saveCaseService to set
     */
    public void setSaveCaseService(SaveCaseService saveCaseService)
    {
        this.saveCaseService = saveCaseService;
    }

    /**
     * @return the responseFolderCompressorService
     */
    public ResponseFolderCompressorService getResponseFolderCompressorService()
    {
        return responseFolderCompressorService;
    }

    /**
     * @param responseFolderCompressorService
     *            the responseFolderCompressorService to set
     */
    public void setResponseFolderCompressorService(ResponseFolderCompressorService responseFolderCompressorService)
    {
        this.responseFolderCompressorService = responseFolderCompressorService;
    }

    /**
     * @return the SARFileBrokerClient
     */
    public SARFileBrokerClient getSARFileBrokerClient()
    {
        return SARFileBrokerClient;
    }

    /**
     * @param SARFileBrokerClient
     *            the SARFileBrokerClient to set
     */
    public void setSARFileBrokerClient(SARFileBrokerClient SARFileBrokerClient)
    {
        this.SARFileBrokerClient = SARFileBrokerClient;
    }

    /**
     * @return the SARDao
     */
    public SARDao getSARDao()
    {
        return SARDao;
    }

    /**
     * @param SARDao
     *            the SARDao to set
     */
    public void setSARDao(SARDao SARDao)
    {
        this.SARDao = SARDao;
    }

    /**
     * @return the caseFileDao
     */
    public CaseFileDao getCaseFileDao()
    {
        return caseFileDao;
    }

    /**
     * @param caseFileDao
     *            the caseFileDao to set
     */
    public void setCaseFileDao(CaseFileDao caseFileDao)
    {
        this.caseFileDao = caseFileDao;
    }

    /**
     * @return the ecmFileDao
     */
    public EcmFileDao getEcmFileDao()
    {
        return ecmFileDao;
    }

    /**
     * @param ecmFileDao
     *            the ecmFileDao to set
     */
    public void setEcmFileDao(EcmFileDao ecmFileDao)
    {
        this.ecmFileDao = ecmFileDao;
    }

    /**
     * @return the ecmFileService
     */
    public EcmFileService getEcmFileService()
    {
        return ecmFileService;
    }

    /**
     * @param ecmFileService
     *            the ecmFileService to set
     */
    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }

    /**
     * @return the originalRequestFolderNameFormat
     */
    public String getOriginalRequestFolderNameFormat()
    {
        return originalRequestFolderNameFormat;
    }

    /**
     * @param originalRequestFolderNameFormat
     *            the originalRequestFolderNameFormat to set
     */
    public void setOriginalRequestFolderNameFormat(String originalRequestFolderNameFormat)
    {
        this.originalRequestFolderNameFormat = originalRequestFolderNameFormat;
    }

    /**
     * @return the notificationSender
     */
    public NotificationSender getNotificationSender()
    {
        return notificationSender;
    }

    /**
     * @param notificationSender
     *            the notificationSender to set
     */
    public void setNotificationSender(NotificationSender notificationSender)
    {
        this.notificationSender = notificationSender;
    }

    public void setQueuesTimeToCompleteService(QueuesTimeToCompleteService queuesTimeToCompleteService)
    {
        this.queuesTimeToCompleteService = queuesTimeToCompleteService;
    }

    public QueuesTimeToCompleteService getQueuesTimeToCompleteService()
    {
        return queuesTimeToCompleteService;
    }

    public SARConfigurationService getSARConfigurationService()
    {
        return SARConfigurationService;
    }

    public void setSARConfigurationService(SARConfigurationService SARConfigurationService)
    {
        this.SARConfigurationService = SARConfigurationService;
    }

    public SubjectAccessRequest getSARById(Long requestId)
    {
        return SARDao.find(requestId);
    }

    public ExecuteSolrQuery getExecuteSolrQuery()
    {
        return executeSolrQuery;
    }

    public void setExecuteSolrQuery(ExecuteSolrQuery executeSolrQuery)
    {
        this.executeSolrQuery = executeSolrQuery;
    }

    public void setSARConfig(SARConfig SARConfig)
    {
        this.SARConfig = SARConfig;
    }
}
