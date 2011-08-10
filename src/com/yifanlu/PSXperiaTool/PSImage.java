/*
 * PSXperia Converter Tool - Generic PSImage functions
 * Copyright (C) 2011 Yifan Lu (http://yifan.lu/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.yifanlu.PSXperiaTool;

import java.io.InputStream;

public abstract class PSImage {
    public interface ProgressCallback {
        public void bytesReadChanged(int delta);

        public void bytesWrittenChanged(int delta);
    }

    public final static int BLOCK_SIZE = 1024;
    public final static long PART_SIZE = 0x9300;
    public final static int PART_BS = 0x700;
    protected final static byte[] HEADER = {7, 112, -2, -38, 3, 0, 0, 0, 16, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    protected InputStream mIn;
    protected byte[] mBuff;
    protected int mBytesWritten;
    protected int mBytesRead;
    private ProgressCallback mCallback;

    public PSImage() {
        this.mIn = null;
        this.mBuff = null;
    }

    protected PSImage(InputStream in) {
        this.mIn = in;
        this.mBuff = new byte[BLOCK_SIZE];
    }

    protected void check() throws IllegalArgumentException {
        if (mIn == null || mBuff == null)
            throw new IllegalArgumentException("Not enough data to start!");
    }

    public void setCallback(ProgressCallback call) {
        mCallback = call;
    }

    public void removeCallback() {
        mCallback = null;
    }

    protected synchronized void addBytesRead(int num) {
        mBytesRead += num;
        if (mCallback != null)
            mCallback.bytesReadChanged(num);
    }

    protected synchronized void addBytesWritten(int num) {
        mBytesWritten += num;
        if (mCallback != null)
            mCallback.bytesWrittenChanged(num);
    }

    public synchronized int getBytesRead() {
        return mBytesRead;
    }

    public synchronized int getBytesWritten() {
        return mBytesWritten;
    }
}
