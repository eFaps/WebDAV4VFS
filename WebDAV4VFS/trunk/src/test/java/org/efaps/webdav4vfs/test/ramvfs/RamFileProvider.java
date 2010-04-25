/*
 * Copyright 2003 - 2010 The eFaps Team
 * Copyright 2002 - 2006 Apache Software Foundation (ASF)
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

package org.efaps.webdav4vfs.test.ramvfs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.FileProvider;


/**
 * RAM File Provider.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @version $Id$
 */
public class RamFileProvider
    extends AbstractOriginatingFileProvider
    implements FileProvider
{
    /** The provider's capabilities. */
    public static final Collection<Capability> CAPABILITIES
        = Collections.unmodifiableCollection(Arrays.asList(new Capability[] {
            Capability.CREATE, Capability.DELETE, Capability.RENAME,
            Capability.GET_TYPE, Capability.GET_LAST_MODIFIED,
            Capability.SET_LAST_MODIFIED_FILE,
            Capability.SET_LAST_MODIFIED_FOLDER,
            Capability.LIST_CHILDREN, Capability.READ_CONTENT,
            Capability.URI, Capability.WRITE_CONTENT,
            Capability.APPEND_CONTENT, Capability.RANDOM_ACCESS_READ,
            Capability.RANDOM_ACCESS_WRITE}));

    /**
     * Constructor.
     */
    public RamFileProvider()
    {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractOriginatingFileProvider#doCreateFileSystem(
     *      org.apache.commons.vfs.FileName, org.apache.commons.vfs.FileSystemOptions)
     */
    @Override()
    protected FileSystem doCreateFileSystem(final FileName _name,
                                            final FileSystemOptions _fileSystemOptions)
    {
        return new RamFileSystem(_name, _fileSystemOptions);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.FileProvider#getCapabilities()
     */
    public Collection<Capability> getCapabilities()
    {
        return RamFileProvider.CAPABILITIES;
    }
}
