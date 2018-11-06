package app;

import app.serial.SerialListener;
import app.video.Animations;
import processing.core.PApplet;


public class App {

    public static void main(String[] args) {
        final SerialListener serialListener = new SerialListener();
        final Animations animations = new Animations();
        final String[] processingArgs = {"MySketch"};

        animations.setup();
        serialListener.initialize();
        PApplet.runSketch(processingArgs, animations);

        new Thread(() -> {
            float volume = 0f;
            while (true){
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
