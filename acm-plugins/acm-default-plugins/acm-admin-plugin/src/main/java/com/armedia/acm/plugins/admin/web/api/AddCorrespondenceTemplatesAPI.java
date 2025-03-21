package com.armedia.acm.plugins.admin.web.api;

/*-
 * #%L
 * ACM Default Plugin: admin
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

import com.armedia.acm.plugins.admin.model.TemplateUpload;

import com.armedia.acm.services.notification.model.NotificationConstants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by manoj.dhungana on 12/4/2014.
 */
@Controller
@RequestMapping({ "/api/v1/plugin/admin", "/api/latest/plugin/admin" })
public class AddCorrespondenceTemplatesAPI
{
    private Logger log = LogManager.getLogger(getClass());

    @RequestMapping(value = "/template", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    @ResponseBody
    public List<Object> templates(
            // @RequestParam("files[]") MultipartFile file,
            HttpServletRequest request, Authentication authentication) throws Exception
    {
        List<Object> templateUploadList = new ArrayList<>();
        String userHome = System.getProperty("user.home");
        String pathName = userHome + "/.arkcase/acm/correspondenceTemplates";
        try
        {
            // save files to disk
            // for multiple files
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> attachments = multipartHttpServletRequest.getMultiFileMap();
            if (attachments != null)
            {
                for (Map.Entry<String, List<MultipartFile>> entry : attachments.entrySet())
                {

                    final List<MultipartFile> attachmentsList = entry.getValue();

                    if (attachmentsList != null && !attachmentsList.isEmpty())
                    {

                        for (final MultipartFile attachment : attachmentsList)
                        {
                            log.info("Adding new template : {}", attachment.getOriginalFilename());
                            saveMultipartToDisk(attachment, pathName);
                        }
                    }
                }
            }
            retrieveTemplateDetails(authentication, pathName, templateUploadList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return templateUploadList;
    }

    @RequestMapping(value = "/template/timestamp/{templateType}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    @ResponseBody
    public List<Object> templateTimestamp(
            // @RequestParam("files[]") MultipartFile file,
            @PathVariable(value = "templateType") String templateType, HttpServletRequest request, Authentication authentication) throws Exception
    {

        List<Object> templateUploadList = new ArrayList<>();
        String userHome = System.getProperty("user.home");
        String pathName = templateType.equals(NotificationConstants.EMAIL_NOTIFICATION_TEMPLATE_TYPE) ? userHome + "/.arkcase/acm/templates" : userHome + "/.arkcase/acm/correspondenceTemplates";
        try
        {
            // save files to disk
            // for multiple files
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            MultiValueMap<String, MultipartFile> attachments = multipartHttpServletRequest.getMultiFileMap();
            if (attachments != null)
            {
                for (Map.Entry<String, List<MultipartFile>> entry : attachments.entrySet())
                {

                    final List<MultipartFile> attachmentsList = entry.getValue();

                    if (attachmentsList != null && !attachmentsList.isEmpty())
                    {
                        for (final MultipartFile attachment : attachmentsList)
                        {
                            log.info("Adding new template : {}", attachment.getOriginalFilename());
                            String fileName = templateType.equals(NotificationConstants.EMAIL_NOTIFICATION_TEMPLATE_TYPE) ? attachment.getOriginalFilename() : createTimestampFileName(attachment.getOriginalFilename());
                            saveMultipartToDisk(attachment, pathName, fileName);
                            retrieveTemplateDetails(authentication, pathName, fileName, templateUploadList);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return templateUploadList;
    }

    public void saveMultipartToDisk(MultipartFile file, String pathName) throws Exception
    {
        File dir = new File(pathName);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        File multipartFile = new File(pathName + "/" + file.getOriginalFilename());
        file.transferTo(multipartFile);
    }

    public void saveMultipartToDisk(MultipartFile file, String pathName, String fileName) throws Exception
    {
        File dir = new File(pathName);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        File multipartFile = new File(pathName + "/" + fileName);
        file.transferTo(multipartFile);
    }

    public void retrieveTemplateDetails(Authentication authentication, String pathName, List<Object> uploadList) throws Exception
    {

        File templateFolder = new File(pathName);
        File[] templates = templateFolder.listFiles();
        if (templates != null)
        {
            for (File template : templates)
            {
                // access creation and last modified date via file attributes
                TemplateUpload templateUpload = new TemplateUpload();
                Path path = Paths.get(pathName + "/" + template.getName());
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                FileTime creationTime = attributes.creationTime();
                FileTime modifiedTime = attributes.lastModifiedTime();

                // details
                templateUpload.setPath(template.getAbsolutePath());
                templateUpload.setModified(modifiedTime.toString());
                templateUpload.setName(template.getName());
                templateUpload.setCreator(authentication.getName());
                templateUpload.setCreated(creationTime.toString());
                uploadList.add(templateUpload);
            }
        }
    }

    public void retrieveTemplateDetails(Authentication authentication, String pathName, String fileName, List<Object> uploadList)
            throws Exception
    {

        File template = new File(pathName, fileName);

        // access creation and last modified date via file attributes
        TemplateUpload templateUpload = new TemplateUpload();
        Path path = Paths.get(pathName + "/" + template.getName());
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime creationTime = attributes.creationTime();
        FileTime modifiedTime = attributes.lastModifiedTime();

        // details
        templateUpload.setPath(template.getAbsolutePath());
        templateUpload.setModified(modifiedTime.toString());
        templateUpload.setName(template.getName());
        templateUpload.setCreator(authentication.getName());
        templateUpload.setCreated(creationTime.toString());
        uploadList.add(templateUpload);
    }

    public String createTimestampFileName(String fileName)
    {
        ZonedDateTime date = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestampName = formatter.format(date);
        return timestampName + "_" + fileName;
    }

}
