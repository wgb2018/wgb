package com.microdev.param;

import com.microdev.type.PlatformType;
import lombok.Data;

/**
 * @author liutf
 */
@Data
public class ChangePwdRequest {
    private String userId;
    /**
     *
     */
    private String oldPwd;
    /**
     *
     */
    private String newPwd;

    private PlatformType platform;
}
