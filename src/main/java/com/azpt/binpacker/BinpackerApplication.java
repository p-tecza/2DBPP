package com.azpt.binpacker;

import com.azpt.binpacker.packing.ContainerPackingManager;
import com.azpt.binpacker.packing.ContainerPackingService;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.domain.BinPacket;
import com.azpt.binpacker.packing.domain.Order;
import com.azpt.binpacker.packing.utils.Dims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class BinpackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BinpackerApplication.class, args);
	}
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		Dims truckDims = new Dims(2400,2400,26000);

		List<Bin> binList = new ArrayList<>();
		List<Bin> binList2 = new ArrayList<>();
		List<Bin> binList3 = new ArrayList<>();
		binList.add(Bin.builder()
						.outOrder(1)
						.binDimensions(new Dims(1000,1000,2000))
						.identifier(1)
						.rotatable(false)
						.stackable(false).build());

		binList.add(Bin.builder()
				.outOrder(2)
				.binDimensions(new Dims(2000,1000,4000))
				.identifier(2)
				.rotatable(false)
				.stackable(false).build());

		binList.add(Bin.builder()
				.outOrder(1)
				.binDimensions(new Dims(800,1000,1200))
				.identifier(3)
				.rotatable(false)
				.stackable(false).build());

		binList.add(Bin.builder()
				.outOrder(3)
				.binDimensions(new Dims(600,1000,800))
				.identifier(4)
				.rotatable(false)
				.stackable(false).build());

		binList.add(Bin.builder()
				.outOrder(1)
				.binDimensions(new Dims(2000,1000,1200))
				.identifier(5)
				.rotatable(false)
				.stackable(false).build());


		binList2.add(Bin.builder()
				.outOrder(1)
				.binDimensions(new Dims(800,1000,1200))
				.identifier(6)
				.rotatable(false)
				.stackable(false).build());

		binList2.add(Bin.builder()
				.outOrder(3)
				.binDimensions(new Dims(600,1000,800))
				.identifier(7)
				.rotatable(false)
				.stackable(false).build());

		binList2.add(Bin.builder()
				.outOrder(1)
				.binDimensions(new Dims(2000,1000,1200))
				.identifier(8)
				.rotatable(false)
				.stackable(false).build());

		binList3.add(Bin.builder()
				.outOrder(1)
				.binDimensions(new Dims(800,1000,1200))
				.identifier(8)
				.rotatable(false)
				.stackable(false).build());

		binList3.add(Bin.builder()
				.outOrder(3)
				.binDimensions(new Dims(600,1000,800))
				.identifier(9)
				.rotatable(false)
				.stackable(false).build());


		BinPacket bp = BinPacket.builder().bins(binList).outOrder(1).build();
		BinPacket bp2 = BinPacket.builder().bins(binList3).outOrder(0).build();
		List<BinPacket> packets = new ArrayList<>();
		packets.add(bp);
		packets.add(bp2);


		ContainerPackingManager tpm = new ContainerPackingService();

		Order newOrder = Order.builder().singleBins(binList2).packets(packets).build();


		log.info("ORDER: ");
		log.info(newOrder.toString());

		tpm.getFinalBinComposition(newOrder,truckDims);


	}

}
