package com.azpt.binpacker.packing.graphics;

import com.azpt.binpacker.packing.domain.VisualizationClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContainerDrawer{
    public static double SCALE = 14;

    public void draw(List<VisualizationClass> list){
        BinPanel panel = new BinPanel(list);

        JFrame frame = new JFrame("Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize((int) (2400/SCALE), (int) (26000/SCALE));
        frame.setVisible(true);

    }
}
