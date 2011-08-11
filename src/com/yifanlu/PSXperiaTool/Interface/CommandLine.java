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

package com.yifanlu.PSXperiaTool.Interface;

import com.android.sdklib.internal.build.SignedJarBuilder;
import com.yifanlu.PSXperiaTool.Extractor.CrashBandicootExtractor;
import com.yifanlu.PSXperiaTool.Logger;
import com.yifanlu.PSXperiaTool.PSImageExtract;
import com.yifanlu.PSXperiaTool.PSXperiaTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Stack;
import java.util.zip.DataFormatException;

public class CommandLine {
    private static class InvalidArgumentException extends Exception {
        private String mMessage;

        public InvalidArgumentException(String message) {
            this.mMessage = message;
        }

        public String getMessage() {
            return this.mMessage;
        }
    }

    public static void main(String[] args) {
        Logger.setLevel(Logger.DEBUG);
        if (args.length < 1)
            printHelp();
        String toDo = args[0];
        try {
            if (toDo.equals("extract") || toDo.equals("x"))
                doExtractData(args);
            if (toDo.equals("convert") || toDo.equals("c"))
                doConvertImage(args);
            if (toDo.equals("decompress") || toDo.equals("d"))
                doDecompressImage(args);
        } catch (InvalidArgumentException ex) {
            Logger.error("Invalid argument: %s", ex.getMessage());
            printHelp();
        } catch (IOException ex) {
            Logger.error("IO error, Java says: %s", ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.error("Process exec Error, Java says: %s", ex.toString());
            ex.printStackTrace();
        } catch (DataFormatException ex) {
            Logger.error("Data format error, Java says: %s", ex.toString());
            ex.printStackTrace();
        } catch (GeneralSecurityException ex) {
            Logger.error("Error signing JAR, Java says: %s", ex.toString());
            ex.printStackTrace();
        } catch (SignedJarBuilder.IZipEntryFilter.ZipAbortException ex) {
            Logger.error("Error signing JAR, Java says: %s", ex.toString());
            ex.printStackTrace();
        }
    }

    public static void doExtractData(String[] args) throws InvalidArgumentException, IOException {
        if (args.length < 4)
            throw new InvalidArgumentException("Not enough input.");

        Stack<String> stack = new Stack<String>();
        stack.addAll(Arrays.asList(args));
        File outputDir = new File(stack.pop());
        File inputZpak = new File(stack.pop());
        File inputApk = new File(stack.pop());

        while (!stack.empty()) {
            String argument = stack.pop();
            if (argument.startsWith("-")) {
                if (argument.equals("-v") || argument.equalsIgnoreCase("--verbose")) {
                    Logger.setLevel(Logger.ALL);
                    continue;
                }
                Logger.warning("Unknown option %s", argument);
            }
        }
        (new CrashBandicootExtractor(inputApk, inputZpak, outputDir)).extractApk();
        System.exit(0);
    }

    public static void doConvertImage(String[] args) throws InvalidArgumentException, IOException, InterruptedException, GeneralSecurityException, SignedJarBuilder.IZipEntryFilter.ZipAbortException {
        if (args.length < 3)
            throw new InvalidArgumentException("Not enough input.");

        Stack<String> stack = new Stack<String>();
        stack.addAll(Arrays.asList(args));
        File outputDir = new File(stack.pop());
        File inputFile = new File(stack.pop());
        String titleId = stack.pop();

        Properties settings = new Properties();
        settings.loadFromXML(PSXperiaTool.class.getResourceAsStream("/resources/defaults.xml"));
        settings.put("KEY_TITLE_ID", titleId);
        File currentDir = new File(".");
        File dataDir = new File(currentDir, "/data");

        Stack<String> stringList = new Stack<String>();
        while (!stack.empty()) {
            String argument = stack.pop();
            if (argument.startsWith("-")) {
                if (argument.equals("-v") || argument.equalsIgnoreCase("--verbose")) {
                    Logger.setLevel(Logger.ALL);
                    continue;
                } else if (argument.equals("-D")) {
                    dataDir = new File(stringList.pop());
                    stringList.empty();
                } else if (argument.equals("--load-xml")) {
                    File xml = new File(stringList.pop());
                    settings.loadFromXML(new FileInputStream(xml));
                    stringList.empty();
                } else if (argument.equals("--game-name")) {
                    String name = stackToString(stringList);
                    settings.put("KEY_DISPLAY_NAME", name);
                    settings.put("KEY_TITLE", name);
                } else if (argument.equals("--description")) {
                    settings.put("KEY_DESCRIPTION", stackToString(stringList));
                } else if (argument.equals("--publisher")) {
                    settings.put("KEY_PUBLISHER", stackToString(stringList));
                } else if (argument.equals("--developer")) {
                    settings.put("KEY_DEVELOPER", stackToString(stringList));
                } else if (argument.equals("--icon-file")) {
                    settings.put("IconFile", new File(stringList.pop()));
                    stringList.empty();
                } else if (argument.equals("--store-type")) {
                    settings.put("KEY_STORE_TYPE", stackToString(stringList));
                } else if (argument.equals("--analog-mode")) {
                    String str = stringList.pop();
                    if (str.equals("true"))
                        settings.put("KEY_ANALOG_MODE", "YES");
                    stringList.empty();
                } else {
                    stringList.push(argument);
                }
            } else {
                stringList.push(argument);
            }
        }

        PSXperiaTool tool = new PSXperiaTool(settings, inputFile, dataDir, outputDir);
        tool.startBuild();

        System.exit(0);
    }

    private static String stackToString(Stack<String> stack) {
        String str = "";
        while (!stack.isEmpty()) {
            str += stack.pop() + " ";
        }
        str = str.replaceAll("^\"", "");
        str = str.replaceAll("\"$", "");
        return str;
    }

    public static void doDecompressImage(String[] args) throws InvalidArgumentException, IOException, DataFormatException {
        if (args.length < 3)
            throw new InvalidArgumentException("Not enough input.");

        Stack<String> stack = new Stack<String>();
        stack.addAll(Arrays.asList(args));
        File outputFile = new File(stack.pop());
        File inputFile = new File(stack.pop());

        while (!stack.empty()) {
            String argument = stack.pop();
            if (argument.startsWith("-")) {
                if (argument.equals("-v") || argument.equalsIgnoreCase("--verbose")) {
                    Logger.setLevel(Logger.ALL);
                    continue;
                }
                Logger.warning("Unknown option %s", argument);
            }
        }
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        PSImageExtract extract = new PSImageExtract(in);
        extract.uncompress(out);
        out.close();
        in.close();
        System.exit(0);
    }

    public static void printHelp() {
        System.out.println("PSXPeria Converter Tool");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  Extract and patch data files");
        System.out.println("  psxperia e[x]tract [-v|--verbose] input.apk input-data.zpak output");
        System.out.println("    [-v|--verbose]    Verbose output");
        System.out.println("    input.apk         Either com.sony.playstation.ncua94900_1.apk or com.sony.playstation.ncea00344_1.apk");
        System.out.println("    input-data.zpak   Either NCUA94900_1_1.zpak or NCEA00344_1_1.zpak (must match region of APK)");
        System.out.println("    output            Directory to extract the files");
        System.out.println("");
        System.out.println("  Convert ISO to Xperia Play APK and ZPAK");
        System.out.println("  psxperia [c]onvert [OPTIONS] titleId image.iso output");
        System.out.println("    titleId           An unique ID, usually from the game in the format NCXAXXXXX_1");
        System.out.println("    image.iso         Input PSX image. You must rip it on your own!");
        System.out.println("    output            Directory to output files");
        System.out.println("    Options (unset options will be set to defaults):");
        System.out.println("      -v|--verbose    Verbose output, including image creation progress");
        System.out.println("      -D directory    Custom location for extracted data files, default is \"./data\"");
        System.out.println("      --load-xml      Load options from Java properties XML");
        System.out.println("      --game-name     Name of the game");
        System.out.println("      --description   Description of the game");
        System.out.println("      --publisher     Publisher of the game");
        System.out.println("      --developer     Developer of the game");
        System.out.println("      --icon-file     Path to image for icon");
        System.out.println("      --store-type    Where to find this title (any string will do)");
        System.out.println("      --analog-mode   true|false, Turn on/off analog controls (game must support it).");
        System.out.println("");
        System.out.println("  Convert image.ps to ISO");
        System.out.println("  psxperia [d]ecompress [-v|--verbose] input.ps output.iso");
        System.out.println("    [-v|--verbose]    Verbose output");
        System.out.println("    input.ps          image.ps from ZPAK");
        System.out.println("    output.iso        ISO file to generate");
        System.exit(0);
    }
}
