package com.caleb.smashshocker;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException, IOException {
        SerialPort sp = SerialPort.getCommPort("COM3"); // device name TODO: must be changed
        sp.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written

        if (sp.openPort()) {
            System.out.println("Port is open :)");
        } else {
            System.out.println("Failed to open port :(");
            return;
        }
        Thread.sleep(5000);

        for(int j = 0; j < 20; j++) {
            for (Integer i = 1; i < 5; ++i) {
                sp.getOutputStream().write(i.byteValue());
                sp.getOutputStream().flush();
                System.out.println("Sent number: " + i);
                Thread.sleep(250);
            }
        }


        if (sp.closePort()) {
            System.out.println("Port is closed :)");
        } else {
            System.out.println("Failed to close port :(");
            return;
        }


    }

}
