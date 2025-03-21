package com.armedia.acm.plugins.ecm.service.lock;

/*-
 * #%L
 * ACM Service: Enterprise Content Management
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

import com.armedia.acm.core.exceptions.AcmObjectLockException;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.service.objectlock.model.AcmObjectLock;
import com.armedia.acm.service.objectlock.service.ObjectLockingProvider;

/**
 * Implementation of {@link ObjectLockingProvider} that handles locks for objects of type FILE.
 * 
 * Created by bojan.milenkoski on 03/05/2018.
 */
public class FileLockingProvider extends DefaultEcmObjectLockingProvider
{
    @Override
    public String getObjectType()
    {
        return EcmFileConstants.OBJECT_FILE_TYPE;
    }

    @Override
    public void checkIfObjectLockCanBeAcquired(Long objectId, String objectType, String lockType, boolean checkChildObjects, String userId)
            throws AcmObjectLockException
    {
        super.checkIfObjectLockCanBeAcquired(objectId, objectType, lockType, false, userId);
    }

    @Override
    public synchronized AcmObjectLock acquireObjectLock(Long objectId, String objectType, String lockType, Long expiry,
            boolean lockChildObjects, String userId)
            throws AcmObjectLockException
    {

        return super.acquireObjectLock(objectId, objectType, lockType, expiry, lockChildObjects, userId);
    }

    @Override
    public synchronized void releaseObjectLock(Long objectId, String objectType, String lockType, boolean unlockChildObjects, String userId,
            Long lockId)
            throws AcmObjectLockException
    {
        super.releaseObjectLock(objectId, objectType, lockType, false, userId, lockId);
    }
}
