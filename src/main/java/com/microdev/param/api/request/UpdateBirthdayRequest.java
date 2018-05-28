package com.microdev.param.api.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UpdateBirthdayRequest {
    private OffsetDateTime birthday;
}
