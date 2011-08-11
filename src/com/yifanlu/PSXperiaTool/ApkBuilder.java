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

import com.android.sdklib.internal.build.SignedJarBuilder;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class ApkBuilder {
    private static final String ALIAS = "signPSXperia";
    private static final char[] KEYSTORE_PASSWORD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
    private static final char[] ALIAS_PASSWORD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
    public static final String VERSION = "0.3 Beta 2";

    private File mInputDir;
    private File mOutputApk;

    public ApkBuilder(File inputDir, File outputApk){
        this.mInputDir = inputDir;
        this.mOutputApk = outputApk;
    }

    public void buildApk() throws IOException, InterruptedException, GeneralSecurityException, SignedJarBuilder.IZipEntryFilter.ZipAbortException {
        String os = System.getProperty("os.name");
        Logger.verbose("Your OS: %s", os);
        File aaptTool;
        if(os.equals("Mac OS X"))
            aaptTool = new File("./aapt-osx");
        else if(os.startsWith("Windows"))
            aaptTool = new File("./aapt-windows.exe");
        else if(os.equals("Linux"))
            aaptTool = new File("./aapt-linux");
        else {
            Logger.warning("Does not understand OS name '%s', assuming to be Linux", os);
            aaptTool = new File("./aapt-linux");
        }
        InputStream in = PSXperiaTool.class.getResourceAsStream("/resources/" + aaptTool.getName());
        Logger.verbose("Extracting %s", aaptTool.getPath());
        writeStreamToFile(in, aaptTool);
        in.close();
        aaptTool.setExecutable(true);

        File androidFrameworkJar = new File("./android-framework.jar");
        Logger.verbose("Extracting %s", androidFrameworkJar.getPath());
        in = PSXperiaTool.class.getResourceAsStream("/resources/android-framework.jar");
        writeStreamToFile(in, androidFrameworkJar);
        in.close();

        File tempApk = new File(mOutputApk.getPath() + ".unsigned");

        String[] cmd = new String[12];
        cmd[0] = (aaptTool.getPath());
        cmd[1] = ("package");
        cmd[2] = ("-f");
        cmd[3] = ("-F");
        cmd[4] = (tempApk.getPath());
        cmd[5] = ("-S");
        cmd[6] = ((new File(mInputDir, "/res")).getPath());
        cmd[7] = ("-M");
        cmd[8] = ((new File(mInputDir, "/assets/AndroidManifest.xml")).getPath());
        cmd[9] = ("-I");
        cmd[10] = (androidFrameworkJar.getPath());
        cmd[11] = (mInputDir.getPath());
        Logger.debug("Running command: " + Arrays.toString(cmd).replaceAll("\\,", ""));
        runCmdWithOutput(cmd);

        Logger.info("Signing apk %s to %s", tempApk.getPath(), mOutputApk.getPath());
        signApk(tempApk);

        Logger.verbose("Cleaning up signing stuff.");
        tempApk.delete();
        androidFrameworkJar.delete();
        aaptTool.delete();
    }

    private void writeStreamToFile(InputStream in, File outFile) throws IOException {
        Logger.verbose("Writing to: %s", outFile.getPath());
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int n;
        while((n = in.read(buffer)) != -1){
            out.write(buffer, 0, n);
        }
        out.close();
    }

    public static void runCmdWithOutput(String[] cmd) throws IOException, InterruptedException {
        Process ps = Runtime.getRuntime().exec(cmd);
        BufferedReader in = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
        String line;
        while ((line = in.readLine()) != null) {
            Logger.debug(line);
        }
        in.close();
        if (ps.waitFor() != 0) {
            throw new IOException("Executable did not return without error.");
        }
    }

    private void signApk(File unsignedApk) throws IOException, GeneralSecurityException, SignedJarBuilder.IZipEntryFilter.ZipAbortException {
        FileInputStream in = new FileInputStream(unsignedApk);
        FileOutputStream out = new FileOutputStream(mOutputApk);
        KeyStore ks = getKeyStore();
        PrivateKey key = (PrivateKey)ks.getKey(ALIAS, ALIAS_PASSWORD);
        X509Certificate cert = (X509Certificate)ks.getCertificate(ALIAS);
        SignedJarBuilder builder = new SignedJarBuilder(out, key, cert);
        builder.writeZip(in, null);
        builder.close();
        out.close();
        in.close();
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream is = PSXperiaTool.class.getResourceAsStream("/resources/signApk.keystore");
        ks.load(is, KEYSTORE_PASSWORD);
        return ks;
    }
}
