/*
 * PSXperia Converter Tool - Zpak creation
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZpakCreate {
    public static final int BLOCK_SIZE = 1024;
    private OutputStream mOut;
    private File mDirectory;
    private byte[] mBuffer;

    public ZpakCreate(OutputStream out, File directory) {
        this.mOut = out;
        this.mDirectory = directory;
        this.mBuffer = new byte[BLOCK_SIZE];
    }

    public void create(boolean noCompress) throws IOException {
        Logger.info("Generating zpak file from directory %s with compression = %b", mDirectory.getPath(), noCompress);
        IOFileFilter filter = new IOFileFilter() {
            public boolean accept(File file) {
                if (file.getName().startsWith(".")) {
                    Logger.debug("Skipping file %s", file.getPath());
                    return false;
                }
                return true;
            }

            public boolean accept(File file, String s) {
                if (s.startsWith(".")) {
                    Logger.debug("Skipping file %s", file.getPath());
                    return false;
                }
                return true;
            }
        };
        Iterator<File> it = FileUtils.iterateFiles(mDirectory, filter, TrueFileFilter.INSTANCE);
        ZipOutputStream out = new ZipOutputStream(mOut);
        out.setMethod(noCompress ? ZipEntry.STORED : ZipEntry.DEFLATED);
        while (it.hasNext()) {
            File current = it.next();
            FileInputStream in = new FileInputStream(current);
            ZipEntry zEntry = new ZipEntry(current.getPath().replaceAll(mDirectory.getPath(), "").substring(1));
            if (noCompress) {
                zEntry.setSize(in.getChannel().size());
                zEntry.setCompressedSize(in.getChannel().size());
                zEntry.setCrc(getCRC32(current));
            }
            out.putNextEntry(zEntry);
            Logger.verbose("Adding file %s", current.getPath());
            int n;
            while ((n = in.read(mBuffer)) != -1) {
                out.write(mBuffer, 0, n);
            }
        }
        out.close();
        Logger.debug("Done with ZPAK creation.");
    }

    public static long getCRC32(File file) throws IOException {
        CheckedInputStream cis = null;
        long fileSize = 0;
        // Computer CRC32 checksum
        cis = new CheckedInputStream(
                new FileInputStream(file), new CRC32());

        fileSize = file.length();

        byte[] buf = new byte[128];
        while (cis.read(buf) != -1) ;

        long checksum = cis.getChecksum().getValue();
        cis.close();
        Logger.verbose("CRC32 of %s is %d", file.getPath(), checksum);
        return checksum;
    }
}
