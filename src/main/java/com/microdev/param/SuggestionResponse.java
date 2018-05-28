package com.microdev.param;

import com.microdev.type.UserType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SuggestionResponse {
    private String userName;
    private String sex;
    private UserType userType;
    private String suggestionContent;
    private OffsetDateTime createTime;
}
