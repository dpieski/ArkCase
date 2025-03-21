package com.armedia.acm.plugins.consultation.model;

/*-
 * #%L
 * ACM Default Plugin: Consultation
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

/**
 * Created by Vladimir Cherepnalkovski <vladimir.cherepnalkovski@armedia.com> on May, 2020
 */
public interface ConsultationConstants
{
    String OBJECT_TYPE = "CONSULTATION";

    String ACTIVE_CONSULTATION_FORM_KEY = "active.consultation.form";

    String EVENT_TYPE_CREATED = "com.armedia.acm.consultation.created";

    String EVENT_TYPE_UPDATED = "com.armedia.acm.consultation.updated";

    String EVENT_TYPE_VIEWED = "com.armedia.acm.consultation.viewed";

    String OWNING_GROUP = "owning group";

    String ASSIGNEE = "assignee";

    String PARENT_OBJECT_TYPE = "PARENT_OBJECT_TYPE";

    String PARENT_OBJECT_ID = "PARENT_OBJECT_ID";

    String MIME_TYPE_PDF = "application/pdf";
    String NEW_FILE = "NEW_FILE";
    String FILE_ID = "FILE_ID";
    String FILE_VERSION = "FILE_VERSION";

    String CONSULTATION_STYLESHEET = "consultation-document.xsl";
    String CONSULTATION_DOCUMENT = "CONSULTATION";
    String CONSULTATION_NAME_FORMAT = "Consultation.pdf";
}
