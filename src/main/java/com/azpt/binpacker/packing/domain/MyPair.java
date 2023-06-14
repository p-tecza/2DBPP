package com.azpt.binpacker.packing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MyPair{
    private Long bestLocalFit;
    private Integer bestLocalCombination;
    private List<VisualizationClass> binList;

    public MyPair(Long bestLocalFit, Integer bestLocalCombination, List<VisualizationClass> binList) {
    this.bestLocalFit = bestLocalFit;
    this.bestLocalCombination = bestLocalCombination;
    this.binList = binList;
    }

    public Long getBestLocalFit() {
        return bestLocalFit;
    }

    public void setBestLocalFit(Long bestLocalFit) {
        this.bestLocalFit = bestLocalFit;
    }

    public Integer getBestLocalCombination() {
        return bestLocalCombination;
    }

    public void setBestLocalCombination(Integer bestLocalCombination) {
        this.bestLocalCombination = bestLocalCombination;
    }

    public List<VisualizationClass> getBinList() {
        return this.binList;
    }

    public void setBinList(List<VisualizationClass> binList) {
        this.binList = binList;
    }
}
