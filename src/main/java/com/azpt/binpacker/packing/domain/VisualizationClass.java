package com.azpt.binpacker.packing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class VisualizationClass {
  private int id;
  private int outOrder;
  private int x;
  private int y;
  private int z;
  private int dx;
  private int dy;
  private int dz;
}
