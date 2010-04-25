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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * RAM File Object Data.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @version $Id$
 */
class RamFileData
    implements Serializable
{
    /**
     * Unique identifier used to serialize.
     */
    private static final long serialVersionUID = 6051674839293160436L;

    /**
     * File Name.
     */
    private FileName name;

    /**
     * File Type.
     */
    private FileType type;

    /**
     * Bytes.
     */
    private byte[] buffer;

    /**
     * Last modified time.
     */
    private long lastModified;

    /**
     * Attributes of the file data.
     */
    private final Map<String,Object> attributes = new HashMap<String,Object>();

    /**
     * Children.
     */
    private Collection<RamFileData> children;

    /**
     * Constructor.
     *
     * @param _name     file name.
     */
    public RamFileData(final FileName _name)
    {
        super();
        this.clear();
        if (_name == null)  {
            throw new IllegalArgumentException("name can not be null");
        }
        this.name = _name;
    }

    /**
     * @return Returns the buffer.
     */
    byte[] getBuffer()
    {
        return this.buffer;
    }

    /**
     * @param _buffer The buffer.
     */
    void setBuffer(final byte[] _buffer)
    {
        updateLastModified();
        this.buffer = _buffer;
    }

    /**
     * @return Returns the lastModified.
     */
    long getLastModified()
    {
        return this.lastModified;
    }

    /**
     * @param _lastModified last modified to set
     */
    void setLastModified(final long _lastModified)
    {
        this.lastModified = _lastModified;
    }

    /**
     * @return Returns the type.
     */
    FileType getType()
    {
        return this.type;
    }

    /**
     * @param _type     type to set
     */
    void setType(final FileType _type)
    {
        this.type = _type;
    }

    /**
     *
     */
    void clear()
    {
        this.buffer = new byte[0];
        updateLastModified();
        this.type = FileType.IMAGINARY;
        this.children = Collections.synchronizedCollection(new ArrayList<RamFileData>());
        this.name = null;
    }

    /**
     * Update the {@link #lastModified last modified date} to current time
     * stamp.
     *
     * @see #lastModified
     */
    void updateLastModified()
    {
        this.lastModified = System.currentTimeMillis();
    }

    /**
     * Returns the file {@link #name}.
     *
     * @return returns the name
     */
    public FileName getName()
    {
        return this.name;
    }

    /**
     * Defines the new file {@link #name}.
     *
     * @param _fileName     new file name
     */
    public void setName(final FileName _fileName)
    {
        this.name = _fileName;
    }

    /**
     * {@inheritDoc}
     *
     * @return string value of the file {@link #name}
     * @see #name
     */
    @Override()
    public String toString()
    {
        return this.name.toString();
    }

    /**
     * Add a child.
     *
     * @param _data     file data of the child
     * @throws FileSystemException if an error occurs.
     */
    void addChild(final RamFileData _data)
        throws FileSystemException
    {
        if (!this.getType().hasChildren())  {
            throw new FileSystemException(
                    "A child can only be added in a folder");
        }

        if (_data == null)  {
            throw new FileSystemException("No child can be null");
        }

        if (this.children.contains(_data))  {
            throw new FileSystemException("Child already exists. " + _data);
        }

        this.children.add(_data);
        updateLastModified();
    }

    /**
     * Remove a child.
     *
     * @param _data The file data.
     * @throws FileSystemException if an error occurs.
     */
    void removeChild(final RamFileData _data)
        throws FileSystemException
    {
        if (!this.getType().hasChildren())  {
            throw new FileSystemException("A child can only be removed from a folder");
        }
        if (!this.children.contains(_data))  {
            throw new FileSystemException("Child not found. " + _data);
        }
        this.children.remove(_data);
        updateLastModified();
    }

    /**
     * @return Returns the children.
     */
    Collection<RamFileData> getChildren()
    {
        if (this.name == null)  {
            throw new IllegalStateException("Data is clear");
        }
        return this.children;
    }

    /**
     * {@inheritDoc}
     *
     * @return <i>true</i> only if <code>_toCompare</code> is this object
     *         itself or if this object and <code>_toCompare</code> has the
     *         same file name
     */
    @Override()
    public boolean equals(final Object _toCompare)
    {
        final boolean ret;
        if (this == _toCompare)  {
            ret = true;
        } else if (!(_toCompare instanceof RamFileData))  {
            ret = false;
        } else  {
            final RamFileData data = (RamFileData) _toCompare;
            ret = this.getName().equals(data.getName());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @return hash code of the file name
     * @see #name
     */
    @Override()
    public int hashCode()
    {
        return this.name.hashCode();
    }

    /**
     *
     * @param _data     data to check if children
     * @return <i>true</i> if {@link #children} contains <code>_data</code>;
     *         otherwise <i>false</i>
     */
    boolean hasChildren(final RamFileData _data)
    {
        return this.children.contains(_data);
    }

    /**
     * @return Returns the size of the buffer
     */
    int size()
    {
        return this.buffer.length;
    }

    /**
     * Resize the buffer.
     *
     * @param _newSize  new buffer size
     */
    void resize(final int _newSize)
    {
        final int size = this.size();
        final byte[] newBuf = new byte[_newSize];
        System.arraycopy(this.buffer, 0, newBuf, 0, size);
        this.buffer = newBuf;
        updateLastModified();
    }

    /**
     * Defines the <code>_value</code> for the attribute
     * <code>_attrName</code>. If <code>_value</code> is <code>null</code>
     * the attribute is removed from {@link #attributes}.
     *
     * @param _attrName     name of the attribute
     * @param _value        value for given attribute
     * @see #attributes
     */
    protected void setAttribute(final String _attrName,
                                final Object _value)
    {
        if (_value == null)  {
            this.attributes.remove(_attrName);
        } else  {
            this.attributes.put(_attrName, _value);
        }
    }

    /**
     * Returns current defined {@link #attributes} of the file.
     *
     * @return all attribute
     * @see #attributes
     */
    protected Map<String,Object> getAttributes()
    {
        return this.attributes;
    }
}
