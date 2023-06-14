package com.azpt.binpacker.packing.threaded;

import com.azpt.binpacker.packing.ContainerPackingService;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.domain.MyPair;
import com.azpt.binpacker.packing.domain.VisualizationClass;
import com.azpt.binpacker.packing.utils.Dims;

import java.util.List;

public class ThreadTask implements Runnable{

    private int startInclusive;
    private int stopExclusive;
    private volatile List<Bin> binList;
    private volatile Dims outDims;
    private ContainerPackingService cps;
    private long bestFit;
    private int bestCombination;

    private List<VisualizationClass> visualization;
    public ThreadTask(ThreadParameters tp){
        this.startInclusive=tp.startInclusive;
        this.stopExclusive=tp.stopExclusive;
        this.binList=tp.binList;
        this.outDims=tp.outDims;
        this.cps = new ContainerPackingService();
    }

    @Override
    public void run() {
        System.out.println("URUCHOMIONO WATEK");
       MyPair results = cps.partialJob(this.startInclusive,this.stopExclusive,this.binList,this.outDims);
       this.bestFit = results.getBestLocalFit();
       this.bestCombination = results.getBestLocalCombination();
       this.visualization = results.getBinList();
    }

    public long getBestFit(){
        return this.bestFit;
    }
    public int getBestCombination(){
        return this.bestCombination;
    }

    public List<VisualizationClass> getVisualization(){
        return this.visualization;
    }
}
