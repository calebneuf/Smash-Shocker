package com.caleb.smashshocker;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class ArduinoController {

    SerialPort sp = null;

    public ArduinoController() {
        sp = SerialPort.getCommPort("COM3"); // device name TODO: must be changed
        sp.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        //sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written
    }

    public boolean openPort() {
        if (sp.openPort()) {
            System.out.println("Port is open :)");
            return true;
        } else {
            System.out.println("Failed to open port :(");
            return false;
        }
    }

    public void sendChar(Integer i) throws IOException {
        sp.getOutputStream().write(i.byteValue());
        sp.getOutputStream().flush();
        System.out.println("Sent number: " + i);
    }

    public void closePort() {
        if (sp.closePort()) {
            System.out.println("Port is closed :)");
        } else {
            System.out.println("Failed to close port :(");
        }
    }
}
