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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.util.RandomAccessMode;

/**
 * A RAM File contains a single RAM FileData instance, it provides methods to
 * access the data by implementing FileObject interface.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @version $Id$
 */
public class RamFileObject
    extends AbstractFileObject
    implements FileObject
{
    /**
     * File System.
     */
    private final RamFileSystem fs;

    /**
     * RAM File Object Data.
     */
    private RamFileData data;

    /**
     * @param _name The name of the file.
     * @param _fs The FileSystem.
     */
    protected RamFileObject(final FileName _name,
                            final RamFileSystem _fs)
    {
        super(_name, _fs);
        this.fs = _fs;
        this.fs.attach(this);
    }

    private void save()
        throws FileSystemException
    {
        this.fs.save(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetType()
     */
    @Override()
    protected FileType doGetType()
    {
        return this.data.getType();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doListChildren()
     */
    @Override()
    protected String[] doListChildren()
    {
        return this.fs.listChildren(this.getName());
    }

    /**
     * {@inheritDoc}
     *
     * @return length of the {@link #data} buffer
     * @see #data
     */
    @Override()
    protected long doGetContentSize()
    {
        return this.data.getBuffer().length;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetInputStream()
     */
    @Override()
    protected InputStream doGetInputStream()
        throws Exception
    {
        // VFS-210: ram allows to gather an input stream even from a directory. So we need to check the type anyway.
        if (!getType().hasContent())  {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }

        return new ByteArrayInputStream(this.data.getBuffer());
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    protected OutputStream doGetOutputStream(final boolean _append)
    {
        if (!_append)  {
            this.data.setBuffer(new byte[0]);
        }
        return new RamFileOutputStream(this);
    }

    /**
     * {@inheritDoc}
     *
     * @throws FileSystemException if file could not be deleted
     */
    @Override
    protected void doDelete()
        throws FileSystemException
    {
        this.fs.delete(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override()
    protected long doGetLastModifiedTime()
    {
        return this.data.getLastModified();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doSetLastModifiedTime(long)
     */
    protected boolean doSetLastModTime(final long _modtime)
    {
        this.data.setLastModified(_modtime);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doCreateFolder()
     */
    @Override()
    protected void doCreateFolder() throws Exception
    {
        this.injectType(FileType.FOLDER);
        this.save();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doRename(org.apache.commons.vfs.FileObject)
     */
    @Override()
    protected void doRename(final FileObject _newfile)
        throws FileSystemException
    {
        this.fs.rename(this, (RamFileObject) _newfile);
    }

    /**
     * {@inheritDoc}
     *
     * @see RamFileRandomAccessContent
     */
    @Override()
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode _mode)
    {
        return new RamFileRandomAccessContent(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doAttach()
     */
    @Override()
    protected void doAttach()
    {
        this.fs.attach(this);
    }

    /**
     * @return Returns the data.
     */
    RamFileData getData()
    {
        return this.data;
    }

    /**
     * @param _data     data to set
     * @see #data
     */
    void setData(final RamFileData _data)
    {
        this.data = _data;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#injectType(org.apache.commons.vfs.FileType)
     */
    @Override
    protected void injectType(final FileType _fileType)
    {
        this.data.setType(_fileType);
        super.injectType(_fileType);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#endOutput()
     */
    @Override()
    protected void endOutput()
        throws Exception
    {
        super.endOutput();
        this.save();
    }

    /**
     * @return Returns the size of the RAMFileData
     */
    int size()
    {
        return (this.data == null) ? 0 : this.data.size();
    }

    /**
     * @param _newSize      new size of the data
     */
    synchronized void resize(final int _newSize)
    {
        this.data.resize(_newSize);
    }

    /**
     * {@inheritDoc}
     *
     * @see RamFileData#setAttribute(String,Object)
     */
    @Override()
    protected void doSetAttribute(final String _attrName,
                                  final Object _value)
    {
        this.data.setAttribute(_attrName, _value);
    }

    /**
     * {@inheritDoc}
     *
     * @see RamFileData#getAttributes()
     */
    @Override()
    protected Map<?,?> doGetAttributes()
    {
        return this.data.getAttributes();
    }
}
