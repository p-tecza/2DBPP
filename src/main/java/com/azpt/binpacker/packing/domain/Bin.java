package com.azpt.binpacker.packing.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.azpt.binpacker.packing.utils.Dims;

@Builder
@Getter
@Setter
public class Bin {
  private Dims binDimensions;
  private boolean stackable;
  private boolean rotatable;
  private int outOrder;
  private int identifier;

  public int getSize(){
    return this.binDimensions.getWidth()*this.binDimensions.getDepth();
  }
}
