package com.azpt.binpacker.packing.domain;

import lombok.experimental.Accessors;

import java.util.List;

@Accessors
public class LoadedContainer {
  List<BinContainerPosition> bins;
}
