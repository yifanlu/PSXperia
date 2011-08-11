/*
 * PSXperia Converter Tool - Main backend
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

import com.android.sdklib.internal.build.SignedJarBuilder;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class PSXperiaTool extends ProgressMonitor {
    public static final String[] FILES_TO_MODIFY = {
            //"/AndroidManifest.xml",
            "/assets/AndroidManifest.xml",
            "/assets/ZPAK/metadata.xml",
            "/res/values/strings.xml",
            "/ZPAK/metadata.xml"
    };

    private File mInputFile;
    private File mDataDir;
    private File mTempDir = null;
    private File mOutputDir;
    private Properties mProperties;
    private static final int TOTAL_STEPS = 8;

    public PSXperiaTool(Properties properties, File inputFile, File dataDir, File outputDir) {
        mInputFile = inputFile;
        mDataDir = dataDir;
        mProperties = properties;
        mOutputDir = outputDir;
        Logger.info("PSXperiaTool initialized, outputting to: %s", mOutputDir.getPath());
        setTotalSteps(TOTAL_STEPS);
    }

    public void startBuild() throws IOException, InterruptedException, GeneralSecurityException, SignedJarBuilder.IZipEntryFilter.ZipAbortException {
        Logger.info("Starting build.");
        checkData(mDataDir);
        mTempDir = createTempDir(mDataDir);
        copyIconImage((File) mProperties.get("IconFile"));
        //BuildResources br = new BuildResources(mProperties, mTempDir);
        replaceStrings();
        generateImage();
        generateDefaultZpak();
        //buildResources(br);
        generateOutput();
        nextStep("Deleting temporary directory");
        FileUtils.deleteDirectory(mTempDir);
        nextStep("Done.");
    }

    private File createTempDir(File dataDir) throws IOException {
        nextStep("Creating temporary directory.");
        File tempDir = new File(new File("."), "/.psxperia." + (int) (Math.random() * 1000));
        if (tempDir.exists())
            FileUtils.deleteDirectory(tempDir);
        if (!tempDir.mkdirs())
            throw new IOException("Cannot create temporary directory!");
        FileUtils.copyDirectory(dataDir, tempDir);
        Logger.debug("Created temporary directory at, %s", tempDir.getPath());
        return tempDir;
    }

    private void checkData(File dataDir) throws IllegalArgumentException, IOException {
        nextStep("Checking to make sure all files are there.");
        if(!mDataDir.exists())
            throw new FileNotFoundException("Cannot find data directory!");
        InputStream fstream = PSXperiaTool.class.getResourceAsStream("/resources/filelist.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fstream));
        String line;
        while((line = reader.readLine()) != null){
            if(line.isEmpty())
                continue;
            File check = new File(mDataDir, line);
            if(!check.exists())
                throw new IllegalArgumentException("Cannot find required data file: " + line);
        }
        Logger.debug("Done checking data.");
    }

    private void copyIconImage(File image) throws IllegalArgumentException, IOException {
        nextStep("Copying icon if needed.");
        if (image == null || !(image instanceof File)){
            Logger.verbose("Icon copying not needed.");
            return;
        }
        if (!image.exists())
            throw new IllegalArgumentException("Icon file not found.");
        FileUtils.copyFile(image, new File(mTempDir, "/res/drawable/icon.png"));
        FileUtils.copyFile(image, new File(mTempDir, "/assets/ZPAK/assets/default/bitmaps/icon.png"));
        Logger.debug("Done copying icon from %s", image.getPath());
    }

    public void replaceStrings() throws IOException {
        nextStep("Replacing strings.");
        Map<String, String> replacement = new TreeMap<String, String>();
        Iterator<Object> it = mProperties.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = mProperties.getProperty(key);
            if (!(value instanceof String))
                continue;
            if (!(key.startsWith("KEY_")))
                continue;
            String find = ((String) key).substring("KEY_".length());
            Logger.verbose("Found replacement key: %s, replacing with: %s", find, value);
            replacement.put("\\{" + find + "\\}", (String) value);
            replacement.put("\\{FILTERED_" + find + "\\}", StringReplacement.filter((String) value));
        }
        StringReplacement strReplace = new StringReplacement(replacement, mTempDir);
        strReplace.execute(FILES_TO_MODIFY);
        Logger.debug("Done replacing strings.");
    }

    private void generateImage() throws IOException {
        nextStep("Generating PSImage.");
        FileInputStream in = new FileInputStream(mInputFile);
        FileOutputStream out = new FileOutputStream(new File(mTempDir, "/ZPAK/data/image.ps"));
        FileOutputStream tocOut = new FileOutputStream(new File(mTempDir, "/image_ps_toc.bin"));
        PSImageCreate ps = new PSImageCreate(in);
        PSImage.ProgressCallback progress = new PSImage.ProgressCallback() {
            int mBytesRead = 0, mBytesWritten = 0;

            public void bytesReadChanged(int delta) {
                mBytesRead += delta;
                jump(mBytesRead);
                Logger.verbose("Image bytes read: %d", mBytesRead);
            }

            public void bytesWrittenChanged(int delta) {
                mBytesWritten += delta;
                Logger.verbose("Compressed PSImage bytes written: %d", mBytesWritten);
            }
        };
        // progress management
        int oldSteps = getSteps();
        setTotalSteps((int)in.getChannel().size());
        jump(0);

        ps.setCallback(progress);
        ps.compress(out);
        ps.writeTocTable(tocOut);
        out.close();
        tocOut.close();
        in.close();

        setTotalSteps(TOTAL_STEPS);
        jump(oldSteps);

        Logger.debug("Done generating PSImage");

        Logger.info("Generating ZPAK.");
        File zpakDirectory = new File(mTempDir, "/ZPAK");
        File zpakFile = new File(mTempDir, "/" + mProperties.getProperty("KEY_TITLE_ID") + ".zpak");
        FileOutputStream zpakOut = new FileOutputStream(zpakFile);
        ZpakCreate zcreate = new ZpakCreate(zpakOut, zpakDirectory);
        zcreate.create(true);
        FileUtils.deleteDirectory(zpakDirectory);
        Logger.debug("Done generating ZPAK at %s", zpakFile.getPath());
    }

    private void generateDefaultZpak() throws IOException {
        nextStep("Generating default ZPAK.");
        File defaultZpakDirectory = new File(mTempDir, "/assets/ZPAK");
        File zpakFile = new File(mTempDir, "/assets/" + mProperties.getProperty("KEY_TITLE_ID") + ".zpak");
        FileOutputStream zpakOut = new FileOutputStream(zpakFile);
        ZpakCreate zcreate = new ZpakCreate(zpakOut, defaultZpakDirectory);
        zcreate.create(false);
        zpakOut.close();
        FileUtils.deleteDirectory(defaultZpakDirectory);
        Logger.debug("Done generating default ZPAK at %s", zpakFile.getPath());
    }

    private void generateOutput() throws IOException, InterruptedException, GeneralSecurityException, SignedJarBuilder.IZipEntryFilter.ZipAbortException {
        nextStep("Done processing, generating output.");
        String titleId = mProperties.getProperty("KEY_TITLE_ID");
        if (!mOutputDir.exists())
            mOutputDir.mkdir();
        File outDataDir = new File(mOutputDir, "/data/com.sony.playstation." + titleId + "/files/content");
        if (!outDataDir.exists())
            outDataDir.mkdirs();
        Logger.debug("Moving files around.");
        FileUtils.cleanDirectory(outDataDir);
        FileUtils.moveFileToDirectory(new File(mTempDir, "/" + titleId + ".zpak"), outDataDir, false);
        FileUtils.moveFileToDirectory(new File(mTempDir, "/image_ps_toc.bin"), outDataDir, false);
        File outApk = new File(mOutputDir, "/com.sony.playstation." + titleId + ".apk");

        ApkBuilder build = new ApkBuilder(mTempDir, outApk);
        build.buildApk();

        Logger.info("Done.");
        Logger.info("APK file: %s", outApk.getPath());
        Logger.info("Data dir: %s", outDataDir.getPath());

    }


}
