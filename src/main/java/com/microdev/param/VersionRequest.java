package com.microdev.param;

import lombok.Data;

@Data
public class VersionRequest {
    private String id;

    private String versionCode;

    private String content;

    private String isUpdate;

    private String type;

    private String address;
}
