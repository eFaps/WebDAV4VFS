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
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.efaps.webdav4vfs.lock.LockException;
import org.efaps.webdav4vfs.lock.LockManager;
import org.efaps.webdav4vfs.vfs.VFSBackend;


/**
 * The body of all COPY or MOVE requests. As these requests are very similar,
 * they are handles mainly by this class. Only the actual execution using the
 * underlying VFS backend is done in sub classes.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public abstract class AbstractCopyMoveBaseHandler
    extends AbstractWebdavHandler
{
    /**
     * Handle a COPY or MOVE request.
     *
     * @param _request      HTTP servlet request
     * @param _response     HTTP servlet response
     * @throws IOException if there is an error executing this request
     */
    @Override
    public void service(final HttpServletRequest _request,
                        final HttpServletResponse _response)
        throws IOException
    {
        boolean overwrite = getOverwrite(_request);
        FileObject object = VFSBackend.resolveFile(_request.getPathInfo());
        FileObject targetObject = getDestination(_request);

        try {
          final LockManager lockManager = LockManager.getInstance();
          LockManager.EvaluationResult evaluation = lockManager.evaluateCondition(targetObject, getIf(_request));
          if (!evaluation.result) {
            _response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
          }
          if ("MOVE".equals(_request.getMethod())) {
            evaluation = lockManager.evaluateCondition(object, getIf(_request));
            if (!evaluation.result) {
              _response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
              return;
            }
          }
        } catch (LockException e) {
          _response.sendError(SC_LOCKED);
          return;
        } catch (ParseException e) {
          _response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
          return;
        }

        if (null == targetObject) {
          _response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }

        if (object.equals(targetObject)) {
          _response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }

        if (targetObject.exists()) {
          if (!overwrite) {
            _response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
          }
          _response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
          FileObject targetParent = targetObject.getParent();
          if (!targetParent.exists() ||
              !FileType.FOLDER.equals(targetParent.getType())) {
            _response.sendError(HttpServletResponse.SC_CONFLICT);
          }
          _response.setStatus(HttpServletResponse.SC_CREATED);
        }

        // delegate the actual execution to a sub class
        this.copyOrMove(object, targetObject, getDepth(_request));
    }

    /**
     * Execute the actual file system operation. Must be implemented by sub
     * classes.
     *
     * @param object the source object
     * @param target the target object
     * @param depth  a depth for copy
     * @throws FileSystemException if there is an error executing the request
     */
    protected abstract void copyOrMove(final FileObject object,
                                       final FileObject target,
                                       final int depth)
        throws FileSystemException;
}
