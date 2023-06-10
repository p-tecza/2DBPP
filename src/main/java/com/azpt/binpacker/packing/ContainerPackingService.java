package com.azpt.binpacker.packing;

import com.azpt.binpacker.packing.domain.*;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContainerPackingService implements ContainerPackingManager {
    long start = System.currentTimeMillis();
    long stop;

    @Override
    public LoadedContainer getFinalBinComposition(Order order, Dims outContainerDimensions) {
        List<Bin> binList = order.getSingleBins();

        binList.sort(Comparator.comparingInt(Bin::getOutOrder)
                .thenComparingInt(Bin::getSize).reversed());

        int maxOutOrder = binList.stream().max(Comparator.comparing(Bin::getOutOrder)).get().getOutOrder();
        if (maxOutOrder > 10) maxOutOrder = 10;


        List<VisualizationClass> binsVisualization = new ArrayList<>();
        List<VisualizationClass> finalBinsVisualization = new ArrayList<>();
        long bestFit = Long.MAX_VALUE;
        Pair<Long, List<VisualizationClass>> returnedResults;

        //
        for (int i = 0; i < Math.pow(2, maxOutOrder + 1); i++) {
            returnedResults = placeBinsInContainer(generateNewContainersOrder(order, i), outContainerDimensions);
            if (bestFit > returnedResults.getKey() && returnedResults.getKey() >= 0) {
                bestFit = returnedResults.getKey();
                finalBinsVisualization = returnedResults.getValue();
            }
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(finalBinsVisualization);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("SPACE WASTED: " + bestFit);
        stop = System.currentTimeMillis();
        log.info("OVERALL TIME: " + (stop - start));
        log.info(json);

        return null;
    }

    private List<Bin> generateNewContainersOrder(Order order, int nIteration) {

//        List<BinPacket> packets = order.getPackets();
//        List<Bin> binList = order.getSingleBins();

        List<Bin> binList = unpackPackets(order);
        binList.sort(Comparator.comparingInt(Bin::getOutOrder)
                .thenComparingInt(Bin::getSize).reversed());

        log.info("PO ULOZENIU W LISTE I SORTOWANIU:\n");

        for (Bin b : binList) {
            log.info(b.toString());
        }

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

//
//    for (int i = 0; i < reverseBinaryOrder.length(); i++) {
//      if(outOrder < 0) break;
//      int finalOutOrder = outOrder;
//      partialBinList = binList.stream()
//        .filter(p -> p.getOutOrder() == finalOutOrder).collect(Collectors.toList());
//      outOrder--;
//
//      if (reverseBinaryOrder.charAt(i) == '1') {
//        partialBinList.sort(Comparator.comparingInt(Bin::getSize));
//      }
//
//      readyBinList.addAll(partialBinList);
//    }

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

    private Pair<Long, List<VisualizationClass>> placeBinsInContainer(List<Bin> binList, Dims truckDimensions) {

        int truckWidth = truckDimensions.getWidth();
        int truckDepth = truckDimensions.getDepth();

        Container truckContainer = new Container(0, 0, truckWidth, truckDepth);
        List<Container> usedSpace = new ArrayList<>();
        List<Pair<Integer, Integer>> possiblePlacementCoordinates = new ArrayList<>(List.of(new Pair<>(0, 0)));
        List<VisualizationClass> binsVisualization = new ArrayList<>();

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

                    int deadlineLength = calculateDeadlineLength(usedSpace, tempContainer, truckContainer);
                    int smallestLengthNextIt = determineSmallestLengthNextIt(p.getOutOrder(), binList);

                    usedSpace.add(tempContainer);
                    foundSpotFlag = true;
                    if (tempContainer.getEndY() > mostForwardDepthPoint) {
                        mostForwardDepthPoint = tempContainer.getEndY();
                    }
                    actualUsedSpace += (long) binWidth * binDepth;
                    binsVisualization.add(new VisualizationClass(p.getIdentifier(), cords.getKey(), cords.getValue(), 0,
                            binWidth, binDepth, 500));

                    possiblePlacementCoordinates.remove(cords);
                    break;
                }

            }

            List<Bin> binListForDeterminingUnusableSpots =
                    getListOfBinsFromThisAndNextOutOrder(p,binList,binCollectionIterator);
            possiblePlacementCoordinates = deleteUnusablePlacementSpots(possiblePlacementCoordinates,usedSpace,
                    binListForDeterminingUnusableSpots,truckContainer);

            if (!foundSpotFlag) {

                return new Pair<>(-1L, null);
            }
            findNewSpots(tempContainer, possiblePlacementCoordinates, truckContainer);
            binCollectionIterator++;

        }

        return new Pair<>((long) mostForwardDepthPoint * truckWidth - actualUsedSpace, binsVisualization); //nieuzyte miejsce

    }

    private List<Pair<Integer, Integer>> deleteUnusablePlacementSpots(List<Pair<Integer, Integer>> possiblePlacementCoordinates,
                                                                      List<Container> usedSpace,
                                                                      List<Bin> listOfBinsToBePlaced,
                                                                      Container truckContainer) {
        List<Pair<Integer, Integer>> updatedSpots = new ArrayList<>();
        for (Pair<Integer, Integer> cords : possiblePlacementCoordinates) {
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
        return updatedSpots;
    }


    private List<Bin> getListOfBinsFromThisAndNextOutOrder(Bin bin, List<Bin> allBins, int binCollectionIterator) {

        List<Bin> specifiedList = new ArrayList<>();
        int currentOutOrder = bin.getOutOrder();
        for (int i = binCollectionIterator + 1; i < allBins.size(); i++) {
            if (allBins.get(i).getOutOrder() > currentOutOrder + 1) {
                break;
            }
            specifiedList.add(allBins.get(i));
        }
        return specifiedList;
    }


    private int determineSmallestLengthNextIt(int currIt, List<Bin> binList) {

        binList.stream().filter(p -> p.getOutOrder() == currIt + 1).collect(Collectors.toList());
        return -1;
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

    private int calculateDeadlineLength(List<Container> usedSpace,
                                        Container placedBin,
                                        Container truckContainer) {

        Container leftZone = new Container(truckContainer.startX, placedBin.startY,
                placedBin.startX, placedBin.endY);

        Container rightZone = new Container(placedBin.endX, placedBin.startY,
                truckContainer.endX, placedBin.endY);

        int minX = placedBin.startX;
        int maxX = placedBin.endX;

        for (Container c : usedSpace) {
            if (checkIfContainsOrOverlaps(leftZone, c) || checkIfContainsOrOverlaps(rightZone, c)) {
                if (minX > c.startX) {
                    minX = c.startX;
                }
                if (maxX < c.endX) {
                    maxX = c.endX;
                }
            }

        }

        return maxX;
    }

    private boolean checkIfContainsOrOverlaps(Container zone, Container container) {
        if (container.getStartX() < zone.getEndX()
                && container.getEndX() > zone.getStartX()
                && container.getStartY() < zone.getEndY()
                && container.getEndY() > zone.getStartY()) {
            return true;
        }
        return container.startY >= zone.startY && container.startX >= zone.startX
                && container.endX <= zone.endX && container.endY <= zone.endY;
    }

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


}
