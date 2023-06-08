package com.azpt.binpacker.packing;



import com.azpt.binpacker.packing.domain.LoadedContainer;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.utils.Dims;

import java.util.List;

public interface ContainerPackingManager {
  LoadedContainer getFinalPaletteComposition(List<Bin> binList, Dims truckDimensions);
}
