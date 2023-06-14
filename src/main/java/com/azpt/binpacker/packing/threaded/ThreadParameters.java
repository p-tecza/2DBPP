package com.azpt.binpacker.packing.threaded;

import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.utils.Dims;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ThreadParameters {
    int startInclusive;
    int stopExclusive;
    List<Bin> binList;
    Dims outDims;
}
