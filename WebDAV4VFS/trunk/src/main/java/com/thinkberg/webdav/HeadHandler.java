/*
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
 */

package com.thinkberg.webdav;

import com.thinkberg.webdav.vfs.VFSBackend;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class HeadHandler extends GetHandler {

  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FileObject object = VFSBackend.resolveFile(request.getPathInfo());

    if (object.exists()) {
      if (FileType.FOLDER.equals(object.getType())) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      } else {
        setHeader(response, object.getContent());
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
