package com.microdev.model;

import com.microdev.type.UserType;
import lombok.Data;

@Data
public class FreeBack extends BaseEntity{
    private String userId;

    private String content;

    private UserType userType;
}
