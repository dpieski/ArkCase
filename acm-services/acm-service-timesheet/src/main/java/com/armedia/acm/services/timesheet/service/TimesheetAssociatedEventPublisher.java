package com.armedia.acm.services.timesheet.service;

import com.armedia.acm.services.timesheet.model.AcmTimesheetAssociatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class TimesheetAssociatedEventPublisher implements ApplicationEventPublisherAware
{

    private Logger LOG = LoggerFactory.getLogger(getClass());
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(AcmTimesheetAssociatedEvent event)
    {
        LOG.debug("Publishing AcmTimesheetAssociated Event");
        getApplicationEventPublisher().publishEvent(event);
    }

    public ApplicationEventPublisher getApplicationEventPublisher()
    {
        return applicationEventPublisher;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}