package com.crobot.core.util;

import com.crobot.runtime.engine.ContextException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static void createNewFile(File file) {
        try {
            if (file.exists()) {
                return;
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readFileByte(File file) {
        try (FileInputStream fis = new FileInputStream(file);) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = fis.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                content.append(currentLine).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private static void renameTo(File source, File target) {
        source.renameTo(target);
    }

    private static boolean writeFileByte2(File file, byte[] content) {
        FileUtil.createNewFile(file);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static boolean writeFile2(File file, String content) {
        FileUtil.createNewFile(file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFileByte(File file, byte[] content) {
        File tmp = new File(file.getAbsolutePath() + "_tmp");
        if (writeFileByte2(tmp, content)) {
            renameTo(tmp, file);
            return true;
        }
        return false;
    }


    public static boolean writeFile(File file, String content) {
        File tmp = new File(file.getAbsolutePath() + "_tmp");
        if (writeFile2(tmp, content)) {
            renameTo(tmp, file);
            return true;
        }
        return false;
    }

    private static boolean writeFileStream2(File file, InputStream content, long totalBytes, ProgressCallback callback) {
        final int BUFFER_SIZE = 8192;
        long currentBytes = 0;
        FileUtil.createNewFile(file);
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = content) { // 包装输入流，确保自动关闭
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                currentBytes += bytesRead;
                int progress = totalBytes > 0 ? (int) ((((double) currentBytes) / totalBytes) * 100) : -1;
                if (callback != null) {
                    callback.onProgress(currentBytes, totalBytes, progress);
                }
            }
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFileStream(File file, long total, InputStream content, ProgressCallback progressCallback) {
        File tmp = new File(file.getAbsolutePath() + "_tmp");
        if (writeFileStream2(tmp, content, total, progressCallback)) {
            renameTo(tmp, file);
            return true;
        }
        return false;
    }

    public static boolean writeFileStream(File file, InputStream content) {
        final int BUFFER_SIZE = 8192;
        FileUtil.createNewFile(file);
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = content) { // 包装输入流，确保自动关闭
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFileStream(File file, InputStream content, int length) {
        final int BUFFER_SIZE = 8192;
        FileUtil.createNewFile(file);
        try (OutputStream outputStream = new FileOutputStream(file);) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesWritten = 0; // 记录已写入的总字节数
            // 核心逻辑：仅读取指定length个字节
            while (totalBytesWritten < length && (bytesRead = content.read(buffer, 0, Math.min(BUFFER_SIZE, (int) (length - totalBytesWritten)))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesWritten += bytesRead; // 累加已写入字节数
            }
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void checkFile(String path, String name) {
        if (!new File(path).exists()) {
            throw new ContextException(String.format("The file %s cannot be located", name));
        }
    }

    public interface ProgressCallback {
        void onProgress(long currentBytes, long totalBytes, int progress);
    }

}
