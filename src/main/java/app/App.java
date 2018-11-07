package app;

import app.serial.SelectCOMPort;
import app.serial.SerialListener;
import app.video.Animations;
import processing.core.PApplet;

public class App {

    public static volatile String PORT_NAME = null;

    public static void main(String[] args) {
        final Thread thread = new Thread(() -> {
            final SelectCOMPort selectCOMPort = new SelectCOMPort();
            PApplet.runSketch(new String[]{"Port Selector"}, selectCOMPort);
        });
        thread.run();
        while (PORT_NAME == null) ;
        System.out.println("Received " + PORT_NAME);
        thread.interrupt();
        final SerialListener serialListener = new SerialListener(PORT_NAME);
        final Animations animations = new Animations();
        final String[] processingArgs = {"MySketch"};
//
//        animations.setup();
        serialListener.initialize();
        PApplet.runSketch(processingArgs, animations);
//
        new Thread(() -> {
            float volume = 0f;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (volume != serialListener.getNewVolume()) {
                    volume = serialListener.getNewVolume();
                    animations.setVolume(volume);
                }
            }
        }).run();
    }
}

