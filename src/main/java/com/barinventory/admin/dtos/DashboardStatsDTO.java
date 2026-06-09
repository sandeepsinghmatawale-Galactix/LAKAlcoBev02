package com.barinventory.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  
@AllArgsConstructor
public class DashboardStatsDTO {
	private long totalBars;
    private long totalBrands;
    private long activeSubscriptions;

}
