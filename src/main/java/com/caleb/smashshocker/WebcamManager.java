package com.caleb.smashshocker;

import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WebcamManager {

    private static WebcamManager instance;

    public static WebcamManager getInstance() {
        if(instance == null)
            instance = new WebcamManager();
        return instance;
    }

    private Webcam webcam;

    public boolean init() {
        try {
            for(Webcam webcams : Webcam.getWebcams()) {
                if(webcams.getName().contains("HD60")) {
                    webcam = webcams;
                    break;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("No capture card found");
            return false;
        }

        if(webcam == null) {
            System.out.println("No capture card found");
            return false;
        }

        System.out.println("Capture card found: " + webcam.getName());

        webcam.open();
        return true;
    }


    /**
     * Get current frame from capture card
     * @return current frame
     * @throws Exception if capture card is not found
     */
    public BufferedImage takeScreenShot() throws Exception {

        if(webcam != null) {
            return webcam.getImage();
        }

        Rectangle screenRect = new Rectangle(0, 0, 0, 0);
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            screenRect = gd.getDefaultConfiguration().getBounds();
        }
        BufferedImage capture = new Robot().createScreenCapture(screenRect);
        if(capture.getWidth() != 1920 || capture.getHeight() != 1080) {
            capture = ImageProcessing.resizeImage(capture, 1920, 1920);
        }
        return capture;
    }


}
