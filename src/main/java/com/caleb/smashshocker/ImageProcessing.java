package com.caleb.smashshocker;

import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public class ImageProcessing {

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }



}
