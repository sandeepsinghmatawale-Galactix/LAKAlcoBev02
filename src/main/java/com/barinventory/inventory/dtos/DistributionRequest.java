package com.barinventory.inventory.dtos;

import lombok.Data;

@Data
public class DistributionRequest {

    private Long wellId;
    private Long brandId;   
    
    private Integer distributedQty;
    private Long brandSizeId;
}