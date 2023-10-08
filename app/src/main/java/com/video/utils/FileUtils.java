package com.video.utils;


import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件处理工具类
 *
 * @author liuguofeng
 */
public class FileUtils {

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                return deleteSingleFile(filePath);
            } else {
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 删除指定文件
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }


    /**
     * 复制文件夹
     *
     * @param source 源文件
     * @param dest   目标文件
     * @throws IOException io异常
     */
    public static void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = Files.newInputStream(source.toPath());
            os = Files.newOutputStream(dest.toPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }


    /**
     * 获取文件夹下的所有文件
     *
     * @param directoryPath 文件夹
     * @return
     */
    public static List<File> listFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<File> files = new ArrayList<>();
        // Get all files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(listFiles(file.getAbsolutePath()));
            }
        }
        return files;
    }

    /**
     * 获取文件夹下的所有文件
     *
     * @param directoryPath 文件夹
     * @return
     */
    public static List<String> listFileStr(String directoryPath) {
        File directory = new File(directoryPath);
        List<String> files = new ArrayList<>();
        // Get all files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                files.addAll(listFileStr(file.getAbsolutePath()));
            }
        }
        return files;
    }

    /**
     * 保存图片
     *
     * @param bitmap   图片
     * @param filePath 存放地址
     */
    public static void saveImage(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes, String filePath) {
        File file = new File(filePath);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


    /**
     * 保存文本文件
     *
     * @param path 保存路径
     * @param txt  文本内容
     */
    public static void saveTxt(String path, String txt) throws IOException {
        FileWriter fw = new FileWriter(path, false);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.append(txt);
        bw.flush();
        fw.flush();
        bw.close();
        fw.close();
    }
}
