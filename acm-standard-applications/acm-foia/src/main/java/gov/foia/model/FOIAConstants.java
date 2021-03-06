/**
 *
 */
package gov.foia.model;

/*-
 * #%L
 * ACM Standard Application: Freedom of Information Act
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

/**
 * @author Lazo Lazarev a.k.a. Lazarius Borg @ zerogravity Aug 17, 2016
 */
public interface FOIAConstants
{

    public String NEW_REQUEST_TYPE = "New Request";
    public String APPEAL_REQUEST_TYPE = "Appeal";

    public String REQ = "REQ";
    public String ACK = "ACK";
    public String RECEIVE_ACK = "RECEIVE_ACK";
    public String REQ_DELETE = "REQ_DELETE";
    public String DENIAL = "DENIAL";
    public String REQ_EXTENSION = "REQ_EXTENSION";

    public String MIME_TYPE_PDF = "application/pdf";
    public String MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public String NEW_FILE = "NEW_FILE";
    public String FILE_ID = "FILE_ID";

    public String EMAIL_HEADER_SUBJECT = "FOIA Extension Notification";
    public String EMAIL_HEADER_ATTACHMENT = "Hello,";
    public String EMAIL_BODY_ATTACHMENT = "Please find the attached document(s) for your review.";
    public String EMAIL_FOOTER_ATTACHMENT = "Powered by ArkCase, Enterprise Case Management platform, http://www.arkcase.com";

    public String EMAIL_RELEASE_SUBJECT = "Request Complete";
    public String EMAIL_RESPONSE_FOLDER_INSTALLMENT_SUBJECT = "A new installment of document(s) are now available for Request";
    public String EMAIL_RELEASE_BODY = "Your %s with number %s has been released and the document(s) are ready for download on the portal. Please go to the check status page at this <a href=\"%s\">link</a>.";
    public String PORTAL_REQUEST_STATUS_RELATIVE_URL = "/../foia/portal/requestStatus?requestNumber=%s";
    public String NEW_REQUEST_TITLE = "NEW REQUEST";

    public String EMAIL_RESPONSE_FOLDER_ZIP = "New installment of documents is now available for your FOIA Request";

    public static final String FOIA_PIPELINE_EXTENSION_PROPERTY_KEY = "foia_request_extension";
}
