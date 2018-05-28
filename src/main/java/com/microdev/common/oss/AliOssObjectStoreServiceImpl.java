package com.microdev.common.oss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author liutf
 */
@Service
public class AliOssObjectStoreServiceImpl implements ObjectStoreService {

    private String platform = "aliyunOSS";
    private OSSClient ossClient;
    private String bucket;
    private String domain;
    private String endpoint;

    private ObjectStoreConfig objectStoreConfig;

    @Autowired
    public AliOssObjectStoreServiceImpl(ObjectStoreConfig objectStoreConfig) {
        this.objectStoreConfig = objectStoreConfig;
        Map<String, String> settings = objectStoreConfig.getSettings();
        this.endpoint = settings.get("endpoint");
        this.domain = settings.get("domain");
        this.bucket = settings.get("bucket");
        this.ossClient = new OSSClient(endpoint, settings.get("accessKeyId"), settings.get("accessKeySecret"));

        if (this.domain == null || "changeme".equals(this.domain)) {
            this.domain = "http://" + this.bucket + "." + this.endpoint + "/";
        } else {
            if (!this.domain.endsWith("/")) {
                this.domain = this.domain + "/";
            }
        }
    }

    /**
     * 上传对象
     *
     * @param key   对象名称
     * @param bytes btye数组
     * @return 文件下载地址
     */
    @Override
    public String uploadObject(String key, byte[] bytes) {
        ossClient.putObject(bucket, key, new ByteArrayInputStream(bytes));
        return domain + key;
    }

    /**
     * 上传文件
     *
     * @param key  文件名
     * @param file 文件
     * @return 文件下载地址
     */
    @Override
    public String uploadFile(String key, File file) {
        ossClient.putObject(bucket, key, file);
        return domain + key;
    }


    /**
     * 下载对象
     *
     * @param key 对象
     * @return byte数组
     */
    @Override
    public byte[] downloadObject(String key) throws IOException {
        File file = File.createTempFile(platform, key);
        ossClient.getObject(new GetObjectRequest(bucket, key), file);
        return FileUtils.readFileToByteArray(file);
    }

    /**
     * 下载文件
     *
     * @param key 文件名称
     * @return 文件
     */
    @Override
    public File downloadFile(String key) throws IOException {
        File file = File.createTempFile(key, platform);
        ossClient.getObject(new GetObjectRequest(bucket, key), file);
        return file;
    }

    /**
     * 下载文件到指定文件
     *
     * @param key  文件名
     * @param file 指定文件
     * @return
     */
    @Override
    public File downloadFile(String key, File file) {
        ossClient.getObject(new GetObjectRequest(bucket, key), file);
        return file;
    }

    /**
     * 下载文件流
     *
     * @param key
     * @return
     */
    @Override
    public InputStream downloadFileStream(String key) {
        OSSObject ossObject = ossClient.getObject(bucket, key);
        return ossObject.getObjectContent();
    }


    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public OSSClient getOssClient() {
        return ossClient;
    }

    public void setOssClient(OSSClient ossClient) {
        this.ossClient = ossClient;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
