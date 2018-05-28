package com.microdev.param.api.request;

import com.microdev.type.UserSex;
import lombok.Data;

@Data
public class UpdateGenderRequest {
    private UserSex gender;
}
