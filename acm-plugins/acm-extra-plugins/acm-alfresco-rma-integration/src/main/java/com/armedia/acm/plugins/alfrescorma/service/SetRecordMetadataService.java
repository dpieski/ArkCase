package com.armedia.acm.plugins.alfrescorma.service;

/*-
 * #%L
 * ACM Extra Plugin: Alfresco RMA Integration
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
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

import com.armedia.acm.plugins.alfrescorma.exception.AlfrescoServiceException;

import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by dmiller on 11/7/2016.
 */
public class SetRecordMetadataService extends AlfrescoService<String> implements InitializingBean
{
    private final RestTemplate restTemplate;
    private final String service = "/s/api/node";
    private transient final Logger LOG = LogManager.getLogger(getClass());
    private BigDecimal alfrescoRmaModuleVersion;
    private String publicationDateField;
    private String originatorField;
    private String originatingOrganizationField;
    private String dateReceivedField;

    public SetRecordMetadataService()
    {
        restTemplate = new RestTemplate();
    }

    /**
     * The context must have:
     * <ul>
     * <li>Key ecmFileId: String, CMIS Version Series ID (NOT the id, the versionSeriesId) of the document which will
     * have its metadata
     * set</li>
     * <li>Key publicationDate: Date</li>
     * <li>Key originator: Date</li>
     * <li>Key originatingOrganization: Date</li>
     * <li>Key dateReceived: Date</li>
     * </ul>
     */
    @Override
    public String doService(Map<String, Object> context) throws AlfrescoServiceException
    {
        validateContext(context);

        String ecmFileId = (String) context.get("ecmFileId");

        // have to split the file id into namespace and id parts
        int namespaceSeparatorIndex = ecmFileId.indexOf("://");
        String namespace = ecmFileId.substring(0, namespaceSeparatorIndex);
        String id = ecmFileId.substring(namespaceSeparatorIndex + 3);

        String fullService = service + "/" + namespace + "/" + id + "/formprocessor";

        String url = baseUrl() + fullService;

        LOG.debug("Full URL: {}", url);

        JSONObject setRecordMetadataPayload = buildPayload(context);
        LOG.debug("Payload: {}", setRecordMetadataPayload);

        HttpEntity<String> entity = buildRestEntity(setRecordMetadataPayload);

        try
        {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            LOG.debug("set metadata response: {}", response.getBody());

            if (HttpStatus.OK.equals(response.getStatusCode()))
            {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                LOG.debug("response: {}", jsonResponse);

                String persistedObject = jsonResponse.getString("persistedObject");
                return persistedObject;
            }
            else
            {
                throw new AlfrescoServiceException("Could not set metadata: " + response.getStatusCode());
            }
        }
        catch (RestClientException e)
        {
            LOG.error("Exception setting metadata: {} {}", e.getMessage(), e);
            throw new AlfrescoServiceException(e.getMessage(), e);
        }
    }

    private JSONObject buildPayload(Map<String, Object> context)
    {
        // Note, these date formats are exactly as the old declareAlfrescoRmaRecordsFlow.xml used to format them.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'11:00:00.000-05:00");
        Date publicationDate = (Date) context.get("publicationDate");
        String publicationDateJson = df.format(publicationDate);

        String originator = (String) context.get("originator");
        String originatingOrganization = (String) context.get("originatingOrganization");
        Date dateReceived = (Date) context.get("dateReceived");
        String dateReceivedJson = df.format(dateReceived);

        JSONObject setRecordMetadataPayload = new JSONObject();
        setRecordMetadataPayload.put(publicationDateField, publicationDateJson);
        setRecordMetadataPayload.put(originatorField, originator);
        setRecordMetadataPayload.put(originatingOrganizationField, originatingOrganization);
        setRecordMetadataPayload.put(dateReceivedField, dateReceivedJson);

        return setRecordMetadataPayload;
    }

    private void validateContext(Map<String, Object> context) throws IllegalArgumentException
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context must not be null");
        }

        if (context.get("ecmFileId") == null || !(context.get("ecmFileId") instanceof String))
        {
            throw new IllegalArgumentException("Context must include an ecmFileId of type String");
        }

        if (context.get("publicationDate") == null || !(context.get("publicationDate") instanceof Date))
        {
            throw new IllegalArgumentException("Context must include a publicationDate of type Date");
        }

        if (context.get("originator") == null || !(context.get("originator") instanceof String))
        {
            throw new IllegalArgumentException("Context must include an originator of type String");
        }

        if (context.get("originatingOrganization") == null || !(context.get("originatingOrganization") instanceof String))
        {
            throw new IllegalArgumentException("Context must include an originatingOrganization of type String");
        }

        if (context.get("dateReceived") == null || !(context.get("dateReceived") instanceof Date))
        {
            throw new IllegalArgumentException("Context must include a dateReceived of type Date");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        BigDecimal switchToDodPropNameVersion = BigDecimal.valueOf(2.3);

        boolean useDodPropertyNames = switchToDodPropNameVersion.compareTo(getAlfrescoRmaModuleVersion()) < 1;

        if (useDodPropertyNames)
        {
            publicationDateField = "prop_dod_publicationDate";
            originatorField = "prop_dod_originator";
            originatingOrganizationField = "prop_dod_originatingOrganization";
            dateReceivedField = "prop_dod_dateReceived";
        }
        else
        {
            publicationDateField = "prop_rma_publicationDate";
            originatorField = "prop_rma_originator";
            originatingOrganizationField = "prop_rma_originatingOrganization";
            dateReceivedField = "prop_rma_dateReceived";
        }

    }

    public BigDecimal getAlfrescoRmaModuleVersion()
    {
        return alfrescoRmaModuleVersion;
    }

    public void setAlfrescoRmaModuleVersion(BigDecimal alfrescoRmaModuleVersion)
    {
        this.alfrescoRmaModuleVersion = alfrescoRmaModuleVersion;
    }

}
