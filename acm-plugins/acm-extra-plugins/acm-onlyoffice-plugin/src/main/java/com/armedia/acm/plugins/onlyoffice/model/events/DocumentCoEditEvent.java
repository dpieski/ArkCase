package com.armedia.acm.plugins.onlyoffice.model.events;

/*-
 * #%L
 * ACM Extra Plugin: OnlyOffice Integration
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

import com.armedia.acm.core.model.AcmEvent;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;

import java.util.Date;

public abstract class DocumentCoEditEvent extends AcmEvent
{
    private static final String OBJECT_TYPE = EcmFileConstants.OBJECT_FILE_TYPE;

    public DocumentCoEditEvent(EcmFile source, String userId)
    {
        super(source);
        setObjectId(source.getId());
        setParentObjectId(source.getParentObjectId());
        setParentObjectType(source.getParentObjectType());
        setEventDate(new Date());
        setUserId(userId);
    }

    @Override
    public String getObjectType()
    {
        return OBJECT_TYPE;
    }

}
