/*
 * PSXperia Converter Tool - Logging
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

import javax.swing.*;

public class Logger {
    public static final int ALL = 0x4;
    public static final int DEBUG = 0x3;
    public static final int INFO = 0x2;
    public static final int WARNING = 0x1;
    public static final int ERROR = 0x0;
    private static Logger ourInstance = new Logger();
    private static int mLevel;
    private static JFrame mFrame;

    public Logger() {
        this.mLevel = ERROR;
    }

    public static Logger getInstance() {
        return ourInstance;
    }

    public static void setLevel(int level) {
        mLevel = level;
    }

    public static int getLevel() {
        return mLevel;
    }

    public static void setGUIFrame(JFrame frame){
        mFrame = frame;
    }

    public static void verbose(String input) {
        verbose("%s", input);
    }

    public static void debug(String input) {
        debug("%s", input);
    }

    public static void info(String input) {
        info("%s", input);
    }

    public static void warning(String input) {
        warning("%s", input);
    }

    public static void error(String input) {
        error("%s", input);
    }

    public static void verbose(String format, Object... input) {
        if (mLevel >= ALL)
            System.out.printf("%s" + format + "\n", addObjectToArray("[V] ", input));
    }

    public static void debug(String format, Object... input) {
        if (mLevel >= DEBUG)
            System.out.printf("%s" + format + "\n", addObjectToArray("[D] ", input));
    }

    public static void info(String format, Object... input) {
        if (mLevel >= INFO)
            System.out.printf("%s" + format + "\n", addObjectToArray("[I] ", input));
    }

    public static void warning(String format, Object... input) {
        if (mLevel >= WARNING){
            System.out.printf("%s" + format + "\n", addObjectToArray("[W] ", input));
            if(mFrame != null)
                JOptionPane.showMessageDialog(mFrame, String.format(format, input), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void error(String format, Object... input) {
        if (mLevel >= ERROR){
            System.out.printf("%s" + format + "\n", addObjectToArray("[E] ", input));
            if(mFrame != null)
                JOptionPane.showMessageDialog(mFrame, String.format(format, input), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Object[] addObjectToArray(Object obj, Object[] arr) {
        Object[] newArr = new Object[arr.length + 1];
        newArr[0] = obj;
        System.arraycopy(arr, 0, newArr, 1, arr.length);
        return newArr;
    }
}
