package com.yifanlu.PSXperiaTool.Extractor;

import com.sun.corba.se.impl.copyobject.FallbackObjectCopierImpl;
import com.yifanlu.PSXperiaTool.Logger;
import com.yifanlu.PSXperiaTool.ZpakCreate;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.Buffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: yifanlu
 * Date: 8/9/11
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrashBandicootExtractor {
    public static long[] KNOWN_VALID_APK_CRC32 = {
            0xE7BCB6D5, 0xBB542581
    };
    private File mApkFile;
    private File mOutputDir;
    private static final int BLOCK_SIZE = 1024;

    public CrashBandicootExtractor(File apk, File outputDir){
        this.mApkFile = apk;
        this.mOutputDir = outputDir;
    }

    public void extractApk() throws IOException {
        verifyApk();
        rawExtract();
    }

    private void verifyApk() throws IOException {
        if(!mApkFile.exists())
            throw new FileNotFoundException("Cannot find APK file: " + mApkFile.getPath());
        long crc32 = ZpakCreate.getCRC32(mApkFile);
        boolean valid = false;
        for(long check : KNOWN_VALID_APK_CRC32)
            if(check == crc32)
                valid = true;
        if(!valid){
            Logger.warning("This APK is not a known valid one. The extractor will continue, but you may get errors later on.");
        }
        if(!mOutputDir.exists())
            mOutputDir.mkdirs();
    }

    private void rawExtract() throws IOException {
        Logger.info("Extracting APK");
        ZipInputStream zip = new ZipInputStream(new FileInputStream(mApkFile));
        ZipEntry entry;
        while((entry = zip.getNextEntry()) != null){
            Logger.verbose("Unzipping %s", entry.getName());
            File file = new File(mOutputDir, entry.getName());
            if(file.isDirectory())
                continue;
            FileUtils.touch(file);
            FileOutputStream out = new FileOutputStream(file.getPath());
            int n;
            byte[] buffer = new byte[BLOCK_SIZE];
            while((n = zip.read(buffer)) != -1){
                out.write(buffer, 0, n);
            }
            out.close();
            zip.closeEntry();
        }
        zip.close();
    }
}
