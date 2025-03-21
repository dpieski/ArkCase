package gov.foia.service;

/*-
 * #%L
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

import com.armedia.acm.plugins.casefile.model.CaseFile;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import gov.foia.model.DispositionReason;
import gov.foia.model.event.FOIARequestDispositionReasonModifiedEvent;

/**
 * Created by ivana.shekerova
 */
public class FOIARequestEventUtility implements ApplicationEventPublisherAware
{
    private ApplicationEventPublisher applicationEventPublisher;

    public void raiseDispositionReasonModifiedInCaseFile(DispositionReason dispositionReason, CaseFile source, String ipAddress,
            String eventDescription)
    {
        FOIARequestDispositionReasonModifiedEvent event = new FOIARequestDispositionReasonModifiedEvent(dispositionReason);
        event.setSucceeded(true);
        event.setIpAddress(ipAddress);
        event.setParentObjectId(source.getId());
        event.setParentObjectType(source.getObjectType());
        event.setParentObjectName(source.getCaseNumber());
        event.setEventDescription(eventDescription);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
