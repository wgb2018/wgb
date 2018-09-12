package com.microdev.param;

import lombok.Data;

@Data
public class HotelCancelParam {
    private String workerId;

    private String messageId;

    private Integer status;
}
