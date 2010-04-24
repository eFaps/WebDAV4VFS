/*
 * Copyright 2003 - 2010 The eFaps Team
 * Copyright 2009 Matthias L. Jugel.
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

package org.efaps.webdav4vfs.lock;

import java.util.List;

/**
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class LockConditionRequiredException
    extends LockException
{
    /**
     * Serial version unique identifier.
     */
    private static final long serialVersionUID = -2181157994257507110L;

    public LockConditionRequiredException(List<Lock> locks) {
        super(locks);
    }
}
