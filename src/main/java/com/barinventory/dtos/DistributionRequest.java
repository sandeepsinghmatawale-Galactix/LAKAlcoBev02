package com.barinventory.dtos;

import lombok.Data;

@Data
public class DistributionRequest {

    private Long wellId;
    
    private Integer distributedQty;
    private Long brandSizeId;
}