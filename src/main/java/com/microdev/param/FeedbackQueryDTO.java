package com.microdev.param;

import com.microdev.type.UserType;
import lombok.Data;

@Data
public class FeedbackQueryDTO {
    private String name;
    private String userType;
}
