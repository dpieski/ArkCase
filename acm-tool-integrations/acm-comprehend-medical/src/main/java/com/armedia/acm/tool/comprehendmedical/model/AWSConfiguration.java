package com.armedia.acm.tool.comprehendmedical.model;

/*-
 * #%L
 * acm-transcribe-tool
 * %%
 * Copyright (C) 2014 - 2019 ArkCase LLC
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
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 05/12/2020
 */
public class AWSConfiguration
{
    private AWSCredentialsConfiguration credentialsConfiguration;
    private AWSComprehendMedicalConfiguration awsComprehendMedicalConfiguration;

    public AWSCredentialsConfiguration getCredentialsConfiguration()
    {
        return credentialsConfiguration;
    }

    public void setCredentialsConfiguration(AWSCredentialsConfiguration credentialsConfiguration)
    {
        this.credentialsConfiguration = credentialsConfiguration;
    }

    public AWSComprehendMedicalConfiguration getAwsTranscribeConfiguration()
    {
        return awsComprehendMedicalConfiguration;
    }

    public void setAwsTranscribeConfiguration(AWSComprehendMedicalConfiguration awsTranscribeConfiguration)
    {
        this.awsComprehendMedicalConfiguration = awsTranscribeConfiguration;
    }
}
