package com.caleb.smashshocker;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Fill {

    public static boolean floodFill(BufferedImage img, int startX, int startY, Color targetColor,
                                    Color replacementColor) {
        return floodFill(img, startX, startY, targetColor.getRGB(), replacementColor.getRGB());
    }

    /**
     * Performs flood fill on the image.
     * Based on the <a href="http://en.wikipedia.org/wiki/Flood_fill" target="_blank">2nd alternative implementation</a>.
     *
     * @param img      the image to perform the floodfill on
     * @param startX   the starting point, x coordinate
     * @param startY   the starting point, y coordinate
     * @param targetColor   the target color (what we want to fill)
     * @param replacementColor   the replacement color (the color fill with)
     * @return      true if successfully filled
     */
    public static boolean floodFill(BufferedImage img, int startX, int startY, int targetColor,
                                    int replacementColor) {
        return floodFill(img, startX, startY, targetColor, replacementColor, new int[4]);
    }

    /**
     * Performs flood fill on the image. Records the extent of the fill
     * (bounding box).
     * Based on the <a href="http://en.wikipedia.org/wiki/Flood_fill" target="_blank">2nd alternative implementation</a>.
     *
     * @param img      the image to perform the floodfill on
     * @param startX   the starting point, x coordinate
     * @param startY   the starting point, y coordinate
     * @param targetColor   the target color (what we want to fill)
     * @param replacementColor   the replacement color (the color fill with)
     * @param extent   for recording the bounding box for the flood fill, all -1 if failed to fill
     * @return      true if successfully filled
     */
    public static boolean floodFill(BufferedImage img, int startX, int startY, Color targetColor,
                                    Color replacementColor, int[] extent) {
        return floodFill(img, startX, startY, targetColor.getRGB(), replacementColor.getRGB(), extent);
    }

    /**
     * Performs flood fill on the image. Records the extent of the fill
     * (bounding box).
     * Based on the <a href="http://en.wikipedia.org/wiki/Flood_fill" target="_blank">2nd alternative implementation</a>.
     *
     * @param img      the image to perform the floodfill on
     * @param startX   the starting point, x coordinate
     * @param startY   the starting point, y coordinate
     * @param targetColor   the target color (what we want to fill)
     * @param replacementColor   the replacement color (the color fill with)
     * @param extent   for recording the bounding box for the flood fill, all -1 if failed to fill
     * @return      true if successfully filled
     */
    public static boolean floodFill(BufferedImage img, int startX, int startY, int targetColor,
                                    int replacementColor, int[] extent) {
        LinkedList<int[]> queue;
        LinkedList<int[]> queueNew;
        int west;
        int east;
        int width;
        int height;
        int i;

        // can't fill?
        if (img.getRGB(startX, startY) != targetColor) {
            extent[0] = -1;
            extent[1] = -1;
            extent[2] = -1;
            extent[3] = -1;
            return false;
        }

        // init extent
        extent[0] = startX;
        extent[1] = startY;
        extent[2] = startX;
        extent[3] = startY;

        // init queue
        queue = new LinkedList<int[]>();
        queue.add(new int[] { startX, startY });

        width = img.getWidth();
        height = img.getHeight();
        while (!queue.isEmpty()) {
            queueNew = new LinkedList<int[]>();
            for (int[] pos : queue) {
                if (img.getRGB(pos[0], pos[1]) != replacementColor) {
                    west = pos[0];
                    east = west;

                    // go west
                    while ((west > 0) && img.getRGB(west - 1, pos[1]) != replacementColor)
                        west--;
                    // go east
                    while ((east < width - 1) && img.getRGB(east + 1, pos[1]) != replacementColor)
                        east++;

                    // check pixels (north/south)
                    for (i = west; i <= east; i++) {
                        img.setRGB(i, pos[1], replacementColor);
                        // north?
                        if ((pos[1] > 0) && (img.getRGB(i, pos[1] - 1) != replacementColor))
                            queueNew.add(new int[] { i, pos[1] - 1 });
                        // south?
                        if ((pos[1] < height - 1) && (img.getRGB(i, pos[1] + 1) != replacementColor))
                            queueNew.add(new int[] { i, pos[1] + 1 });
                    }

                    // update bounding box
                    if (extent[0] > west)
                        extent[0] = west;
                    if (extent[2] < east)
                        extent[2] = east;
                    if (extent[1] > pos[1])
                        extent[1] = pos[1];
                    if (extent[3] < pos[1])
                        extent[3] = pos[1];
                }
            }
            queue = queueNew;
        }

        return true;
    }
}
