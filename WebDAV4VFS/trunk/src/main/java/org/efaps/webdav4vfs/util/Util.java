/*
 * Copyright 2003 - 2010 The eFaps Team
 * Copyright 2007 Matthias L. Jugel.
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

package org.efaps.webdav4vfs.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public final class Util
{
    /**
     * Date formatter for the HTTP date.
     */
    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     *
     * @param _time     time to format to string
     * @return formatted string for the time
     */
    public static String getDateString(long _time)
    {
        return httpDateFormat.format(new Date(_time));
    }

    /**
     *
     * @param _object
     * @return
     */
    public static String getETag(final FileObject _object)
    {
        final String fileName = _object.getName().getPath();
        String lastModified = "";
        try {
            lastModified = String.valueOf(_object.getContent().getLastModifiedTime());
        } catch (final FileSystemException e) {
            // ignore error here
        }
        return DigestUtils.shaHex(fileName + lastModified);
    }
}
