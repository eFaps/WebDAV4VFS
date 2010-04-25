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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.RandomAccessContent;

/**
 * RAM File Random Access Content.
 *
 * @author The eFaps Team
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Apache Commons VFS team</a>
 * @author Apache Commons Id Team
 * @version $Id$
 */
public class RamFileRandomAccessContent
    implements RandomAccessContent
{
    /**
     * File pointer (within {@link #buf}).
     */
    private int filePointer = 0;

    /**
     * File buffer.
     */
    private byte[] buf;

    /**
     * Buffer used to convert a long value to bytes vs.
     *
     * @see #readLong()
     * @see #writeLong(long)
     */
    private final byte[] buffer8 = new byte[8];

    /**
     * Buffer used to convert an integer value to bytes.
     *
     * @see #writeInt(int)
     */
    private final byte[] buffer4 = new byte[4];

    /**
     * buffer
     */
    private final byte[] buffer2 = new byte[2];

    /**
     * Buffer used to store a value in the buffer and then write it to the
     * {@link #buf}.
     *
     * @see #write(int)
     */
    private final byte[] buffer1 = new byte[1];

    /**
     * File object.
     */
    private final RamFileObject file;

    /**
     * Link to the input stream of the file.
     */
    private final InputStream rafis;

    /**
     * @param _file     file to access
     */
    public RamFileRandomAccessContent(final RamFileObject _file)
    {
        super();
        this.buf = _file.getData().getBuffer();
        this.file = _file;

        this.rafis = new InputStream()  {
            @Override()
            public int read() throws IOException
            {
                int ret;
                try  {
                    ret = readByte();
                } catch (final EOFException e)  {
                    ret = -1;
                }
                return ret;
            }

            @Override()
            public long skip(final long _n)
                throws IOException
            {
                seek(getFilePointer() + _n);
                return _n;
            }

            @Override()
            public void close()
            {
            }

            @Override()
            public int read(final byte[] _b)
                throws IOException
            {
                return read(_b, 0, _b.length);
            }

            @Override()
            public int read(final byte[] _b,
                            final int _off,
                            final int _len)
                throws IOException
            {
                final int retLen = Math.min(_len, getLeftBytes());
                RamFileRandomAccessContent.this.readFully(_b, _off, retLen);
                return retLen;
            }

            @Override()
            public int available() throws IOException
            {
                return getLeftBytes();
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @see #filePointer
     */
    public long getFilePointer()
    {
        return this.filePointer;
    }

    /**
     * {@inheritDoc}
     *
     * @see #filePointer
     */
    public void seek(final long _pos)
    {
        this.filePointer = (int) _pos;
    }

    /**
     * {@inheritDoc}
     *
     * @return length of {@link #buf}
     * @see #buf
     */
    public long length()
    {
        return this.buf.length;
    }

    /**
     * Dummy method because files in the memory must not be closed.
     */
    public void close()
    {
    }

    /**
     * {@inheritDoc}
     */
    public byte readByte()
        throws IOException
    {
        return (byte) this.readUnsignedByte();
    }

    /**
     * {@inheritDoc}
     */
    public char readChar()
        throws IOException
    {
        final int ch1 = this.readUnsignedByte();
        final int ch2 = this.readUnsignedByte();
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    /**
     * {@inheritDoc}
     */
    public double readDouble()
        throws IOException
    {
        return Double.longBitsToDouble(this.readLong());
    }

    /**
     * {@inheritDoc}
     */
    public float readFloat()
        throws IOException
    {
        return Float.intBitsToFloat(this.readInt());
    }

    /**
     * {@inheritDoc}
     */
    public int readInt()
        throws IOException
    {
        return (readUnsignedByte() << 24) | (readUnsignedByte() << 16)
                | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    /**
     * {@inheritDoc}
     * <p>Returns the next byte on the position {@link #filePointer} within
     * {@link #buf}.</p>
     */
    public int readUnsignedByte()
        throws EOFException
    {
        if (this.filePointer < this.buf.length)  {
            return this.buf[this.filePointer++] & 0xFF;
        } else  {
            throw new EOFException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int readUnsignedShort()
        throws IOException
    {
        this.readFully(this.buffer2);
        return toUnsignedShort(this.buffer2);
    }

    /**
     * {@inheritDoc}
     */
    public long readLong()
        throws IOException
    {
        this.readFully(this.buffer8);
        return toLong(this.buffer8);
    }

    /**
     * {@inheritDoc}
     */
    public short readShort()
        throws IOException
    {
        this.readFully(this.buffer2);
        return toShort(this.buffer2);
    }

    /**
     * {@inheritDoc}
     */
    public boolean readBoolean()
        throws IOException
    {
        return (this.readUnsignedByte() != 0);
    }

    /**
     * {@inheritDoc}
     */
    public int skipBytes(final int _n)
        throws IOException
    {
        if (_n < 0)  {
            throw new IndexOutOfBoundsException(
                    "The skip number can't be negative");
        }

        final long newPos = this.filePointer + _n;

        if (newPos > this.buf.length)  {
            throw new IndexOutOfBoundsException("Tyring to skip too much bytes");
        }

        seek(newPos);

        return _n;
    }

    /**
     * {@inheritDoc}
     */
    public void readFully(final byte[] _bytes)
        throws IOException
    {
        this.readFully(_bytes, 0, _bytes.length);
    }

    /**
     * {@inheritDoc}
     */
    public void readFully(final byte[] _bytes,
                          final int _off,
                          final int _len)
    {
        if (_len < 0)  {
            throw new IndexOutOfBoundsException("Length is lower than 0");
        }

        if (_len > this.getLeftBytes())  {
            throw new IndexOutOfBoundsException("Read length (" + _len
                    + ") is higher than buffer left bytes ("
                    + this.getLeftBytes() + ") ");
        }

        System.arraycopy(this.buf, this.filePointer, _bytes, _off, _len);

        this.filePointer += _len;
    }

    private int getLeftBytes()
    {
        return this.buf.length - this.filePointer;
    }

    /**
     * {@inheritDoc}
     */
    public String readUTF()
        throws IOException
    {
        return DataInputStream.readUTF(this);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException if the file could not be resized
     */
    public void write(final byte[] _b,
                      final int _off,
                      final int _len)
        throws IOException
    {
        if (this.getLeftBytes() < _len)  {
            final int newSize = this.buf.length + _len - this.getLeftBytes();
            this.file.resize(newSize);
            this.buf = this.file.getData().getBuffer();
        }
        System.arraycopy(_b, _off, this.buf, this.filePointer, _len);
        this.filePointer += _len;
    }

    /**
     * {@inheritDoc}
     * <p>Internally method {@link #write(byte[], int, int)} is used to
     * write.</p>
     *
     * @see #write(byte[], int, int)
     */
    public void write(final byte[] _b)
        throws IOException
    {
        this.write(_b, 0, _b.length);
    }

    /**
     * {@inheritDoc}
     */
    public void writeByte(final int _i)
        throws IOException
    {
        this.write(_i);
    }

    /**
     * Build a long from first 8 bytes of the array.
     *
     * @param _b    byte[] to convert
     * @return related long value
     * @author Apache-Commons-Id Team
     */
    public static long toLong(final byte[] _b)
    {
        return ((((long) _b[7]) & 0xFF) + ((((long) _b[6]) & 0xFF) << 8)
                + ((((long) _b[5]) & 0xFF) << 16)
                + ((((long) _b[4]) & 0xFF) << 24)
                + ((((long) _b[3]) & 0xFF) << 32)
                + ((((long) _b[2]) & 0xFF) << 40)
                + ((((long) _b[1]) & 0xFF) << 48) + ((((long) _b[0]) & 0xFF) << 56));
    }

    /**
     * Build a 8-byte array from a long. No check is performed on the array
     * length.
     *
     * @param _n    number to convert.
     * @param _b
     *            The array to fill.
     * @return A byte[].
     * @author Apache-Commons-Id Team
     */
    public static byte[] toBytes(final long _n,
                                 final byte[] _b)
    {
        long n = _n;
        _b[7] = (byte) (_n);
        n >>>= 8;
        _b[6] = (byte) (_n);
        n >>>= 8;
        _b[5] = (byte) (_n);
        n >>>= 8;
        _b[4] = (byte) (_n);
        n >>>= 8;
        _b[3] = (byte) (_n);
        n >>>= 8;
        _b[2] = (byte) (_n);
        n >>>= 8;
        _b[1] = (byte) (_n);
        n >>>= 8;
        _b[0] = (byte) (_n);
        return _b;
    }

    /**
     * Build a short from first 2 bytes of the array.
     *
     * @param _b    byte array to convert
     * @return a short
     * @author Apache-Commons-Id Team
     */
    public static short toShort(final byte[] _b)
    {
        return (short) toUnsignedShort(_b);
    }

    /**
     * Build a short from first 2 bytes of the array.
     *
     * @param _b    byte array to convert.
     * @return a short
     * @author Apache-Commons-Id Team
     */
    public static int toUnsignedShort(final byte[] _b)
    {
        return ((_b[1] & 0xFF) + ((_b[0] & 0xFF) << 8));
    }

    /**
     * {@inheritDoc}
     */
    public void write(final int _b)
        throws IOException
    {
        this.buffer1[0] = (byte) _b;
        this.write(this.buffer1);
    }

    /**
     * {@inheritDoc}
     */
    public void writeBoolean(final boolean _value)
        throws IOException
    {
        this.write(_value ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    public void writeBytes(final String _value)
        throws IOException
    {
        write(_value.getBytes());
    }

    /**
     * {@inheritDoc}
     */
    public void writeChar(final int _value)
        throws IOException
    {
        this.buffer2[0] = (byte) ((_value >>> 8) & 0xFF);
        this.buffer2[1] = (byte) ((_value >>> 0) & 0xFF);
        write(this.buffer2);
    }

    /**
     * {@inheritDoc}
     */
    public void writeChars(final String _value)
        throws IOException
    {
        final int len = _value.length();
        for (int i = 0; i < len; i++)  {
            writeChar(_value.charAt(i));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeDouble(final double _value)
        throws IOException
    {
        writeLong(Double.doubleToLongBits(_value));
    }

    /**
     * {@inheritDoc}
     */
    public void writeFloat(final float _value)
        throws IOException
    {
        writeInt(Float.floatToIntBits(_value));
    }

    /**
     * {@inheritDoc}
     */
    public void writeInt(final int _value)
        throws IOException
    {
        this.buffer4[0] = (byte) ((_value >>> 24) & 0xFF);
        this.buffer4[1] = (byte) ((_value >>> 16) & 0xFF);
        this.buffer4[2] = (byte) ((_value >>> 8) & 0xFF);
        this.buffer4[3] = (byte) (_value & 0xFF);
        write(this.buffer4);
    }

    /**
     * {@inheritDoc}
     */
    public void writeLong(final long _value)
        throws IOException
    {
        write(toBytes(_value, this.buffer8));
    }

    /**
     * {@inheritDoc}
     */
    public void writeShort(final int _value)
        throws IOException
    {
        this.buffer2[0] = (byte) ((_value >>> 8) & 0xFF);
        this.buffer2[1] = (byte) (_value & 0xFF);
        write(this.buffer2);
    }

    /**
     * {@inheritDoc}
     */
    public void writeUTF(final String _str)
        throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(_str.length());
        final DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeUTF(_str);
        dataOut.flush();
        dataOut.close();
        final byte[] b = out.toByteArray();
        write(b);
    }

    /**
     * {@inheritDoc}
     * <p>Throws always {@link UnsupportedOperationException} because method is
     * deprecated.</p>
     */
    public String readLine()
    {
        throw new UnsupportedOperationException("deprecated");
    }

    /**
     * Returns the {@link #rafis input stream}.
     *
     * @return input stream
     * @see #rafis
     */
    public InputStream getInputStream()
    {
        return this.rafis;
    }
}
