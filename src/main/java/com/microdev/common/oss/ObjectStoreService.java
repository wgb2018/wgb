package com.microdev.common.oss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author liutf
 */
public interface ObjectStoreService {
    /**
     * 上传对象
     *
     * @param key   对象名称
     * @param bytes btye数组
     */
    String uploadObject(String key, byte[] bytes);

    /**
     * 上传文件
     *
     * @param key  文件名
     * @param file 文件
     */
    String uploadFile(String key, File file);


    /**
     * 下载对象
     *
     * @param key 对象
     * @return byte数组
     */
    byte[] downloadObject(String key) throws IOException;

    /**
     * 下载文件
     *
     * @param key 文件名称
     * @return 文件
     */
    File downloadFile(String key) throws IOException;

    /**
     * 下载文件到指定文件
     *
     * @param key  文件名
     * @param file 指定文件
     * @return
     */
    File downloadFile(String key, File file);

    /**
     * 下载文件流
     *
     * @param key
     * @return
     */
    InputStream downloadFileStream(String key);
}
