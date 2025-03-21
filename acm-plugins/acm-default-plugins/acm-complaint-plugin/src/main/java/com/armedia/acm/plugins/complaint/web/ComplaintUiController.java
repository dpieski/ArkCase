package com.armedia.acm.plugins.complaint.web;

/*-
 * #%L
 * ACM Default Plugin: Complaints
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

import com.armedia.acm.form.config.FormUrl;
import com.armedia.acm.frevvo.config.FrevvoFormName;
import com.armedia.acm.pluginmanager.model.AcmPlugin;
import com.armedia.acm.plugins.complaint.model.ComplaintConstants;
import com.armedia.acm.services.authenticationtoken.service.AuthenticationTokenService;
import com.armedia.acm.services.users.dao.UserActionDao;
import com.armedia.acm.services.users.model.AcmUserAction;
import com.armedia.acm.services.users.model.AcmUserActionName;

import org.json.JSONArray;
import org.json.JSONException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

@RequestMapping("/plugin/complaint")
public class ComplaintUiController
{
    private Logger log = LogManager.getLogger(getClass());
    private AcmPlugin plugin;
    private AuthenticationTokenService authenticationTokenService;
    private FormUrl formUrl;
    private UserActionDao userActionDao;
    private Map<String, Object> formProperties;
    private Map<String, Object> notificationProperties;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView openComplaints(Authentication auth, HttpServletRequest request)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("complaint");

        initModelAndView(mv, auth);

        if (null != request && "successful".equals(request.getParameter("frevvoFormSubmit_status")))
        {
            AcmUserAction userAction = getUserActionDao().findByUserIdAndName(auth.getName(), AcmUserActionName.LAST_COMPLAINT_CREATED);

            if (null != userAction)
            {
                Long id = userAction.getObjectId();

                if (null != id)
                {
                    String page = request.getParameter("frevvoFormSubmit_page");

                    mv.addObject("frevvoFormSubmit_id", id);
                    mv.addObject("frevvoFormSubmit_page", page);
                }
            }
        }

        return mv;
    }

    @PreAuthorize("hasPermission(#complaintId, 'COMPLAINT', 'read|write|group-read|group-write')")
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.GET)
    public ModelAndView viewFile(Authentication auth, @PathVariable(value = "complaintId") Long complaintId)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("complaint");
        mv.addObject("objId", complaintId);

        initModelAndView(mv, auth);
        return mv;
    }

    private void addJsonArrayProp(ModelAndView mv, Map<String, Object> props, String propName, String attrName)
    {
        if (null != props)
        {
            try
            {
                Object prop = props.get(propName);
                if (null != prop)
                {
                    JSONArray ar = new JSONArray(prop.toString());
                    mv.addObject(attrName, ar);
                }

            }
            catch (JSONException e)
            {
                log.error(e.getMessage());
            }
        }
    }

    private ModelAndView initModelAndView(ModelAndView mv, Authentication auth)
    {
        Map<String, Object> props = getPlugin().getPluginProperties();
        addJsonArrayProp(mv, props, "search.tree.filter", "treeFilter");
        addJsonArrayProp(mv, props, "search.tree.sort", "treeSort");
        addJsonArrayProp(mv, props, "fileTypes", "fileTypes");
        mv.addObject("arkcaseUrl", getNotificationProperties().get("arkcase.url"));
        mv.addObject("arkcasePort", getNotificationProperties().get("arkcase.port"));
        mv.addObject("allowMailFilesAsAttachments", getNotificationProperties().get("notification.allowMailFilesAsAttachments"));
        mv.addObject("allowMailFilesToExternalAddresses",
                getNotificationProperties().get("notification.allowMailFilesToExternalAddresses"));
        String token = this.authenticationTokenService.getTokenForAuthentication(auth);
        mv.addObject("token", token);
        log.debug("Security token: " + token);

        // Frevvo form URLs
        mv.addObject("newComplaintFormUrl", getComplaintUrl());
        mv.addObject("closeComplaintFormUrl", getCloseComplaintUrl());
        mv.addObject("formDocuments", getFormProperties().get("form.documents"));
        return mv;
    }

    @RequestMapping(value = "/wizard", method = RequestMethod.GET)
    public ModelAndView openComplaintWizard()
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("complaintWizard");

        // Frevvo form URLs
        mv.addObject("newComplaintFormUrl", getComplaintUrl());

        return mv;

    }

    /**
     * Create complaint URL
     *
     * @return
     */
    private String getComplaintUrl()
    {
        // Default one
        String complaintFormName = FrevvoFormName.COMPLAINT.toLowerCase();
        String activeFormName = getActiveFormName(ComplaintConstants.ACTIVE_COMPLAINT_FORM_KEY);
        if (activeFormName != null)
        {
            complaintFormName = activeFormName;
        }

        return formUrl.getNewFormUrl(complaintFormName, false);
    }

    /**
     * Create complaint URL
     *
     * @return
     */
    private String getCloseComplaintUrl()
    {
        // Default one
        String closeComplaintFormName = FrevvoFormName.CLOSE_COMPLAINT.toLowerCase();
        String activeFormName = getActiveFormName(ComplaintConstants.ACTIVE_CLOSE_COMPLAINT_FORM_KEY);
        if (activeFormName != null)
        {
            closeComplaintFormName = activeFormName;
        }

        return formUrl.getNewFormUrl(closeComplaintFormName, false);
    }

    private String getActiveFormName(String key)
    {
        if (getFormProperties() != null)
        {
            if (getFormProperties().containsKey(key))
            {
                String activeFormName = (String) getFormProperties().get(key);

                if (activeFormName != null && !"".equals(activeFormName))
                {
                    return activeFormName;
                }
            }
        }

        return null;
    }

    public AuthenticationTokenService getAuthenticationTokenService()
    {
        return authenticationTokenService;
    }

    public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService)
    {
        this.authenticationTokenService = authenticationTokenService;
    }

    public FormUrl getFormUrl()
    {
        return formUrl;
    }

    public void setFormUrl(FormUrl formUrl)
    {
        this.formUrl = formUrl;
    }

    public UserActionDao getUserActionDao()
    {
        return userActionDao;
    }

    public void setUserActionDao(UserActionDao userActionDao)
    {
        this.userActionDao = userActionDao;
    }

    public Map<String, Object> getFormProperties()
    {
        return formProperties;
    }

    public void setFormProperties(Map<String, Object> formProperties)
    {
        this.formProperties = formProperties;
    }

    public AcmPlugin getPlugin()
    {
        return plugin;
    }

    public void setPlugin(AcmPlugin plugin)
    {
        this.plugin = plugin;
    }

    public Map<String, Object> getNotificationProperties()
    {
        return notificationProperties;
    }

    public void setNotificationProperties(Map<String, Object> notificationProperties)
    {
        this.notificationProperties = notificationProperties;
    }
}
