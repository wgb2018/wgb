package com.microdev.common.context;

import lombok.Data;

/**
 * @author liutf
 */
@Data
public class CallId {
    private String id;
    private String parent;
    private String root;
}
