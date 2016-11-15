package com.armedia.acm.web.api.service;

import java.util.Map;

/**
 * Created by dragan.simonovski on 05/11/2016.
 */
public interface LoginWarningMessageService
{
    boolean isEnabled();

    String getMessage();

    Map<String, Object> getWarning();
}