package com.microdev.common;

import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.utils.FileSafeCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class FilePush {

    @Autowired
    private ObjectStoreService objectStoreService;

    public String pushFileToServer(String catalog, String localPath) throws Exception {
        InputStream fis = new FileInputStream(new File(localPath));
        catalog = catalog + FileSafeCode.getSha1(fis) + ".html";
        fis = new FileInputStream(new File(localPath));
        byte[] b = new byte[fis.available()];
        fis.read(b);
        return objectStoreService.uploadObject(catalog, b);
    }
}
