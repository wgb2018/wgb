package com.microdev.param;

import lombok.Data;

@Data
public class BindPcResponse {

    private String messageId;
    private String messageType;
    private String hrName;
    private String hrLogo;
    private String address;
    private String username;
    private int age;
    private String sex;
    private String mobile;

}
