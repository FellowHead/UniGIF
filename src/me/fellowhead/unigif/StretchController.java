package me.fellowhead.unigif;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StretchController
{
    public static EditorController ec;
    public static Stage stage;

    public TextField durationField;
    public Label fpsText;

    public void initialize()
    {
        durationField.textProperty().addListener((observable, oldValue, s) -> {
            if (s.isEmpty())
            {
                return;
            }
            if (!s.matches("\\d*")) {
                durationField.setText(s.replaceAll("[^\\d]", ""));
                return;
            }
            if (s.contains("-"))
            {
                durationField.setText(s.replace("-", ""));
                return;
            }
            fpsText.setText("= " + (Math.round(1000f / Integer.parseInt(s)) / 10f) + " fps");
        });
        durationField.setText("4");
    }

    public void finish()
    {
        ec.onImport(0);
        stage.close();
    }

    public void clickOK(ActionEvent actionEvent) {
        ec.stretch(Float.parseFloat(durationField.getText()));
        finish();
    }
}