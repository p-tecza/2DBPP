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

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("SINGLE BINS:\n");
        for (Bin b : singleBins) {
            returnString.append(b.toString());
            returnString.append("\n");
        }
        returnString.append("BIN PACKETS:\n");
        int it = 0;
        for (BinPacket bp : packets) {
            returnString.append("NR ");
            returnString.append(it++);
            returnString.append("[\n");
            for (Bin b : bp.getBins()) {
                returnString.append(b.toString());
                returnString.append("\n");
            }
            returnString.append("]\n");
        }
        return returnString.toString();
    }
}
