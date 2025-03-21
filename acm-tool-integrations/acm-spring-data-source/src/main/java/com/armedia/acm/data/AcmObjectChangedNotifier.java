package com.armedia.acm.data;

/*-
 * #%L
 * Tool Integrations: Spring Data Source
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

import static com.armedia.acm.data.AcmObjectEventConstants.ACTION_DELETE;
import static com.armedia.acm.data.AcmObjectEventConstants.ACTION_INSERT;
import static com.armedia.acm.data.AcmObjectEventConstants.ACTION_UPDATE;

import com.armedia.acm.core.AcmObject;
import com.armedia.acm.core.AcmObjectNumber;
import com.armedia.acm.core.AcmParentObjectInfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Date;

/**
 * Created by nebojsha on 06.05.2016.
 */
public class AcmObjectChangedNotifier implements ApplicationListener<AcmDatabaseChangesEvent>
{
    private transient Logger log = LogManager.getLogger(getClass());
    private MessageChannel objectEventChannel;

    @Override
    public void onApplicationEvent(AcmDatabaseChangesEvent acmDatabaseChangesEvent)
    {
        AcmObjectChangelist changes = acmDatabaseChangesEvent.getObjectChangelist();

        changes.getAddedObjects().stream().forEach(o -> notifyChange(AcmObjectEventConstants.ACTION_INSERT, o));
        changes.getDeletedObjects().stream().forEach(o -> notifyChange(AcmObjectEventConstants.ACTION_DELETE, o));
        changes.getUpdatedObjects().stream().forEach(o -> notifyChange(AcmObjectEventConstants.ACTION_UPDATE, o));
    }

    public void notifyChange(String action, Object object)
    {
        if (!(object instanceof AcmObject))
        {
            // not an instance of AcmObject, nothing to do
            return;
        }

        AcmObjectEvent objectChangedEvent = new AcmObjectEvent(action);
        updateAcmObjectInfo(objectChangedEvent, object);
        updateAcmEntityInfo(objectChangedEvent, object);
        updateAcmParentObjectInfo(objectChangedEvent, object);

        createAndSendMessage(objectChangedEvent);
    }

    private void createAndSendMessage(AcmObjectEvent acmObject)
    {
        Message<AcmObjectEvent> insertMessage = MessageBuilder.withPayload(acmObject).build();
        objectEventChannel.send(insertMessage);
    }

    public void setObjectEventChannel(MessageChannel ftpChannel)
    {
        this.objectEventChannel = ftpChannel;
    }

    /**
     * update objectChanged with acm object info
     *
     * @param objectChangedEvent
     *            instance of objectChangedEvent
     * @param object
     *            Object
     */
    private void updateAcmObjectInfo(AcmObjectEvent objectChangedEvent, Object object)
    {

        AcmObject acmObject = (AcmObject) object;
        objectChangedEvent.setObjectId(acmObject.getId());
        objectChangedEvent.setObjectType(acmObject.getObjectType());
        objectChangedEvent.setClassName(object.getClass().getName());
        if (acmObject instanceof AcmObjectNumber)
        {
            objectChangedEvent.setObjectNumber(((AcmObjectNumber) acmObject).getAcmObjectNumber());
        }
    }

    /**
     * update objectChanged with acm entity info
     *
     * @param objectChangedEvent
     *            instance of objectChangedEvent
     * @param object
     *            Object
     */
    private void updateAcmEntityInfo(AcmObjectEvent objectChangedEvent, Object object)
    {
        if (!(object instanceof AcmEntity))
        {
            // not an instance of AcmEntity, nothing to do
            return;
        }
        AcmEntity acmEntity = (AcmEntity) object;
        Date date = null;
        String userId = null;

        switch (objectChangedEvent.getAction())
        {
        case ACTION_UPDATE:
            date = acmEntity.getModified();
            userId = acmEntity.getModifier();
            break;
        case ACTION_INSERT:
            date = acmEntity.getCreated();
            userId = acmEntity.getCreator();
            break;
        case ACTION_DELETE:
            date = acmEntity.getModified();
            userId = acmEntity.getModifier();
            break;
        default:
            log.warn("ACTION must be provided before AcmEntity info is chosen.");
        }

        objectChangedEvent.setDate(date);
        objectChangedEvent.setUser(userId);
    }

    /**
     * update objectChanged with parent info
     *
     * @param objectChangedEvent
     *            instance of objectChangedEvent
     * @param object
     *            Object
     */
    private void updateAcmParentObjectInfo(AcmObjectEvent objectChangedEvent, Object object)
    {
        if (!(object instanceof AcmParentObjectInfo))
        {
            // not an instance of AcmParentObjectInfo, nothing to do
            return;
        }
        AcmParentObjectInfo parentObjectInfo = (AcmParentObjectInfo) object;

        objectChangedEvent.setParentObjectId(parentObjectInfo.getParentObjectId());
        objectChangedEvent.setParentObjectType(parentObjectInfo.getParentObjectType());
    }

}
