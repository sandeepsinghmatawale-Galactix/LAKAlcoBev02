package com.barinventory.dtos;

import lombok.Data;

@Data
public class StockroomClosingRequest {

   
    private Integer closingStock;
    private Long brandSizeId;
}