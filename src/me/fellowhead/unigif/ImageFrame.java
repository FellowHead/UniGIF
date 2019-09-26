package me.fellowhead.unigif;

import java.awt.image.BufferedImage;
public class ImageFrame {
    private int delay;
    private final BufferedImage image;
    private final String disposal;
    private final int width, height;
    private final String name;

    public ImageFrame (BufferedImage image, int delay, String disposal, int width, int height, String name){
        this.image = image;
        this.delay = delay;
        this.disposal = disposal;
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public ImageFrame (BufferedImage image, String name, int delay){
        this.image = image;
        this.delay = delay;
        this.disposal = null;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setDelay(int delay)
    {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public String getDisposal() {
        return disposal;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getName()
    {
        return name;
    }
}