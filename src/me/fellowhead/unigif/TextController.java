package me.fellowhead.unigif;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;

public class TextController
{
    public static EditorController ec;
    public static Stage stage;

    public ListView listView;
    public HBox listTools;
    public TextField textField;
    public ColorPicker colorPicker;
    public TextField startField;
    public TextField endField;
    public VBox itemTools;
    public TextField yField;
    public TextField xField;

    public static boolean isOpen = false;
    private int selectedFrame = 0;

    public void initialize()
    {
        listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int frame = newValue.intValue();
                if (frame < 0) { frame = 0; }
                onSelectFrame(frame);
            }
        });
        textField.setText("...");
        colorPicker.setValue(Color.WHITE);
        register(startField, 0);
        register(endField, 10);
        register(xField, 25);
        register(yField, 25);
        textField.textProperty().addListener((observable, oldValue, s) -> {
            if (selectedFrame >= 0)
            {
                getItems().get(selectedFrame).text = s;
                //updateListview();
            }
        });
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedFrame >= 0)
            {
                getItems().get(selectedFrame).color =
                        new java.awt.Color((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue());
            }
        });
        startField.textProperty().addListener((observable, oldValue, s) -> {
            if (repair(startField, s))
            {
                getItems().get(selectedFrame).start = Integer.parseInt(s);
            }
        });
        endField.textProperty().addListener((observable, oldValue, s) -> {
            if (repair(endField, s))
            {
                getItems().get(selectedFrame).end = Integer.parseInt(s);
            }
        });

        xField.textProperty().addListener((observable, oldValue, s) -> {
            if (repair(xField, s))
            {
                getItems().get(selectedFrame).x = Integer.parseInt(s);
            }
        });
        yField.textProperty().addListener((observable, oldValue, s) -> {
            if (repair(yField, s))
            {
                getItems().get(selectedFrame).y = Integer.parseInt(s);
            }
        });
        updateListview();
    }

    public boolean repair(TextField field, String s)
    {
        if (s.isEmpty())
        {
            return false;
        }
        if (!s.matches("\\d*")) {
            field.setText(s.replaceAll("[^\\d]", ""));
            return false;
        }
        if (s.contains("-"))
        {
            field.setText(s.replace("-", ""));
            return false;
        }
        return selectedFrame >= 0;
    }

    public void onSelectFrame(int index)
    {
        Main.print("Selecting " + index);
        Main.print("list: " + getItems().size());
        selectedFrame = index;
        TextItem ti = getItems().get(index);
        textField.setText(ti.text);
        colorPicker.setValue(ti.getFXColor());
        startField.setText("" + ti.getStart());
        endField.setText("" + ti.getEnd());
        xField.setText("" + ti.x);
        yField.setText("" + ti.y);

        itemTools.setDisable(false);
    }

    public ArrayList<TextItem> getItems()
    {
        return ec.texts;
    }

    private void register(TextField field, int def)
    {
        field.setText(def + "");
    }

    public void clickMoveUp(ActionEvent actionEvent) {
        if (selectedFrame > 0)
        {
            final int index = selectedFrame;
            final TextItem toMove = getItems().get(index);
            getItems().set(index, getItems().get(index - 1));
            getItems().set(index - 1, toMove);
            updateListview();
            select(index - 1);
        }
    }

    public void clickMoveDown(ActionEvent actionEvent) {
        if (selectedFrame < getItems().size() - 1)
        {
            final int index = selectedFrame;
            final TextItem toMove = getItems().get(index);
            getItems().set(index, getItems().get(index + 1));
            getItems().set(index + 1, toMove);
            updateListview();
            select(index + 1);
        }
    }

    public void updateListview()
    {
        listView.getItems().clear();
        for (int i = 0; i < getItems().size(); i++)
        {
            listView.getItems().add((i + 1) + ": \"" + getItems().get(i).text + "\"");
        }
        boolean disable = getItems().size() == 0;
        listTools.setDisable(disable);
        itemTools.setDisable(disable);
    }

    public void select(int index)
    {
        index = Math.max(0, index);
        listView.getSelectionModel().clearSelection();
        listView.getSelectionModel().select(index);
        listView.scrollTo(index);
    }

    public void clickDuplicate(ActionEvent actionEvent) {
        if (selectedFrame >= 0)
        {
            final int index = selectedFrame;
            getItems().add(index, getItems().get(index));
            updateListview();
            select(index + 1);
        }
    }

    public void clickRemove(ActionEvent actionEvent) {
        if (selectedFrame >= 0)
        {
            final int remove = selectedFrame;
            getItems().remove(remove);
            updateListview();
            if (remove >= getItems().size())
            {
                if (getItems().size() > 0)
                {
                    select(getItems().size() - 1);
                }
                else
                {
                    selectedFrame = -1;
                    Main.print("List is empty");
                }
            }
            else
            {
                select(remove);
            }
        }
    }

    public void clickAdd(ActionEvent actionEvent) {
        final int index = selectedFrame;
        getItems().add(index + 1, new TextItem("Sample text", Font.getFont("Arial"), java.awt.Color.white, 10, 10, 0, 10));
        updateListview();
        select(index);
    }
}