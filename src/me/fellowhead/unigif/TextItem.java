package me.fellowhead.unigif;

import javafx.scene.control.ListView;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TextItem {
    public String text;
    public Font font;
    public Color color;
    public int x;
    public int y;
    public int start;
    public int end;

    public TextItem(String text, Font font, Color color, int x, int y, int start, int end){
        this.text = text;
        this.font = font;
        this.color = color;
        this.x = x;
        this.y = y;
        this.start = start;
        this.end = end;
    }

    public TextItem(String text, String fontPath, int size, Float weight, Color color, int x, int y, int start, int end)
    {
        this.text = text;

        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
        } catch (Exception e) {
            e.printStackTrace();
            font = Font.getFont("Arial");
        }

        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FAMILY, font.getName());
        attributes.put(TextAttribute.WEIGHT, weight);
        attributes.put(TextAttribute.SIZE, size);
        this.font = new Font(attributes);
        this.color = color;
        this.x = x;
        this.y = y;
        this.start = start;
        this.end = end;
    }

    public boolean isVisible(int frame)
    {
        return frame >= start && frame <= end;
    }

    public javafx.scene.paint.Color getFXColor()
    {
        return javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getDuration() {
        return end - start;
    }
}