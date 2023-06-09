package com.azpt.binpacker.packing;



import com.azpt.binpacker.packing.domain.LoadedContainer;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.domain.Order;
import com.azpt.binpacker.packing.utils.Dims;

import java.util.List;

public interface ContainerPackingManager {
  LoadedContainer getFinalBinComposition(Order order, Dims truckDimensions);
}
