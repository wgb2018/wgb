package com.microdev.common.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author liutf
 */
public class FileUtil {
    /**
     * 返回文件 sha1 的文件名
     */
    public static String fileNameReplaceSHA1(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();//上传的文件名
        String fileSuffix = FilenameUtils.getExtension(originalFilename).toLowerCase();//文件后缀
        String fileSha1 = FileSafeCode.getSha1(file.getInputStream());//文件的 sha1
        //拼接 sha1 和文件后缀
        String fileName = fileSha1 + (fileSuffix.length() > 0 ? ("." + fileSuffix) : "");
        return fileName;
    }

    /**
     * 返回文件 sha1 的文件名
     */
    public static String fileNameReplaceSHA1(File file) throws Exception {
        String originalFilename = file.getName();//上传的文件名
        String fileSuffix = FilenameUtils.getExtension(originalFilename).toLowerCase();//文件后缀
        String fileSha1 = FileSafeCode.getSha1(file);//文件的 sha1

        //拼接 sha1 和文件后缀
        String fileName = fileSha1 + (fileSuffix.length() > 0 ? ("." + fileSuffix) : "");
        return fileName;
    }
    /**
     * B方法追加文件：使用FileWriter
     */
    public static void appendFile(String fileName, String content) {
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 复制文件
     * @param fromFile
     * @param toFile
     * <br/>
     * 2016年12月19日  下午3:31:50
     * @throws IOException
     */
    public void copyFile(File fromFile,File toFile) throws IOException{
        FileInputStream ins = new FileInputStream(fromFile);
        FileOutputStream out = new FileOutputStream(toFile);
        byte[] b = new byte[1024];
        int n=0;
        while((n=ins.read(b))!=-1){
            out.write(b, 0, n);
        }

        ins.close();
        out.close();
    }
}
