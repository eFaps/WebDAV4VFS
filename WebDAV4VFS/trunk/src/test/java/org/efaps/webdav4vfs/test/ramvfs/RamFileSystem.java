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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.local.LocalFileName;

/**
 * A RAM File System.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @version $Id$
 */
public class RamFileSystem
    extends AbstractFileSystem
    implements Serializable
{
    /**
     * Unique identifier used to serialize.
     */
    private static final long serialVersionUID = 5587421059149890672L;

    /**
     * Cache of RAM File Data.
     */
    private Map<FileName,RamFileData> cache = Collections.synchronizedMap(new HashMap<FileName,RamFileData>());

    /**
     * @param _rootName             root file name
     * @param _fileSystemOptions    file system options
     */
    protected RamFileSystem(final FileName _rootName,
                            final FileSystemOptions _fileSystemOptions)
    {
        super(_rootName, null, _fileSystemOptions);
        // create root
        final RamFileData rootData = new RamFileData(_rootName);
        rootData.setType(FileType.FOLDER);
        rootData.setLastModified(System.currentTimeMillis());
        this.cache.put(_rootName, rootData);
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    protected FileObject createFile(final FileName _name)
    {
        return new RamFileObject(_name, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    @SuppressWarnings("unchecked")
    protected void addCapabilities(final Collection _caps)
    {
        _caps.addAll(RamFileProvider.CAPABILITIES);
    }

    /**
     * @param _name The name of the file.
     * @return children The names of the children.
     */
    String[] listChildren(final FileName _name)
    {
        final String[] names;
        final RamFileData data = this.cache.get(_name);
        if ((data == null) || !data.getType().hasChildren())  {
            names = null;
        } else  {
            final Collection<RamFileData> children = data.getChildren();

            names = new String[children.size()];

            int pos = 0;
            for (final RamFileData childData : children)  {
                names[pos] = childData.getName().getBaseName();
                pos++;
            }
        }
        return names;
    }

    /**
     * Delete a file.
     *
     * @param _file     file to delete
     * @throws FileSystemException if file could not be deleted
     */
    void delete(final RamFileObject _file)
        throws FileSystemException
    {
        // root is read only check
        if (_file.getParent() == null)  {
            throw new FileSystemException("unable to delete root");
        }

        // Remove reference from cache
        this.cache.remove(_file.getName());
        // Notify the parent
        final RamFileObject parent = (RamFileObject) this.resolveFile(_file.getParent().getName());
        parent.getData().removeChild(_file.getData());
        parent.close();
        // close the file
        _file.getData().clear();
        _file.close();
    }

    /**
     * Saves a file.
     *
     * @param _fileObject   file object to save
     * @throws FileSystemException if file object could not be saved
     */
    void save(final RamFileObject _fileObject)
        throws FileSystemException
    {

        // Validate name
        if (_fileObject.getData().getName() == null)  {
            throw new FileSystemException(new IllegalStateException("The data has no name. " + _fileObject));
        }

        // Add to the parent
        if (_fileObject.getName().getDepth() > 0)  {
            final RamFileData parentData = this.cache.get(_fileObject.getParent().getName());
            // Only if not already added
            if (!parentData.hasChildren(_fileObject.getData()))  {
                final RamFileObject parent = (RamFileObject) _fileObject.getParent();
                parent.getData().addChild(_fileObject.getData());
                parent.close();
            }
        }
        // Store in cache
        this.cache.put(_fileObject.getName(), _fileObject.getData());
        _fileObject.getData().updateLastModified();
        _fileObject.close();
    }

    /**
     * Moves the original file <code>_from</code> to the new file
     * <code>_to</code>.
     *
     * @param _from     original file
     * @param _to       new file.
     * @throws FileSystemException if an error occurs
     */
    void rename(final RamFileObject _from,
                final RamFileObject _to)
        throws FileSystemException
    {
        if (!this.cache.containsKey(_from.getName()))  {
            throw new FileSystemException("File does not exist: " + _from.getName());
        }
        // copy data
        _to.getData().setBuffer(_from.getData().getBuffer());
        _to.getData().setLastModified(_from.getData().getLastModified());
        _to.getData().setType(_from.getData().getType());
        _to.getData().getAttributes().clear();
        _to.getData().getAttributes().putAll(_from.getData().getAttributes());
        // fix problem that the file name of the data was cleared
        _to.getData().setName(_to.getName());

        this.save(_to);

        // copy children
        if (_from.getType() == FileType.FOLDER)  {
            for (final FileObject child : _from.getChildren())  {
                final FileName newFileName = ((LocalFileName) _to.getName()).createName(
                        _to.getName().getPath() + "/" + child.getName().getBaseName(),
                        child.getName().getType());
                this.rename((RamFileObject) child, new RamFileObject(newFileName, this));
            }
        }

        this.delete(_from);
    }

    /**
     * Attach the related file data to the file object <code>_fo</code>.
     *
     * @param _fo   file object to attach
     */
    public void attach(final RamFileObject _fo)
    {
        if (_fo.getName() == null)  {
            throw new IllegalArgumentException("Null argument");
        }
        RamFileData data = this.cache.get(_fo.getName());
        if (data == null)  {
            data = new RamFileData(_fo.getName());
        }
        _fo.setData(data);
    }

    /**
     * Import a Tree.
     *
     * @param _file The File
     * @throws FileSystemException if an error occurs.
     */
    public void importTree(final File _file)
        throws FileSystemException
    {
        final FileObject fileFo = getFileSystemManager().toFileObject(_file);
        this.toRamFileObject(fileFo, fileFo);
    }

    /**
     * Import the given file with the name relative to the given root.
     *
     * @param _fo       file object
     * @param _root     root file object
     * @throws FileSystemException if file object could not be imported
     */
    void toRamFileObject(final FileObject _fo,
                         final FileObject _root)
        throws FileSystemException
    {
        final RamFileObject memFo = (RamFileObject) this.resolveFile(
                _fo.getName().getPath().substring(_root.getName().getPath().length()));
        if (_fo.getType().hasChildren())  {
            // Create Folder
            memFo.createFolder();
            // Import recursively
            final FileObject[] fos = _fo.getChildren();
            for (int i = 0; i < fos.length; i++)  {
                final FileObject child = fos[i];
                this.toRamFileObject(child, _root);
            }
        } else if (_fo.getType().equals(FileType.FILE))  {
            // Read bytes
            try  {
                final InputStream is = _fo.getContent().getInputStream();
                try  {
                    final OutputStream os = new BufferedOutputStream(memFo.getOutputStream(), 512);
                    int i;
                    while ((i = is.read()) != -1)  {
                        os.write(i);
                    }
                    os.flush();
                    os.close();
                } finally  {
                    try  {
                        is.close();
                    } catch (final IOException e)  {
                        // ignore on close exception
                    }
                }
            } catch (final IOException e)  {
                throw new FileSystemException(e.getClass().getName() + " " + e.getMessage());
            }
        } else  {
            throw new FileSystemException("File is not a folder nor a file " + memFo);
        }
    }

    /**
     * @return Returns the size of the FileSystem
     */
    int size()
    {
        int size = 0;
        for (final RamFileData data : this.cache.values())  {
            size += data.size();
        }
        return size;
    }

    /**
     * Close the RAMFileSystem.
     */
    @Override()
    public void close()
    {
        this.cache = null;
        super.close();
    }
}
