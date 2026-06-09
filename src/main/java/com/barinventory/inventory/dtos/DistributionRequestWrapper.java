package com.barinventory.inventory.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DistributionRequestWrapper {

	private List<DistributionRequest> requests= new ArrayList<>();
}