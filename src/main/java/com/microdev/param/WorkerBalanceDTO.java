package com.microdev.param;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小时工款项信息
 */
@Data
public class WorkerBalanceDTO {
    /**
     * 已结款总额
     */
    private BigDecimal settledAmount;
    /**
     * 未结款总额
     */
    private BigDecimal unsettledAmount;
    /**
     * 已结款项明细
     */
    private List<WorkerBalanceDetailDTO> settledDetails;
    /**
     * 未结款项明细
     */
    private List<WorkerBalanceDetailDTO> unsettledDetails;
}
