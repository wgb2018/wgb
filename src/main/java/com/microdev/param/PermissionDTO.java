
package com.microdev.param;

import com.microdev.type.ActionType;
import lombok.Data;

@Data
public class PermissionDTO {
    private String id;
    private String name;
    private ActionType action;
    private String uri;
}