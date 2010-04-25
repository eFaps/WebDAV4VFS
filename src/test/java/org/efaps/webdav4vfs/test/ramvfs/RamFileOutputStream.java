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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs.FileSystemException;

/**
 * OutputStream to a RamFile.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @version $Id$
 */
public class RamFileOutputStream extends OutputStream
{

    /**
     * File.
     */
    protected RamFileObject file;

    /**
     * buffer.
     */
    protected byte[] buffer1 = new byte[1];

    /** File is open or closed. */
    private boolean closed = false;

    private IOException exc;

    /**
     * @param _file     base file
     */
    public RamFileOutputStream(final RamFileObject _file)
    {
        super();
        this.file = _file;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    @Override
    public void write(final byte[] _b,
                      final int _off,
                      final int _len)
    {
        final int size = this.file.getData().size();
        final int newSize = this.file.getData().size() + _len;
        this.file.resize(newSize);
        System.arraycopy(_b, _off, this.file.getData().getBuffer(), size, _len);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#write(int)
     */
    @Override
    public void write(final int _b)
    {
        this.buffer1[0] = (byte) _b;
        this.write(this.buffer1, 0, this.buffer1.length);
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws IOException
    {
        if (!this.closed)  {
            // Notify on close that there was an IOException while writing
            if (this.exc != null)  {
                throw this.exc;
            }
            try  {
                this.closed = true;
                // Close the
                this.file.endOutput();
            } catch (final Exception e)  {
                throw new FileSystemException(e);
            }
        }
    }
}
