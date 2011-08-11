/*
 * PSXperia Converter Tool - String search & replacement
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

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class StringReplacement {
    private static Map<String, Object> mFrameworkList;
    private Map<String, String> mMap;
    private File mDataDir;

    public StringReplacement(Map<String, String> map, File dataDir) {
        this.mMap = map;
        this.mDataDir = dataDir;
    }

    public void execute(String[] filesToModify) throws IOException {
        Logger.debug("Replacing strings in XML data.");
        for (String name : filesToModify) {
            File f = new File(mDataDir, name);
            replaceStringsIn(f);
        }
        Logger.debug("Done with string replacement.");
    }

    public void replaceStringsIn(File file) throws IOException {
        String name = file.getPath();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedWriter writer = new BufferedWriter(new FileWriter(name + ".tmp"));
        Logger.debug("Writing to temporary file: %s", name + ".tmp");
        String line;
        while ((line = reader.readLine()) != null) {
            Logger.verbose("Data line before replacement: %s", line);
            Iterator<String> keys = mMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = mMap.get(key);
                line = line.replaceAll(key, value);
            }
            Logger.verbose("Data line after replacement: %s", line);
            writer.write(line);
            writer.newLine();
        }
        reader.close();
        writer.close();
        file.delete();
        FileUtils.moveFile(new File(name + ".tmp"), file);
        Logger.debug("Successfully cleaned up and done with string replacement.");
    }

    public static String filter(String value) {
        return value.replaceAll("\\'", "\\\\\\\\'");
    }
}
