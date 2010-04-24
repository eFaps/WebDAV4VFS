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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.efaps.webdav4vfs.data.DavResource;
import org.efaps.webdav4vfs.data.DavResourceFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

/**
 * Helper class for DAV tests.
 *
 * @author Matthias L. Jugel
 */
public abstract class AbstractDavTestCase
{
    private static final String PROP_EXISTS = "propstat[status='HTTP/1.1 200 OK']/prop/";
    private static final String PROP_MISSING = "propstat[status='HTTP/1.1 404 Not Found']/prop/";
    private static final String EMPTY = "";

    protected FileObject aFile;
    protected FileObject aDirectory;

    @BeforeMethod()
    public void setUp()
        throws Exception
    {
        FileSystemManager fsm = VFS.getManager();
        FileObject fsRoot = fsm.createVirtualFileSystem(fsm.resolveFile("ram:/"));
        this.aFile = fsRoot.resolveFile("/file.txt");
        this.aFile.delete();
        this.aFile.createFile();
        this.aDirectory = fsRoot.resolveFile("/folder");
        this.aDirectory.delete();
        this.aDirectory.createFolder();
    }

    protected void testPropertyValue(final FileObject _object,
                                     final String _propertyName,
                                     final String _propertyValue)
        throws FileSystemException
    {
        final Element root = serializeDavResource(_object, _propertyName);
        Assert.assertEquals(selectExistingPropertyValue(root, _propertyName), _propertyValue);
    }

    protected void testPropertyNoValue(final FileObject _object,
                                       final String _propertyName)
        throws FileSystemException
    {
        final Element root = serializeDavResource(_object, _propertyName);
        Assert.assertEquals(selectExistingPropertyValue(root, _propertyName), EMPTY);
    }

    protected Node selectExistingProperty(final Element _root,
                                          final String _propertyName)
    {
        return _root.selectSingleNode(PROP_EXISTS + _propertyName);
    }

    protected Node selectMissingProperty(final Element _root,
                                         final String _propertyName)
    {
        return _root.selectSingleNode(PROP_MISSING + _propertyName);
    }

    protected String selectMissingPropertyName(final Element _root,
                                               final String _propertyName)
    {
        return selectMissingProperty(_root, _propertyName).getName();
    }

    protected String selectExistingPropertyValue(final Element _root,
                                                 final String _propertyName)
    {
        return selectExistingProperty(_root, _propertyName).getText();
    }

    protected Element serializeDavResource(final FileObject _object,
                                           final String _propertyName)
        throws FileSystemException
    {
        final Element root = DocumentHelper.createElement("root");
        final DavResourceFactory factory = DavResourceFactory.getInstance();
        final DavResource davResource = factory.getDavResource(_object);

        Element testPropertyEl = (Element) root.addElement("prop").detach();
        testPropertyEl.addElement(_propertyName);

        davResource.getPropertyValues(root, testPropertyEl);

        return root;
    }
}
