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

package org.efaps.webdav4vfs.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.efaps.webdav4vfs.util.Util;
import org.efaps.webdav4vfs.vfs.VFSBackend;


/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class GetHandler
    extends AbstractWebdavHandler
{

    @Override()
    public void service(final HttpServletRequest _request,
                        final HttpServletResponse _response)
        throws IOException
    {
        FileObject object = VFSBackend.resolveFile(_request.getPathInfo());

        if (object.exists()) {
            if (FileType.FOLDER.equals(object.getType())) {
                _response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            setHeader(_response, object.getContent());

            final InputStream is = object.getContent().getInputStream();
            final OutputStream os = _response.getOutputStream();
            IOUtils.copyLarge(is, os);
            is.close();
        } else {
            _response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    void setHeader(final HttpServletResponse response,
                   final FileContent _content)
        throws FileSystemException
    {
    response.setHeader("Last-Modified", Util.getDateString(_content.getLastModifiedTime()));
    response.setHeader("Content-Type", _content.getContentInfo().getContentType());
    response.setHeader("ETag", Util.getETag(_content.getFile()));
  }


}
