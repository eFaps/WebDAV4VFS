/*
 * Copyright 2003 - 2010 The eFaps Team
 * Copyright 2007, 2009 Matthias L. Jugel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.webdav4vfs.test;

import org.efaps.webdav4vfs.lock.Lock;
import org.efaps.webdav4vfs.lock.LockConflictException;
import org.efaps.webdav4vfs.lock.LockManager;
import org.efaps.webdav4vfs.util.Util;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Matthias L. Jugel
 */
public class DavLockManagerTest
    extends AbstractDavTestCase
{
    private final String OWNER_STR = "testowner";

    @Test()
    public void testAcquireSingleSharedFileLock()
    {
        final Lock sharedLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        Exception ex = null;
        try {
            LockManager.getInstance().acquireLock(sharedLock);
        } catch (final Exception e) {
            ex = e;
        }
        Assert.assertNull(
                ex,
                "check that exception was thrown");
    }

    @Test()
    public void testAcquireDoubleSharedFileLock()
    {
        final Lock sharedLock1 = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final Lock sharedLock2 = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR + "1", 0, 3600);
        Exception ex = null;
        try {
            LockManager.getInstance().acquireLock(sharedLock1);
            LockManager.getInstance().acquireLock(sharedLock2);
        } catch (final Exception e) {
            ex = e;
        }
        Assert.assertNull(
                ex,
                "check exception was thrown");
    }

    @Test()
    public void testFailToAcquireExclusiveLockOverSharedLock()
    {
        final Lock sharedLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final Lock exclusiveLock = new Lock(aFile, Lock.WRITE, Lock.EXCLUSIVE, OWNER_STR, 0, 3600);
        Exception ex = null;
        try {
            LockManager.getInstance().acquireLock(sharedLock);
            LockManager.getInstance().acquireLock(exclusiveLock);
            Assert.assertTrue(false, "acquireLock() should fail");
        } catch (final Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex, "check that exception was thrown");
        Assert.assertEquals(ex.getClass(), LockConflictException.class);
    }

    @Test()
    public void testConditionUnmappedFails()
        throws Exception
    {
        final String condition = "<http://cid:8080/litmus/unmapped_url> (<opaquelocktoken:cd6798>)";
        Assert.assertFalse(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition for unmapped resource must fail");
    }

    @Test()
    public void testConditionSimpleLockToken()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + ">)";
        LockManager.getInstance().acquireLock(aLock);
        Assert.assertTrue(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition with existing lock token should not fail");
    }

    @Test()
    public void testConditionSimpleLockLokenWrong()
        throws Exception
    {
        Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "x>)";
        LockManager.getInstance().acquireLock(aLock);
        try {
            LockManager.getInstance().evaluateCondition(aFile, condition);
        } catch (final LockConflictException e) {
            Assert.assertFalse(
                    e.getLocks().isEmpty(),
                    "condition with wrong lock token must fail on locked resource");
        }
    }

    @Test()
    public void testConditionSimpleLockTokenAndETag()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "> [" + Util.getETag(aFile) + "])";
        LockManager.getInstance().acquireLock(aLock);
        Assert.assertTrue(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition with existing lock token and correct ETag should not fail");
    }

    @Test()
    public void testConditionSimpleLockTokenWrongAndETag()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "x> [" + Util.getETag(aFile) + "])";
        LockManager.getInstance().acquireLock(aLock);
        LockConflictException ex = null;
        try {
            LockManager.getInstance().evaluateCondition(aFile, condition);
        } catch (final LockConflictException e) {
            ex = e;
        }
        Assert.assertNotNull(
                ex,
                "check that exception was thrown");
        Assert.assertFalse(
                ex.getLocks().isEmpty(),
                "condition with non-existing lock token and correct ETag should fail");
    }

    @Test()
    public void testConditionSimpleLockTokenAndETagWrong()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "> [" + Util.getETag(aFile) + "x])";
        LockManager.getInstance().acquireLock(aLock);
        Assert.assertFalse(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition with existing lock token and incorrect ETag should fail");
    }

    @Test()
    public void testConditionSimpleLockTokenWrongAndETagWrong()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "x> [" + Util.getETag(aFile) + "x])";
        LockManager.getInstance().acquireLock(aLock);
        Assert.assertFalse(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition with non-existing lock token and incorrect ETag should fail");
    }

    @Test()
    public void testConditionSimpleLockTokenWrongAndETagOrSimpleETag()
        throws Exception
    {
        Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String eTag = Util.getETag(aFile);
        final String condition = "(<" + aLock.getToken() + "x> [" + eTag + "]) ([" + eTag + "])";
        LockManager.getInstance().acquireLock(aLock);
        LockConflictException ex = null;
        try {
            LockManager.getInstance().evaluateCondition(aFile, condition);
        } catch (final LockConflictException e) {
            ex = e;
        }
        Assert.assertNotNull(
                ex,
                "check that the lock conflict exception was thrown");
        Assert.assertFalse(
                ex.getLocks().isEmpty(),
                "condition with one correct ETag in list should not fail on locked resource");
    }

    @Test()
    public void testConditionSimpleNegatedLockTokenWrongAndETag()
        throws Exception
    {
        Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String eTag = Util.getETag(aFile);
        final String condition = "(Not <" + aLock.getToken() + "x> [" + eTag + "])";
        Assert.assertTrue(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "condition with negated wrong lock token and correct ETag should not fail on unlocked resource");
    }


    @Test()
    public void testConditionMustNotFail()
        throws Exception
    {
        Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String condition = "(<" + aLock.getToken() + "x>) (Not <DAV:no-lock>)";
        Assert.assertTrue(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "using (Not <DAV:no-lock>) in condition list must not fail on unlocked resource");
    }

    @Test()
    public void testComplexConditionWithBogusLockToken()
        throws Exception
    {
        final Lock aLock = new Lock(aFile, Lock.WRITE, Lock.SHARED, OWNER_STR, 0, 3600);
        final String eTag = Util.getETag(aFile);
        final String condition = "(<" + aLock.getToken() + "> [" + eTag + "x]) (Not <DAV:no-lock> [" + eTag + "x])";
        LockManager.getInstance().acquireLock(aLock);
        Assert.assertFalse(
                LockManager.getInstance().evaluateCondition(aFile, condition).result,
                "complex condition with bogus eTag should fail");
    }
}
