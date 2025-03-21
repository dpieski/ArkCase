package com.armedia.acm.plugins.casefile.web;

/*-
 * #%L
 * ACM Default Plugin: Case File
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
import com.armedia.acm.plugins.casefile.model.CaseFileConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Properties;

/**
 * Created by jwu on 8/28/14.
 */
@RequestMapping("/plugin/casefile")
public class CaseFileUiController
{
    private Logger log = LogManager.getLogger(getClass());
    private Properties properties;
    private AcmPlugin plugin;
    private FormUrl formUrl;
    private Map<String, Object> formProperties;
    private Map<String, Object> notificationProperties;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView openComplaints(Authentication auth)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(getViewName());
        initModelAndView(mv);
        return mv;
    }

    @RequestMapping(value = "/{caseId}", method = RequestMethod.GET)
    public ModelAndView viewFile(Authentication auth, @PathVariable(value = "caseId") Long caseId)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(getViewName());
        initModelAndView(mv);
        mv.addObject("objId", caseId);
        return mv;
    }

    private String getViewName()
    {
        String jsp = "casefile";
        return jsp;
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

    private ModelAndView initModelAndView(ModelAndView mv)
    {
        Map<String, Object> props = plugin.getPluginProperties();
        addJsonArrayProp(mv, props, "search.tree.filter", "treeFilter");
        addJsonArrayProp(mv, props, "search.tree.sort", "treeSort");
        addJsonArrayProp(mv, props, "fileTypes", "fileTypes");
        mv.addObject("arkcaseUrl", getNotificationProperties().get("arkcase.url"));
        mv.addObject("arkcasePort", getNotificationProperties().get("arkcase.port"));

        mv.addObject("allowMailFilesAsAttachments", getNotificationProperties().get("notification.allowMailFilesAsAttachments"));
        mv.addObject("allowMailFilesToExternalAddresses",
                getNotificationProperties().get("notification.allowMailFilesToExternalAddresses"));

        mv.addObject("newCaseFormUrl", formUrl.getNewFormUrl(FrevvoFormName.CASE_FILE, false));
        mv.addObject("changeCaseStatusFormUrl", formUrl.getNewFormUrl(FrevvoFormName.CHANGE_CASE_STATUS, false));
        mv.addObject("enableFrevvoFormEngine", formUrl.enableFrevvoFormEngine(FrevvoFormName.ROI));
        mv.addObject("editCaseFileFormUrl", getCaseFileUrl());
        mv.addObject("reinvestigateCaseFileFormUrl", getCaseFileUrl());
        mv.addObject("formDocuments", getFormProperties().get("form.documents"));
        mv.addObject("caseFileTreeRootNameExpression", props.get("casefile.tree.root.name.expression"));
        return mv;
    }

    @RequestMapping(value = "/wizard", method = RequestMethod.GET)
    public ModelAndView openCaseFileWizard()
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("casefileWizard");

        // Frevvo form URLs
        mv.addObject("newCaseFileFormUrl", getCaseFileUrl());

        return mv;
    }

    private String getCaseFileUrl()
    {
        // Default one
        String caseFileFormName = CaseFileConstants.OBJECT_TYPE.toLowerCase();
        if (getFormProperties() != null)
        {
            if (getFormProperties().containsKey(CaseFileConstants.ACTIVE_CASE_FORM_KEY))
            {
                String activeFormName = (String) getFormProperties().get(CaseFileConstants.ACTIVE_CASE_FORM_KEY);

                if (activeFormName != null && !"".equals(activeFormName))
                {
                    caseFileFormName = activeFormName;
                }
            }
        }

        return formUrl.getNewFormUrl(caseFileFormName, false);
    }

    public FormUrl getFormUrl()
    {
        return formUrl;
    }

    public void setFormUrl(FormUrl formUrl)
    {
        this.formUrl = formUrl;
    }

    public AcmPlugin getPlugin()
    {
        return plugin;
    }

    public void setPlugin(AcmPlugin plugin)
    {
        this.plugin = plugin;
    }

    public Map<String, Object> getFormProperties()
    {
        return formProperties;
    }

    public void setFormProperties(Map<String, Object> formProperties)
    {
        this.formProperties = formProperties;
    }

    public Map<String, Object> getNotificationProperties()
    {
        return notificationProperties;
    }

    public void setNotificationProperties(Map<String, Object> notificationProperties)
    {
        this.notificationProperties = notificationProperties;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
}
