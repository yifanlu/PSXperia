/*
 * PSXperia Converter Tool - PSImage Extraction
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PSImageExtract extends PSImage {
    public PSImageExtract(InputStream in) {
        super(in);
    }

    public void uncompress(OutputStream out) throws IOException, DataFormatException {
        Logger.info("Extracting PS Image [BETA! This tool WILL return with an exception, even if successful].");
        check();
        mIn.skip(HEADER.length);
        mBytesRead = HEADER.length;
        mBytesWritten = 0;
        Inflater inf = new Inflater();
        int n;
        byte[] buff = new byte[BLOCK_SIZE];
        try
        {
            while ((n = inf.inflate(buff, 0, BLOCK_SIZE)) != -1) {
                out.write(buff, 0, n);
                addBytesWritten(n);
                //System.out.println("Bytes written: " + getBytesWritten());

                System.out.println(inf.finished());
                System.out.println(inf.needsDictionary());

                if (inf.finished() || inf.needsDictionary()) {
                    int remainder = inf.getRemaining();
                    changeBuffer(remainder);
                    inf.reset();
                    inf.setInput(mBuff, 0, remainder);
                    System.out.println("WE HAVE CHANGED IT!");
                } else if (inf.needsInput()) {
                    fill(inf);
                }
            }
        }catch(DataFormatException ex){
            out.write(new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
            out.write(mBuff);
            byte[] b = new byte[1024];
            mIn.read(b);
            out.write(b);
            out.close();
            throw ex;
        }
        Logger.debug("Extraction done.");
    }

    protected void fill(Inflater inf) throws IOException {
        Logger.verbose("Filling buffer with compressed data.");
        if (mBuff.length < BLOCK_SIZE)
            mBuff = new byte[BLOCK_SIZE];
        int n = mIn.read(mBuff, 0, BLOCK_SIZE);
        if (n == -1) {
            throw new IOException("Unexpected end of ZLIB input stream");
        }
        inf.setInput(mBuff, 0, BLOCK_SIZE);
        addBytesRead(n);
    }

    protected void changeBuffer(int remaining) throws IOException {
        Logger.verbose("End of segment, shifting buffer and trying next segment.");
        int bs = mBuff.length;
        byte[] newBuff = new byte[remaining];
        int i, j;
        i = bs - remaining;
        j = 0;
        while (i < bs) {
            newBuff[j++] = mBuff[i++];
        }
        mBuff = newBuff;
    }

}
