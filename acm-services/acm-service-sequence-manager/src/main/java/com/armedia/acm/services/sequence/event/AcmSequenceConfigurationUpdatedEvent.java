/**
 * 
 */
package com.armedia.acm.services.sequence.event;

/*-
 * #%L
 * ACM Service: Sequence Manager
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
import com.armedia.acm.services.sequence.model.AcmSequenceConfiguration;
import com.armedia.acm.services.sequence.model.AcmSequenceConstants;

import java.util.Date;
import java.util.List;

/**
 * @author sasko.tanaskoski
 *
 */
public class AcmSequenceConfigurationUpdatedEvent extends AcmEvent
{

    private static final long serialVersionUID = -467443790265934307L;

    public AcmSequenceConfigurationUpdatedEvent(List<AcmSequenceConfiguration> source)
    {
        super(source);
        setEventDate(new Date());
    }

    @Override
    public List<AcmSequenceConfiguration> getSource()
    {
        return (List<AcmSequenceConfiguration>) super.getSource();
    }

    @Override
    public String getEventType()
    {
        return AcmSequenceConstants.SEQUENCE_CONFIGURATION_UPDATED_EVENT;
    }

}
