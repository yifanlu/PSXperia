/*
 * PSXperia Converter Tool - Test
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

package com.yifanlu.PSXperiaTool.Test;

import com.yifanlu.PSXperiaTool.Logger;
import com.yifanlu.PSXperiaTool.PSXperiaTool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class MainTest {

    /*
        There is no GUI yet, you have to manually plug in the fields below.
     */
    public static void main(String[] args) throws Exception {
        Logger.setLevel(Logger.DEBUG);
        Properties defaults = new Properties();
        File currentDir = new File(new File(".").getAbsolutePath());
        defaults.loadFromXML(new FileInputStream(new File(currentDir, "/defaults/defaults.xml")));

        // SETTINGS
        defaults.put("ImageName", "/path/to/game.iso");
        defaults.put("OutputDirectory", (new File(currentDir, "/output")).getAbsolutePath());
        defaults.put("KEY_TITLE_ID", "NCXA00000_1");
        defaults.put("IconFile", new File("/path/to/icon.png"));
        defaults.put("KEY_PUBLISHER", "Game publisher here.");
        defaults.put("KEY_DEVELOPER", "Game developer here.");
        defaults.put("KEY_DISPLAY_NAME", "Game name here.");
        defaults.put("KEY_TITLE", "Game title here.");
        defaults.put("KEY_DESCRIPTION", "Game description here.");
        // END SETTINGS

        PSXperiaTool xps = new PSXperiaTool(currentDir, defaults);
        xps.startBuild();
    }
}
