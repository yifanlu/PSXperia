/*
 * PSXperia Converter Tool - PSImage creation
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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class PSImageCreate extends PSImage {
    public final static byte[] TOC_HEADER = {0x04, 0x00, 0x00, 0x00};
    public final static byte[] TOC_CONST = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00};
    private ArrayList<Integer> mEntries;

    public PSImageCreate(InputStream in) {
        super(in);
    }

    public void compress(OutputStream out) throws IOException {
        check();
        Logger.info("Beginning PSImage compression.");
        mEntries = new ArrayList<Integer>();
        out.write(HEADER);
        mBytesRead = 0;
        mBytesWritten = HEADER.length;
        while (mIn.available() != 0) {
            InputStream in = splitPart();
            compressPart(in, out);
            in.close();
        }
        (new File("temp.dat")).deleteOnExit();
    }

    protected FileInputStream splitPart() throws IOException {
        Logger.verbose("Splitting image.");
        FileOutputStream out = new FileOutputStream("temp.dat");
        byte[] buff = new byte[PART_BS];
        int n;
        long count = PART_SIZE / PART_BS;
        while (count-- > 0) {
            if ((n = mIn.read(buff, 0, PART_BS)) == -1)
                break;
            addBytesRead(n);
            out.write(buff, 0, n);
        }
        out.close();
        Logger.verbose("Done splitting image.");
        return new FileInputStream("temp.dat");
    }

    protected void compressPart(InputStream in, OutputStream out) throws IOException {
        Logger.verbose("Compressing part.");
        Deflater defl = new Deflater(Deflater.DEFAULT_COMPRESSION);
        DeflaterOutputStream zOut = new DeflaterOutputStream(out, defl);
        // temp, Deflate.getBytesWritten() is broken
        mEntries.add((int) ((FileOutputStream) out).getChannel().size());
        //
        byte[] buff = new byte[PART_BS];
        int n;
        while ((n = in.read(buff, 0, PART_BS)) != -1) {
            zOut.write(buff, 0, n);
        }
        addBytesWritten((int) defl.getBytesWritten());
        zOut.finish();
        Logger.verbose("Done compressing part.");
    }

    public void writeTocTable(OutputStream out) throws IOException {
        Logger.info("Generating image TOC.");
        int size = (0x18 + 0x4 * mEntries.size());
        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(TOC_HEADER);
        bb.putInt(getBytesRead());
        bb.put(TOC_CONST);
        bb.putInt(mEntries.size());
        Iterator<Integer> it = mEntries.iterator();
        while (it.hasNext()) {
            bb.putInt(it.next());
        }
        out.write(bb.array());
    }
}
