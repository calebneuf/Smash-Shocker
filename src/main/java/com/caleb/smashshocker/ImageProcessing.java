package com.caleb.smashshocker;

import net.sourceforge.tess4j.util.ImageHelper;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class ImageProcessing {

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage gaussianNoise(BufferedImage image) {
        Raster source = image.getRaster ();
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster out = output.getRaster ();
        double stdDev = 10.0;
        int currVal;// the current value
        double newVal;// the new "noisy" value
        double gaussian;// gaussian number
        int bands = out.getNumBands ();// number of bands
        int width = image.getWidth ();// width of the image
        int height = image.getHeight ();// height of the image
        java.util.Random randGen = new java.util.Random ();
        for (int j = 0;j < height;j ++) {
            for (int i = 0;i < width;i ++) {
                gaussian = randGen.nextGaussian ();
                for (int b = 0;b < bands;b ++) {
                    newVal = stdDev * gaussian;
                    currVal = source.getSample (i, j, b);
                    newVal = newVal + currVal;
                    if (newVal < 0) newVal = 0.0;
                    if (newVal > 255) newVal = 255.0;
                    out.setSample (i, j, b, (int) (newVal));
                }
            }
        }
        return output;
    }


    public static BufferedImage scale(BufferedImage source, double scale, boolean bilinearFiltering){
        try{
            BufferedImage destination = new BufferedImage((int)(source.getWidth() * scale), (int)(source.getHeight() * scale), source.getType());
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, getInterpolationType(bilinearFiltering));
            return scaleOp.filter(source, destination);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getInterpolationType(boolean bilinearFiltering){
        return bilinearFiltering ? AffineTransformOp.TYPE_BILINEAR : AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
    }

    public static BufferedImage applyFilters(BufferedImage image) {
        BufferedImage newImage = ImageHelper.convertImageToGrayscale(image);
        gaussianNoise(newImage);
        binarize(newImage);
        return newImage;
    }


    private static void binarize(BufferedImage image) {
        for (int i = 0; i < image.getWidth(); i++)
            for (int j = 0; j < image.getHeight(); j++)
                image.setRGB(i, j, gamma(image.getRGB(i, j)) > 127 ? Color.white.getRGB() : Color.black.getRGB());
    }



    private static int gamma(int rgb) {
        return (red(rgb) + green(rgb) + blue(rgb)) / 3;
    }

    private static int red(int rgb) {
        return (rgb >> 16) & 0x000000FF;
    }

    private static int green(int rgb) {
        return (rgb >> 8) & 0x000000FF;
    }

    private static int blue(int rgb) {
        return (rgb) & 0x000000FF;
    }



}
