package me.fellowhead.unigif;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AboutController
{
    public static Stage s;

    public ImageView img;
    public Label textApp;
    public AnchorPane maxPane;

    public void initialize()
    {
        img.setImage(new Image(Main.class.getResourceAsStream("resources/icon.png"), img.getFitWidth(), img.getFitHeight(), true, false));
        s.setWidth(maxPane.getPrefWidth());
        s.setHeight(maxPane.getPrefHeight() + 50);
        textApp.setText(Main.appName + " " + Main.version);
    }

    public void clickLinkWriter(MouseEvent actionEvent) {
        Main.openUri("http://elliot.kroo.net/software/java/GifSequenceWriter/");
    }

    public void clickLinkReader(MouseEvent actionEvent) {
        Main.openUri("https://stackoverflow.com/a/17269591");
    }
}