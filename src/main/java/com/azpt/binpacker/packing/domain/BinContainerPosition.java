package com.azpt.binpacker.packing.domain;

import lombok.experimental.Accessors;

@Accessors
public class BinContainerPosition {
  private Bin bin;
  private int x;
  private int y;
  private int layer;
}
