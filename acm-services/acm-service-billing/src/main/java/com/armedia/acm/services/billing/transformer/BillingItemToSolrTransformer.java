package com.armedia.acm.services.billing.transformer;

/*-
 * #%L
 * ACM Service: Billing
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

import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.CREATOR_FULL_NAME_LCS;
import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.DESCRIPTION_PARSEABLE;
import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.MODIFIER_FULL_NAME_LCS;
import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.PARENT_OBJECT_ID_I;
import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.PARENT_REF_S;
import static com.armedia.acm.services.search.model.solr.SolrAdditionalPropertiesConstants.PARENT_TYPE_S;

import com.armedia.acm.services.billing.dao.BillingItemDao;
import com.armedia.acm.services.billing.model.BillingConstants;
import com.armedia.acm.services.billing.model.BillingItem;
import com.armedia.acm.services.search.model.solr.SolrAdvancedSearchDocument;
import com.armedia.acm.services.search.service.AcmObjectToSolrDocTransformer;
import com.armedia.acm.services.users.dao.UserDao;
import com.armedia.acm.services.users.model.AcmUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author sasko.tanaskoski
 *
 */
public class BillingItemToSolrTransformer implements AcmObjectToSolrDocTransformer<BillingItem>
{
    private final Logger LOG = LogManager.getLogger(getClass());

    private UserDao userDao;
    private BillingItemDao billingItemDao;

    @Override
    public List<BillingItem> getObjectsModifiedSince(Date lastModified, int start, int pageSize)
    {
        return getBillingItemDao().findModifiedSince(lastModified, start, pageSize);
    }

    @Override
    public SolrAdvancedSearchDocument toSolrAdvancedSearch(BillingItem in)
    {
        SolrAdvancedSearchDocument solrDoc = new SolrAdvancedSearchDocument();
        LOG.debug("Creating Solr advanced search document for BILLING_ITEM.");

        String name = String.format("%s_%d", BillingConstants.OBJECT_TYPE_ITEM, in.getId());
        mapRequiredProperties(solrDoc, in.getId(), in.getCreator(), in.getCreated(), in.getModifier(), in.getModified(),
                BillingConstants.OBJECT_TYPE_ITEM, name);

        mapAdditionalProperties(in, solrDoc.getAdditionalProperties());

        return solrDoc;
    }

    @Override
    public void mapAdditionalProperties(BillingItem in, Map<String, Object> additionalProperties)
    {
        additionalProperties.put(DESCRIPTION_PARSEABLE, in.getItemDescription());

        /** Additional properties for full names instead of ID's */
        AcmUser creator = getUserDao().quietFindByUserId(in.getCreator());
        if (creator != null)
        {
            additionalProperties.put(CREATOR_FULL_NAME_LCS, creator.getFirstName() + " " + creator.getLastName());
        }

        AcmUser modifier = getUserDao().quietFindByUserId(in.getModifier());
        if (modifier != null)
        {
            additionalProperties.put(MODIFIER_FULL_NAME_LCS, modifier.getFirstName() + " " + modifier.getLastName());
        }

        additionalProperties.put("item_number_i", in.getItemNumber());
        additionalProperties.put("item_description_s", in.getItemDescription());
        additionalProperties.put("item_amount_s", Double.toString(in.getItemAmount()));

        additionalProperties.put(PARENT_TYPE_S, in.getParentObjectType());
        additionalProperties.put(PARENT_OBJECT_ID_I, in.getParentObjectId());
        additionalProperties.put(PARENT_REF_S, String.format("%d-%s", in.getParentObjectId(), in.getParentObjectType()));
    }

    @Override
    public boolean isAcmObjectTypeSupported(Class acmObjectType)
    {
        return BillingItem.class.equals(acmObjectType);
    }

    public UserDao getUserDao()
    {
        return userDao;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public BillingItemDao getBillingItemDao()
    {
        return billingItemDao;
    }

    public void setBillingItemDao(BillingItemDao billingItemDao)
    {
        this.billingItemDao = billingItemDao;
    }

    @Override
    public Class<?> getAcmObjectTypeSupported()
    {
        return BillingItem.class;
    }
}
