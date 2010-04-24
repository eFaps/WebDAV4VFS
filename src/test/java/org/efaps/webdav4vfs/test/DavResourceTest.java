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

import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Element;
import org.efaps.webdav4vfs.data.DavCollection;
import org.efaps.webdav4vfs.data.DavResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for the DAV resource wrapper. Checks that resources are serialized
 * correctly.
 *
 * @author Matthias L. Jugel
 */
public class DavResourceTest
    extends AbstractDavTestCase
{
    @Test()
    public void testFileCreationDateIsNull()
        throws FileSystemException
    {
        final Element root = serializeDavResource(aFile, DavResource.PROP_CREATION_DATE);
        Assert.assertNull(selectExistingProperty(root, DavResource.PROP_CREATION_DATE));
    }

    @Test()
    public void testFileCreationDateIsMissing()
        throws IOException
    {
        final Element root = serializeDavResource(aFile, DavResource.PROP_CREATION_DATE);
        Assert.assertEquals(
                selectMissingPropertyName(root, DavResource.PROP_CREATION_DATE),
                DavResource.PROP_CREATION_DATE);
    }

    @Test()
    public void testFileDisplayNameWithValue()
        throws FileSystemException
    {
        testPropertyValue(aFile, DavResource.PROP_DISPLAY_NAME, aFile.getName().getBaseName());
    }

    @Test()
    public void testFileResourceTypeNotMissing()
        throws FileSystemException
    {
        final Element root = serializeDavResource(aFile, DavResource.PROP_RESOURCETYPE);
        Assert.assertNull(selectMissingProperty(root, DavResource.PROP_RESOURCETYPE));
    }

    @Test()
    public void testDirectoryResourceTypeNotMissing()
        throws FileSystemException
    {
        final Element root = serializeDavResource(aDirectory, DavResource.PROP_RESOURCETYPE);
        Assert.assertNull(selectMissingProperty(root, DavResource.PROP_RESOURCETYPE));
    }

    @Test()
    public void testFileResourceType()
        throws FileSystemException
    {
        testPropertyNoValue(aFile, DavResource.PROP_RESOURCETYPE);
    }

    @Test()
    public void testDirectoryResourceType()
        throws FileSystemException
    {
        final Element root = serializeDavResource(aDirectory, DavResource.PROP_RESOURCETYPE);
        Assert.assertNotNull(selectExistingProperty(root, DavResource.PROP_RESOURCETYPE).selectSingleNode(DavCollection.COLLECTION));
    }
}
