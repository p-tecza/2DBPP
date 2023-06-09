package com.azpt.binpacker.packing.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Order {
    private List<Bin> singleBins;
    private List<BinPacket> packets;
}
