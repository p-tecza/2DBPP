package com.azpt.binpacker;

import com.azpt.binpacker.packing.ContainerPackingManager;
import com.azpt.binpacker.packing.ContainerPackingService;
import com.azpt.binpacker.packing.domain.Bin;
import com.azpt.binpacker.packing.domain.Order;
import com.azpt.binpacker.packing.utils.Dims;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class BinpackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BinpackerApplication.class, args);
	}
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		Dims truckDims = new Dims(2400,2400,26000);

		List<Bin> binList = new ArrayList<>();
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

		ContainerPackingManager tpm = new ContainerPackingService();

		tpm.getFinalBinComposition(Order.builder().singleBins(binList).build(),truckDims);
	}

}
