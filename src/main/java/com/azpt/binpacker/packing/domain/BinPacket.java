package com.azpt.binpacker.packing.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class BinPacket {
    private List<Bin> bins;
    private int outOrder;
}
