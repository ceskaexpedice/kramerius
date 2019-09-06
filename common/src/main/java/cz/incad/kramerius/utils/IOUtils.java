package cz.incad.kramerius.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.logging.Level;

public class IOUtils {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(IOUtils.class.getName());

    private IOUtils() {
    }

    /**
     * Kopirovani ze vstupniho proud do vystupniho
     * 
     * @param is
     *            Vstupni proud
     * @param os
     *            Vystupni proud
     * @throws IOException
     */
    public static void copyStreams(InputStream is, OutputStream os)
            throws IOException {
        org.apache.commons.io.IOUtils.copy(is, os);
    }

    /**
     * Kopirovani ze vstupniho proud do vystupniho
     * 
     * @param is
     *            Vstupni proud
     * @param closeInput
     *            Zavrit vstupni proud
     * @param os
     *            Vystupni proud
     * @param closeOutput
     *            Zavrit vystupni proud
     * @throws IOException
     */
    public static void copyStreams(InputStream is, boolean closeInput,
            OutputStream os, boolean closeOutput) throws IOException {
        byte[] buffer = new byte[8192];
        int read = -1;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        if (closeInput)
            is.close();
        if (closeOutput)
            os.close();
    }

    /**
     * Kopiruje a pocita digest
     * 
     * @param is
     *            Vstupni stream
     * @param os
     *            Vystupni stream
     * @param digest
     *            Digest
     * @throws IOException
     */
    public static void copyStreams(InputStream is, OutputStream os,
            MessageDigest digest) throws IOException {
        byte[] buffer = new byte[8192];
        int read = -1;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
            digest.update(buffer, 0, read);
        }
    }

    public static String readAsString(InputStream is, Charset charset,
            boolean closeInput) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyStreams(is, bos);
            return new String(bos.toByteArray(), charset);
        } finally {
            if ((is != null) && closeInput) {
                is.close();
            }
        }
    }

    
    public static void saveToFile(String data, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
        } finally {
            if (fos != null)
                fos.close();
        }
    }
    public static void saveToFile(InputStream data, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            copyStreams(data, fos);
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    public static void saveToFile(InputStream data, File file, boolean closeInput) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            copyStreams(data, fos);
        } finally {
            if (fos != null)
                tryClose(fos);
            if (closeInput) {
                tryClose(data);
            }
        }
    }

    public static void saveToFile(byte[] data, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    public static byte[] bos(File inFile) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(inFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyStreams(is, bos);
            return bos.toByteArray();
        } finally {
            if (is != null)
                is.close();
        }

    }
    public static byte[] bos(InputStream is, boolean closeInput) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyStreams(is, bos);
            return bos.toByteArray();
        } finally {
            if (is != null) {
                if (closeInput) {
                    is.close();
                }
            }
        }

    }

    public static File checkDirectory(String name) {
        File directory = new File(name);
        if (!directory.exists() || !directory.isDirectory()) {
            if (!directory.mkdirs()) {
                LOGGER.severe("Folder doesn't exist and can't be created: "
                        + directory.getAbsolutePath());
                throw new RuntimeException(
                        "Folder doesn't exist and can't be created: "
                                + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    public static void cleanDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    cleanDirectory(files[i]);
                }
                files[i].delete();
            }
        }
    }

    public static void tryClose(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void tryClose(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    
    public static void tryClose(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void tryClose(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void copyBundledResources(Class caller, String[] texts,
            String prefix, File folder) throws FileNotFoundException,
            IOException {
        for (String def : texts) {
            InputStream is = null;
            FileOutputStream os = null;
            try {
                File file = new File(folder, def);
                if (!file.exists()) {
                    LOGGER.fine("destination file " + file);
                    String res = prefix + def;
                    is = caller.getResourceAsStream(res);
                    if (is == null)
                        throw new IOException("cannot find resource " + res);
                    os = new FileOutputStream(file);
                    copyStreams(is, os);
                }
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static void copyFiles(File source, File dest) throws IOException {
        FileChannel fichannel = new FileInputStream(source).getChannel();
        FileChannel foChannel = new FileOutputStream(dest).getChannel();

        long size = fichannel.size();
        fichannel.transferTo(0, size, foChannel);
    }

    public static void copyFolders(File sourceFolder, File destFolder)
            throws IOException {
        if (!destFolder.exists())
            destFolder.mkdirs();
        File[] listFiles = sourceFolder.listFiles();
        if (listFiles != null) {
            for (File srcFile : listFiles) {
                if (srcFile.isDirectory())
                    copyFolders(srcFile,
                            new File(destFolder, srcFile.getName()));
                if (srcFile.isFile()) {
                    File destFile = new File(destFolder, srcFile.getName());
                    destFile.createNewFile();
                    copyFiles(srcFile, destFile);
                }
            }
        }
    }
}
