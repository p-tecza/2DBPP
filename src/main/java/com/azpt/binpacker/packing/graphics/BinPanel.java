package com.azpt.binpacker.packing.graphics;

import com.azpt.binpacker.packing.domain.VisualizationClass;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.azpt.binpacker.packing.graphics.ContainerDrawer.SCALE;

public class BinPanel extends JPanel {

    private List<VisualizationClass> visualizationClasses;
    private Map<Integer, Color> colorMap;

    public BinPanel(List<VisualizationClass> visualizationClasses) {
        this.visualizationClasses = visualizationClasses;
        this.colorMap = new HashMap<>();

        for (VisualizationClass vc : visualizationClasses) {
            colorMap.put(vc.getId(), new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color((float) (220/255.0), (float) (220/255.0), (float) (220/255.0)));
        g.fillRect(0, 0, (int) (2400/SCALE), (int) (26000/SCALE));


        for (VisualizationClass vc : visualizationClasses) {
            Color color = colorMap.get(vc.getId());
            g.setColor(color);
            g.fillRect((int) (vc.getX()/ SCALE), (int) (vc.getY()/ SCALE), (int) (vc.getDx()/ SCALE), (int) (vc.getDy()/ SCALE));

            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            String text = String.valueOf(vc.getId());
            int textX = (int) (vc.getX()/ SCALE + (vc.getDx()/ SCALE - fm.stringWidth(text)) / 2);
            int textY = (int) (vc.getY()/ SCALE + ((vc.getDy()/ SCALE - fm.getHeight()) / 2) + fm.getAscent());
            g.drawString(text, textX, textY);
        }
    }
}