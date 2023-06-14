package com.azpt.binpacker.packing.threaded;

import com.azpt.binpacker.packing.ContainerPackingService;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.domain.VisualizationClass;
import com.azpt.binpacker.packing.graphics.ContainerDrawer;
import com.azpt.binpacker.packing.utils.Dims;

import java.util.ArrayList;
import java.util.List;

public class ThreadedTask{

    private final static int nThreads = 5;
    ContainerPackingService cps = new ContainerPackingService();

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        List<Bin> binList2 = new ArrayList<>();

        binList2.add(Bin.builder()
                .outOrder(0)
                .binDimensions(new Dims(600,1000,1200))
                .identifier(1)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(0)
                .binDimensions(new Dims(400,1000,1200))
                .identifier(2)
                .rotatable(false)
                .stackable(false).build());
        binList2.add(Bin.builder()
                .outOrder(0)
                .binDimensions(new Dims(400,1000,1200))
                .identifier(3)
                .rotatable(false)
                .stackable(false).build());
        binList2.add(Bin.builder()
                .outOrder(0)
                .binDimensions(new Dims(1400,1000,1200))
                .identifier(4)
                .rotatable(false)
                .stackable(false).build());
        binList2.add(Bin.builder()
                .outOrder(1)
                .binDimensions(new Dims(400,1000,1200))
                .identifier(5)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(1)
                .binDimensions(new Dims(600,1000,800))
                .identifier(6)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(1)
                .binDimensions(new Dims(600,1000,1200))
                .identifier(7)
                .rotatable(false)
                .stackable(false).build());


        binList2.add(Bin.builder()
                .outOrder(1)
                .binDimensions(new Dims(800,1000,1200))
                .identifier(8)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(2)
                .binDimensions(new Dims(1000,1000,800))
                .identifier(9)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(2)
                .binDimensions(new Dims(600,800,1000))
                .identifier(10)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(2)
                .binDimensions(new Dims(200,1000,800))
                .identifier(11)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(2)
                .binDimensions(new Dims(100,1000,800))
                .identifier(12)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(3)
                .binDimensions(new Dims(210,1000,1000))
                .identifier(13)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(3)
                .binDimensions(new Dims(1000,1000,800))
                .identifier(14)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(3)
                .binDimensions(new Dims(1000,1000,800))
                .identifier(15)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(3)
                .binDimensions(new Dims(400,1000,1000))
                .identifier(16)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(4)
                .binDimensions(new Dims(600,1000,1000))
                .identifier(17)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(4)
                .binDimensions(new Dims(1000,1000,1000))
                .identifier(18)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(4)
                .binDimensions(new Dims(1600,1000,1000))
                .identifier(19)
                .rotatable(false)
                .stackable(false).build());

        binList2.add(Bin.builder()
                .outOrder(0)
                .binDimensions(new Dims(2200,1000,600))
                .identifier(20)
                .rotatable(false)
                .stackable(false).build());


        Dims outDims = new Dims(2400,2400,26000);

        List<Thread> listOfThreads = new ArrayList<>();
        List<ThreadTask> listOfRunnables = new ArrayList<>();

        long globalOptimal = Long.MAX_VALUE;
        int globalBestCombination=0;

        List<Bin> binList1 = new ArrayList<>(binList2);

        List<List<Bin>> binLists = new ArrayList<>();

        binLists.add(binList1);
        binLists.add(binList2);

        for(int i = 0; i < nThreads; i++){
            ThreadTask newRunnable = new ThreadTask(new ThreadParameters(i*2000,(i+1)*2000,
                    binList1,outDims));
//            ThreadTask newRunnable = new ThreadTask(new ThreadParameters(0,10000,
//                    binList1,outDims));
            Thread newThread = new Thread(newRunnable);
            listOfThreads.add(newThread);
            listOfRunnables.add(newRunnable);
            newThread.start();
        }

        List<VisualizationClass> finalVis = new ArrayList<>();
        long res;
        for(int i = 0 ; i < nThreads; i++){
            listOfThreads.get(i).join();
            res = listOfRunnables.get(i).getBestFit();
            if(res < globalOptimal){
                globalOptimal = res;
                globalBestCombination = listOfRunnables.get(i).getBestCombination();
                finalVis = listOfRunnables.get(i).getVisualization();
            }
        }
        long stop = System.currentTimeMillis();
        ContainerDrawer drawer = new ContainerDrawer();
        drawer.draw(finalVis);

        System.out.println("CZAS: "+ (stop-start)+"ms");
        System.out.println("NAJLEPSZE DOPASOWANIE: "+globalOptimal);
        System.out.println("NAJLEPSZA KOMBINACJA: "+globalBestCombination);


    }

}
