package app.serial;

import gnu.io.CommPortIdentifier;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static app.App.PORT_NAME;


public class SelectCOMPort extends PApplet {

    private final int off;
    private final int on;
    boolean first = true;
    private final List<String> stringList = new ArrayList<>();

    public SelectCOMPort() {
        final Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            stringList.add(((CommPortIdentifier) portEnum.nextElement()).getName());
            portEnum.nextElement();
        }
        noLoop();
        off = this.color(4, 79, 111);
        on = this.color(84, 145, 158);
    }

    @Override
    public void settings() {
        size(1000, (1000));
    }

    @Override
    public void mousePressed() {
        float pin = (mouseY / 30f) - 1;
        if (pin < stringList.size()) {
            PORT_NAME = stringList.get(Math.round(pin));
            System.out.printf("Selected : %s\n", PORT_NAME);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void draw() {
        background(off);
        stroke(on);

        int i = 0;
        while (i < stringList.size()) {
            fill(on);
            if (first) {
                System.out.printf("Y : %d AND name is %s\n", 30 + i * 30, stringList.get(i));
            }
            text(stringList.get(i), 420, 30 + i++ * 30);
        }
        first = false;
    }

}
