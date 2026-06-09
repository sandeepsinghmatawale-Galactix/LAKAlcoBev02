package com.barinventory.inventory.dtos;

import lombok.Data;

@Data
public class WellClosingRequest {

    private Long wellId;
    private Long brandSizeId;
    private Integer closingStock;
}