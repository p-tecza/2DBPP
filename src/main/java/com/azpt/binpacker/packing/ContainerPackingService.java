package com.azpt.binpacker.packing;

import com.azpt.binpacker.packing.domain.*;
import com.azpt.binpacker.packing.graphics.ContainerDrawer;
import com.azpt.binpacker.packing.utils.Dims;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContainerPackingService implements ContainerPackingManager {
    long start = System.currentTimeMillis();
    long stop;

    ContainerDrawer drawer = new ContainerDrawer();
    public MyPair partialJob(int startInclusive, int stopExclusive,
                                             List<Bin> binListToCopy, Dims outDims ){

        List<Bin> binList = new ArrayList<>(binListToCopy);
        binList.sort(Comparator.comparingInt(Bin::getOutOrder)
                .thenComparingInt(Bin::getSize).reversed());
        long bestLocalFit = Long.MAX_VALUE;
        int bestLocalCombination = startInclusive;
        List<VisualizationClass> bestLocalBinSetup = new ArrayList<>();
        Pair<Long, Pair<List<VisualizationClass>, List<VisualizationClass>>> returnedResults;

        for(int i = startInclusive; i < stopExclusive; i++){
            returnedResults = placeBinsInContainer(generateNewContainersOrder(binList, i), outDims);
            if (bestLocalFit > returnedResults.getKey() && returnedResults.getKey() >= 0) {
                bestLocalFit = returnedResults.getKey();
                bestLocalCombination = i;
                bestLocalBinSetup = returnedResults.getValue().getKey();
//                finalBinsVisualization = returnedResults.getValue().getKey();
//                wiz = returnedResults.getValue().getValue();
            }
        }

        System.out.println("BLF:"+bestLocalFit);
        System.out.println("BLC:"+bestLocalCombination);

        return new MyPair(bestLocalFit,bestLocalCombination, bestLocalBinSetup);
    }

    @Override
    public LoadedContainer getFinalBinComposition(Order order, Dims outContainerDimensions) {
        List<Bin> binList = order.getSingleBins();

        binList.sort(Comparator.comparingInt(Bin::getOutOrder)
                .thenComparingInt(Bin::getSize).reversed());

        int maxOutOrder = binList.stream().max(Comparator.comparing(Bin::getOutOrder)).get().getOutOrder();
        if (maxOutOrder > 10) maxOutOrder = 10;


        List<VisualizationClass> binsVisualization = new ArrayList<>();
        List<VisualizationClass> finalBinsVisualization = new ArrayList<>();
        List<VisualizationClass> wiz = new ArrayList<>();
        long bestFit = Long.MAX_VALUE;
        Pair<Long, Pair<List<VisualizationClass>, List<VisualizationClass>>> returnedResults;

        //Math.pow(2, maxOutOrder + 1)
        for (int i = 0; i < 5000; i++) {
            returnedResults = placeBinsInContainer(generateNewContainersOrder(binList, i), outContainerDimensions);
            if (bestFit > returnedResults.getKey() && returnedResults.getKey() >= 0) {
                bestFit = returnedResults.getKey();
                finalBinsVisualization = returnedResults.getValue().getKey();
                wiz = returnedResults.getValue().getValue();
            }
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(finalBinsVisualization);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        log.info("SPACE WASTED: " + bestFit);
//        stop = System.currentTimeMillis();
//        log.info("OVERALL TIME: " + (stop - start));
//        log.info(json);

        drawer.draw(finalBinsVisualization);
        drawer.draw(wiz);

        return null;
    }

    private List<Bin> generateNewContainersOrder(List<Bin> binList, int nIteration) {

//        List<Bin> binList = order.getSingleBins();

        binList.sort(Comparator.comparingInt(Bin::getOutOrder)
                .thenComparingInt(Bin::getSize).reversed());


        String sortingOrder = Integer.toBinaryString(nIteration);
        String reverseBinaryOrder = new StringBuilder(sortingOrder).reverse().toString();
        List<Bin> partialBinList;
        List<Bin> readyBinList = new ArrayList<>();
        int outOrder = binList.stream().max(Comparator.comparing(Bin::getOutOrder)).get().getOutOrder();

        for (int i = 0; i < reverseBinaryOrder.length(); i++) {

            if (outOrder < 0) break;
            int finalOutOrder = outOrder;
            partialBinList = binList.stream()
                    .filter(p -> p.getOutOrder() == finalOutOrder).collect(Collectors.toList());
            outOrder--;
            if (reverseBinaryOrder.charAt(i) == '1') {
                partialBinList.sort(Comparator.comparingInt(Bin::getSize));
            }
            readyBinList.addAll(partialBinList);
        }

        for (int i = readyBinList.size(); i < binList.size(); i++) {
            readyBinList.add(binList.get(i));
        }

        return readyBinList;
    }

    private List<Bin> unpackPackets(Order order) {

        int maxOutOrder = determineHighestOutOrder(order);
        boolean containedSingleBin = false;
        List<BinPacket> packets = order.getPackets();
        List<Bin> bins = order.getSingleBins();

        bins.sort(Comparator.comparingInt(Bin::getOutOrder));
        packets.sort(Comparator.comparingInt(BinPacket::getOutOrder));

        List<Bin> listOfBinsWithPackets = new ArrayList<>();
        int newListOrderIt = 0;
        for (int i = 0; i <= maxOutOrder; i++) {

            for (BinPacket bp : packets) {
                if (bp.getOutOrder() == i) {
                    for (Bin b : bp.getBins()) {
                        b.setOutOrder(newListOrderIt);
                        listOfBinsWithPackets.add(b);
                    }
                    newListOrderIt++;
                }
            }

            for (Bin b : bins) {
                if (b.getOutOrder() == i) {
                    listOfBinsWithPackets.add(
                            Bin.builder().binDimensions(b.getBinDimensions())
                                    .rotatable(b.isRotatable())
                                    .identifier(b.getIdentifier())
                                    .stackable(b.isStackable())
                                    .outOrder(newListOrderIt)
                                    .build()
                    );
                    containedSingleBin = true;
                }
            }
            if (containedSingleBin) {
                newListOrderIt++;
                containedSingleBin = false;
            }

        }
        return listOfBinsWithPackets;
    }

    private int determineHighestOutOrder(Order order) {
        List<BinPacket> packets = order.getPackets();
        List<Bin> bins = order.getSingleBins();
        int maxBinOutOrder = bins.stream().max(Comparator.comparing(Bin::getOutOrder))
                .orElse(Bin.builder().build()).getOutOrder();
        int maxPacketOutOrder = packets.stream().max(Comparator.comparing(BinPacket::getOutOrder))
                .orElse(BinPacket.builder().build()).getOutOrder();
        return Math.max(maxBinOutOrder, maxPacketOutOrder);
    }

    private Pair<Long, Pair<List<VisualizationClass>, List<VisualizationClass>>> placeBinsInContainer(List<Bin> binList, Dims truckDimensions) {

        int truckWidth = truckDimensions.getWidth();
        int truckDepth = truckDimensions.getDepth();

        Container truckContainer = new Container(0, 0, truckWidth, truckDepth);
        List<Container> usedSpace = new ArrayList<>();
        List<Pair<Integer, Integer>> possiblePlacementCoordinates = new ArrayList<>(List.of(new Pair<>(0, 0)));
        List<VisualizationClass> binsVisualization = new ArrayList<>();
        List<Pair<Integer, Integer>> rejectedSpots = new ArrayList<>();

        boolean foundSpotFlag;
        long actualUsedSpace = 0;
        int mostForwardDepthPoint = 0;


        int binCollectionIterator = 0;
        for (Bin p : binList) {



            int binWidth = p.getBinDimensions().getWidth();
            int binDepth = p.getBinDimensions().getDepth();
            foundSpotFlag = false;
            Container tempContainer = new Container();
            int startX;
            int startY;
            for (Pair<Integer, Integer> cords : possiblePlacementCoordinates) {
                startX = cords.getKey();
                startY = cords.getValue();
                tempContainer = new Container(startX, startY, startX + binWidth, startY + binDepth);

                if (!checkIfOverlaps(truckContainer, usedSpace, tempContainer)) {


//                    log.info("WKLADAM KONTENER: " + p.getIdentifier() +" OUT ORDER: "+p.getOutOrder());

                    usedSpace.add(tempContainer);
                    foundSpotFlag = true;
                    if (tempContainer.getEndY() > mostForwardDepthPoint) {
                        mostForwardDepthPoint = tempContainer.getEndY();
                    }
                    actualUsedSpace += (long) binWidth * binDepth;
                    binsVisualization.add(new VisualizationClass(p.getIdentifier(), p.getOutOrder(), cords.getKey(), cords.getValue(), 0,
                            binWidth, binDepth, 500));

                    possiblePlacementCoordinates.remove(cords);
;
                    possiblePlacementCoordinates =
                            deleteOverlappedPlacementPoints(possiblePlacementCoordinates, tempContainer);


                    break;
                }

            }

            List<Bin> binListForDeterminingUnusableSpots =
                    getListOfBinsFromThisAndNextOutOrder(p, binList, binCollectionIterator);
            possiblePlacementCoordinates = deleteUnusablePlacementSpots(possiblePlacementCoordinates, usedSpace,
                    binListForDeterminingUnusableSpots, truckContainer, rejectedSpots);


            if (!foundSpotFlag) {
//                log.info("NIE ZNALEZIONO MIEJSCA NA KONTENER");

                //TODO DODAJ PRÓBĘ WPAKOWANIA NA KONIEC KONTENERA JEŻELI SIĘ DA

                tempContainer = tryToFitUnderAllContainers(usedSpace,truckContainer,p);
                if(tempContainer == null){
                    return new Pair<>(-1L, null);
                }
                usedSpace.add(tempContainer);
                binsVisualization.add(new VisualizationClass(p.getIdentifier(), p.getOutOrder(),
                        tempContainer.startX, tempContainer.startY, 0,
                        binWidth, binDepth, 500));
                actualUsedSpace += (long) binWidth * binDepth;
            }
            findNewSpots(tempContainer, possiblePlacementCoordinates, truckContainer);
            binCollectionIterator++;
//            log.info("POZOSTAJA PLACEMENT POINTS: "+possiblePlacementCoordinates.size());

        }


        List<VisualizationClass> wiz = calculateUnusedSpaceBasedOnRejectedPoints(rejectedSpots, usedSpace, truckDimensions);

//        log.info("TO DRUGIE NIEUZYTE MIEJSCE");
//        log.info("-> " + ((long) mostForwardDepthPoint * truckWidth - actualUsedSpace));


        return new Pair<>((long) mostForwardDepthPoint * truckWidth - actualUsedSpace,
                new Pair<>(binsVisualization, wiz)); //nieuzyte miejsce

    }

    private Container tryToFitUnderAllContainers(List<Container> usedSpace, Container truckContainer,
                                                              Bin wannaBePlacedBin){

        int maxY = 0;
        for(Container c: usedSpace){
            if(c.getEndY() > maxY){
                maxY=c.getEndY();
            }
        }

        if(maxY + wannaBePlacedBin.getBinDimensions().getDepth() > truckContainer.getEndY()
        || wannaBePlacedBin.getBinDimensions().getWidth() > truckContainer.getEndX()){
            return null;
        }

        return new Container(0,maxY,
                wannaBePlacedBin.getBinDimensions().getWidth(),
                maxY+wannaBePlacedBin.getBinDimensions().getDepth());
    }

    private List<Pair<Integer, Integer>> deleteOverlappedPlacementPoints(List<Pair<Integer, Integer>> placementPoints,
                                                                         Container placedContainer) {
        List<Pair<Integer, Integer>> nonOverlappedPlacemendPoints = new ArrayList<>();
        for (Pair<Integer, Integer> p : placementPoints) {
            if (!isPointOverlappedByRectangle(p, placedContainer)) {
                nonOverlappedPlacemendPoints.add(p);

            } else {
//                log.info("WYRZUCAM TEN PUNKT");
//                log.info(p.getKey() + " " + p.getValue());
            }
        }
        return nonOverlappedPlacemendPoints;
    }

    private boolean isPointOverlappedByRectangle(Pair<Integer, Integer> point, Container rectangle) {
        return point.getKey() >= rectangle.getStartX() && point.getKey() <= rectangle.getEndX() &&
                point.getValue() >= rectangle.getStartY() && point.getValue() <= rectangle.getEndY();
//        return true;
    }

    private List<VisualizationClass> calculateUnusedSpaceBasedOnRejectedPoints(List<Pair<Integer, Integer>> rejectedSpots, List<Container> usedSpace,
                                                                               Dims truckDimensions) {
        int spaceWastedSum = 0;
        int widthPoint;
        int depthPoint;

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        Container truckContainer = new Container(0, 0, truckDimensions.getWidth(), truckDimensions.getDepth());

        List<VisualizationClass> wiz = new ArrayList<>();

        for (Pair<Integer, Integer> rejectedSpot : rejectedSpots) {
            widthPoint = truckDimensions.getWidth();
            depthPoint = truckDimensions.getDepth();

            for (Container c : usedSpace) {
                if (c.getStartX() > rejectedSpot.getKey() && c.getStartX() < widthPoint) {
                    widthPoint = c.getStartX();
                }
                if (c.getStartY() > rejectedSpot.getValue() && c.getStartY() < depthPoint) {
                    depthPoint = c.getStartY();
                }
            }

            if (depthPoint != truckDimensions.getDepth()) {
//                log.info("ODRZUCANY KONTENER");
//                log.info("KORDY: X:" + rejectedSpot.getKey() + " Y:" + rejectedSpot.getValue());
//                log.info("WYMIARY: " + (widthPoint - rejectedSpot.getKey()) + "x" + (depthPoint - rejectedSpot.getValue()));


                if (!checkIfOverlaps(truckContainer, usedSpace, new Container(
                        rejectedSpot.getKey(), rejectedSpot.getValue(), widthPoint, depthPoint)
                )) {
                    wiz.add(new VisualizationClass(123,1, rejectedSpot.getKey(), rejectedSpot.getValue(), 0,
                            widthPoint - rejectedSpot.getKey(), depthPoint - rejectedSpot.getValue(), 500));

                    spaceWastedSum += (widthPoint - rejectedSpot.getKey()) * (depthPoint - rejectedSpot.getValue());
                }


            }

        }

        try {
            json = ow.writeValueAsString(wiz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        log.info("TU JSON");
//        log.info(json);
//
//        log.info("ZUZYTE MIEJSCE: "+spaceWastedSum);

        return wiz;
    }

    private List<Pair<Integer, Integer>> deleteUnusablePlacementSpots(List<Pair<Integer, Integer>> possiblePlacementCoordinates,
                                                                      List<Container> usedSpace,
                                                                      List<Bin> listOfBinsToBePlaced,
                                                                      Container truckContainer,
                                                                      List<Pair<Integer, Integer>> rejectedSpots) {

        //TODO policz i dodaj nieuzywane miejsce na podstawie odrzuconych pkt
        List<Pair<Integer, Integer>> updatedSpots = new ArrayList<>();
        for (Pair<Integer, Integer> cords : possiblePlacementCoordinates) {
            Container verticalFreeSpace =
                    new Container(cords.getKey(), cords.getValue(),
                            findLengthToBinClosestToRight(usedSpace,cords,truckContainer), truckContainer.endY);
            if(!checkIfOverlaps(truckContainer,usedSpace,verticalFreeSpace)){
                updatedSpots.add(cords);
                continue;
            }

            for (Bin b : listOfBinsToBePlaced) {
                int startX = cords.getKey();
                int startY = cords.getValue();
                int binWidth = b.getBinDimensions().getWidth();
                int binDepth = b.getBinDimensions().getDepth();
                Container tempContainer = new Container(startX, startY, startX + binWidth, startY + binDepth);
                if (!checkIfOverlaps(truckContainer, usedSpace, tempContainer)) {
                    updatedSpots.add(new Pair<>(cords));
                    break;
                }
            }
        }

        for (Pair<Integer, Integer> cords : possiblePlacementCoordinates) {
            if (!updatedSpots.contains(cords)) {
                rejectedSpots.add(cords);
            }
        }

        return updatedSpots;
    }


    private int findLengthToBinClosestToRight(List<Container> usedSpace, Pair<Integer,Integer> spot, Container truckContainer){

        int closestToRight = truckContainer.endX - truckContainer.startX;

        for(Container c : usedSpace){
            if(spot.getValue() < c.getEndY() && spot.getValue() > c.getStartY()){
                int lengthToRightContainer = c.getStartX() - spot.getKey();
                if(lengthToRightContainer > 0 && closestToRight > lengthToRightContainer){
                    closestToRight = lengthToRightContainer;
                }
            }
        }
        return closestToRight;
    }

    private int findBinClosestToBottom(List<Bin> usedSpace, Pair<Integer,Integer> spot){
        return 1;
    }

    private List<Bin> getListOfBinsFromThisAndNextOutOrder(Bin bin, List<Bin> allBins, int binCollectionIterator) {

        List<Bin> specifiedList = new ArrayList<>();
        int currentOutOrder = bin.getOutOrder();
        for (int i = binCollectionIterator + 1; i < allBins.size(); i++) {
            if (allBins.get(i).getOutOrder() != currentOutOrder) {
                break;
            }
            specifiedList.add(allBins.get(i));
        }
        return specifiedList;
    }

    private boolean checkIfOverlaps(Container truckContainer, List<Container> usedSpace, Container testContainer) {

        if (testContainer.getEndX() > truckContainer.getEndX() || testContainer.getEndY() > truckContainer.getEndY()) {
            return true;
        }
        for (Container placedContainer : usedSpace) {
            if (testContainer.getStartX() < placedContainer.getEndX()
                    && testContainer.getEndX() > placedContainer.getStartX()
                    && testContainer.getStartY() < placedContainer.getEndY()
                    && testContainer.getEndY() > placedContainer.getStartY()) {
                return true;
            }
        }
        return false;
    }

    private void findNewSpots(Container lastPlaced,
                              List<Pair<Integer, Integer>> possiblePlacements,
                              Container truckContainer) {
        if (truckContainer.getEndX() > lastPlaced.getEndX()) {
            possiblePlacements.add(new Pair<>(lastPlaced.getEndX(), lastPlaced.getStartY()));
        }
        if (truckContainer.getEndY() > lastPlaced.getEndY()) {
            possiblePlacements.add(new Pair<>(lastPlaced.getStartX(), lastPlaced.getEndY()));
        }
    }

//    private int calculateDeadlineLength(List<Container> usedSpace,
//                                        Container placedBin,
//                                        Container truckContainer) {
//
//        Container leftZone = new Container(truckContainer.startX, placedBin.startY,
//                placedBin.startX, placedBin.endY);
//
//        Container rightZone = new Container(placedBin.endX, placedBin.startY,
//                truckContainer.endX, placedBin.endY);
//
//        int minX = placedBin.startX;
//        int maxX = placedBin.endX;
//
//        for (Container c : usedSpace) {
//            if (checkIfContainsOrOverlaps(leftZone, c) || checkIfContainsOrOverlaps(rightZone, c)) {
//                if (minX > c.startX) {
//                    minX = c.startX;
//                }
//                if (maxX < c.endX) {
//                    maxX = c.endX;
//                }
//            }
//
//        }
//
//        return maxX;
//    }

//    private boolean checkIfContainsOrOverlaps(Container zone, Container container) {
//        if (container.getStartX() < zone.getEndX()
//                && container.getEndX() > zone.getStartX()
//                && container.getStartY() < zone.getEndY()
//                && container.getEndY() > zone.getStartY()) {
//            return true;
//        }
//        return container.startY >= zone.startY && container.startX >= zone.startX
//                && container.endX <= zone.endX && container.endY <= zone.endY;
//    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private class Container {
        int startX;
        int startY;
        int endX;
        int endY;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private class PlacementSpots {
        List<Pair<Integer, Integer>> updatedSpots;
        List<Pair<Integer, Integer>> rejectedSpots;
    }


}
