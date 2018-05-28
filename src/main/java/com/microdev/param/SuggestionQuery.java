package com.microdev.param;

import com.microdev.type.UserType;
import lombok.Data;

@Data
public class SuggestionQuery {
    private String userName;
    private UserType userType;
}
