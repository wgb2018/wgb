package com.microdev.param;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Data
public class FileRequest {
    private String fileType;
    private MultipartFile file;
}
