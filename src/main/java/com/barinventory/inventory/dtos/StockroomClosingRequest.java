package com.barinventory.inventory.dtos;

import lombok.Data;

@Data
public class StockroomClosingRequest {

   
    private Integer closingStock;
    private Long brandSizeId;
}