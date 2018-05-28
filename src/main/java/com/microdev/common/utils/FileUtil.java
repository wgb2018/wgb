package com.microdev.common.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
}
