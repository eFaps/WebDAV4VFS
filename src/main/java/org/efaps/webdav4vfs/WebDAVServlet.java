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

package org.efaps.webdav4vfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.efaps.webdav4vfs.handler.AbstractWebdavHandler;
import org.efaps.webdav4vfs.handler.CopyHandler;
import org.efaps.webdav4vfs.handler.DeleteHandler;
import org.efaps.webdav4vfs.handler.GetHandler;
import org.efaps.webdav4vfs.handler.HeadHandler;
import org.efaps.webdav4vfs.handler.LockHandler;
import org.efaps.webdav4vfs.handler.MkColHandler;
import org.efaps.webdav4vfs.handler.MoveHandler;
import org.efaps.webdav4vfs.handler.OptionsHandler;
import org.efaps.webdav4vfs.handler.PostHandler;
import org.efaps.webdav4vfs.handler.PropFindHandler;
import org.efaps.webdav4vfs.handler.PropPatchHandler;
import org.efaps.webdav4vfs.handler.PutHandler;
import org.efaps.webdav4vfs.handler.UnlockHandler;
import org.efaps.webdav4vfs.vfs.VFSBackend;


/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class WebDAVServlet
    extends HttpServlet
{
    /**
     * Serial version unique identifier.
     */
    private static final long serialVersionUID = 8773578894080767401L;

    private static final Log LOG = LogFactory.getLog(WebDAVServlet.class);

    private final Map<String, AbstractWebdavHandler> handlers = new HashMap<String, AbstractWebdavHandler>();

    public WebDAVServlet()
    {
        this.handlers.put("COPY", new CopyHandler());
        this.handlers.put("DELETE", new DeleteHandler());
        this.handlers.put("GET", new GetHandler());
        this.handlers.put("HEAD", new HeadHandler());
        this.handlers.put("LOCK", new LockHandler());
        this.handlers.put("MKCOL", new MkColHandler());
        this.handlers.put("MOVE", new MoveHandler());
        this.handlers.put("OPTIONS", new OptionsHandler());
        this.handlers.put("POST", new PostHandler());
        this.handlers.put("PROPFIND", new PropFindHandler());
        this.handlers.put("PROPPATCH", new PropPatchHandler());
        this.handlers.put("PUT", new PutHandler());
        this.handlers.put("UNLOCK", new UnlockHandler());
    }

    @Override()
    public void init(final ServletConfig servletConfig)
        throws ServletException
    {
        super.init(servletConfig);
        String rootUri = servletConfig.getInitParameter("vfs.uri");
        String authDomain = servletConfig.getInitParameter("vfs.auth.domain");
        String authUser = servletConfig.getInitParameter("vfs.auth.user");
        String authPass = servletConfig.getInitParameter("vfs.auth.password");
        try {
            StaticUserAuthenticator userAuthenticator =
                new StaticUserAuthenticator(authDomain, authUser, authPass);
            FileSystemOptions options = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, userAuthenticator);

            VFSBackend.initialize(rootUri, options);
        } catch (FileSystemException e) {
            LOG.error(String.format("can't create file system backend for '%s'", rootUri));
        }
    }

    @Override()
    public void service(final HttpServletRequest request,
                        final HttpServletResponse response)
        throws ServletException, IOException
    {
//    String auth = request.getHeader("Authorization");
//    String login = "", password = "";
//
//    if (auth != null) {
//      auth = new String(Base64.decodeBase64(auth.substring(auth.indexOf(' ') + 1).getBytes()));
//      login = auth.substring(0, auth.indexOf(':'));
//      password = auth.substring(auth.indexOf(':') + 1);
//    }
//
//    AWSCredentials credentials = AWSCredentials.load(password,  ))
//    if (user == null) {
//      response.setHeader("WWW-Authenticate", "Basic realm=\"Moxo\"");
//      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//      return;
//    }


        // show we are doing the litmus test
        String litmusTest = request.getHeader("X-Litmus");
        if (null == litmusTest) {
            litmusTest = request.getHeader("X-Litmus-Second");
        }
        if (litmusTest != null) {
            LOG.info(String.format("WebDAV Litmus Test: %s", litmusTest));
        }

        String method = request.getMethod();
        LOG.debug(String.format(">> %s %s", request.getMethod(), request.getPathInfo()));
        if (handlers.containsKey(method)) {
            handlers.get(method).service(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
        LOG.debug(String.format("<< %s (%s)", request.getMethod(), response.toString().replaceAll("[\\r\\n]+", "")));
    }
}
