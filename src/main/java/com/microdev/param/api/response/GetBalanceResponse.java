package com.microdev.param.api.response;

import lombok.Data;

@Data
public class GetBalanceResponse {
    private Double settledAmount = 0.00;
    private Double unsettledAmount = 0.00;
}
