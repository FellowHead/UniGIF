package me.fellowhead.unigif;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorController {
    @FXML
    private ToggleButton playToggle;
    @FXML
    private Menu editMenu;
    @FXML
    private Label fpsText;
    @FXML
    private HBox frameTools;
    @FXML
    private TextField fieldDuration;
    @FXML
    private CheckBox toggleMirror;
    @FXML
    private Label frameText;
    @FXML
    private TextField fieldSkip;
    @FXML
    private Label infoText;
    @FXML
    private Pane imgParent;
    @FXML
    private ImageView imgView;
    @FXML
    private ListView listView;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private MenuItem exportButton;
    @FXML
    private ColorPicker backgroundPicker;
    @FXML
    private TextField fieldSizeX;
    @FXML
    private TextField fieldSizeY;

    private Timeline timeline;
    public ArrayList<ImageFrame> allFrames = new ArrayList<>();
    public int selectedFrame = -1;
    private static String defaultInfo = Main.appName + " " + Main.version;
    private static File dirImport = null;
    private static File dirExport = null;
    private static int lsWidth = 10;
    private static int lsHeight = 10;
    public ArrayList<TextItem> texts;
    public static EditorController main;

    public static int getLsWidth() {
        return lsWidth;
    }

    public static int getLsHeight() {
        return lsHeight;
    }

    public static void setLsWidth(int lsWidth) {
        EditorController.lsWidth = lsWidth;
        main.fieldSizeX.setText(lsWidth + "");
    }

    public static void setLsHeight(int lsHeight) {
        EditorController.lsHeight = lsHeight;
        main.fieldSizeY.setText(lsHeight + "");
    }

    public BufferedImage getProcessedFrame(int index) {
        BufferedImage frame = allFrames.get(index).getImage();
        BufferedImage img = new BufferedImage(lsWidth, lsHeight, frame.getType());
        Graphics g = img.getGraphics();
        g.setColor(Main.paintToAwt(backgroundPicker.getValue()));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(frame, 0, 0, null);

        for (TextItem item : texts) {
            if (item.isVisible(index + 1)) {
                g.setColor(item.color);
                g.setFont(item.font);
                g.drawString(item.text, item.x, item.y);
            }
        }

        return img;
    }

    public void initialize() {
        main = this;
        imgView.fitWidthProperty().bind(imgParent.widthProperty());
        imgView.fitHeightProperty().bind(imgParent.heightProperty());
        fieldSkip.setText("1");
        infoText.setText(defaultInfo);
        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            //String name = newValue + "";
            int frame = newValue.intValue();
            if (frame < 0) {
                frame = 0;
            }
            //int frame = Integer.parseInt(name.substring(0, name.indexOf(":"))) - 1;
            //Main.print("Selecting frame " + frame);
            onSelectFrame(frame);
        });
        register(fieldDuration, "4", () -> {
            fpsText.setText("= " + (Math.round(1000f / Integer.parseInt(fieldDuration.getText())) / 10f) + " fps");
            rebuildTimeline();
        });
        register(fieldSizeX, "400", () -> {
            lsWidth = Integer.parseInt(fieldSizeX.getText());
        });
        register(fieldSizeY, "400", () -> {
            lsHeight = Integer.parseInt(fieldSizeY.getText());
        });
        toggleMirror.selectedProperty().addListener((observable, oldValue, newValue) -> rebuildTimeline());
        playToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rebuildTimeline();
                playToggle.setDisable(true);
            }
        });

        resetImage();

        texts = new ArrayList<>();
        //texts.add(new TextItem("Test", "C:/tmp/Rockwell.ttf", 20, TextAttribute.WEIGHT_REGULAR, Color.white, 10, 50, 3, 7));

//        ImageFrame[] frames = new ImageFrame[0];
//        try {
//            frames = Main.readGif("C:/tmp/giphy.gif");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (ImageFrame frame : frames) {
//            addFrame(frame, false);
//        }

        rebuildTimeline();
        updateListview();
    }

    private void register(TextField field, String def, Runnable r) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (repair(field, newValue))
            {
                r.run();
            }
        });
//        field.setOnAction(event -> {
//
//        });
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                repair(field, field.getText());
                if (field.getText().isEmpty()) {
                    field.setText(def);
                }
            }
        });
        field.setText(def);
        r.run();
    }

//    void easyRepair(TextField field, String s) {
//
//    }

    public boolean repair(TextField field, String s) {
        if (s.isEmpty()) {
            return false;
        }
        if (!s.matches("\\d*")) {
            field.setText(s.replaceAll("[^\\d]", ""));
            return false;
        }
        if (s.contains("-")) {
            field.setText(s.replace("-", ""));
            return false;
        }
        if (Integer.parseInt(s) <= 0) {

        }
        return true;
    }

    public void onSelectFrame(int index) {
        if (index < allFrames.size()) {
            selectedFrame = index;
            timeline.stop();
            playToggle.setSelected(false);
            playToggle.setDisable(false);
            setImageFrame(index);
        }
        frameTools.setDisable(false);
    }

    public int getFrameStep() {
        return 1;
    }

    public void stretch(float nRate) {
        float mult = nRate / getGlobalDuration();
        Main.print("Stretching " + mult);
        ArrayList<ImageFrame> nFrames = new ArrayList<>();
        for (float i = 0; i < allFrames.size(); i += mult) {
            nFrames.add(allFrames.get((int) i));
        }
        allFrames = new ArrayList<>(nFrames);
        selectedFrame /= mult;
        setGlobalDuration(Math.round(nRate));
    }

    public void setImageFrame(int frame) {
        imgView.setImage(SwingFXUtils.toFXImage(getProcessedFrame(frame), null));
        frameText.setText("Frame " + (frame + 1));
    }

    public void setCurrentImage(BufferedImage img, String text) {
        imgView.setImage(SwingFXUtils.toFXImage(img, null));
        frameText.setText(text);
    }

    public void setGlobalDuration(int i) {
        fieldDuration.setText(i + "");
    }

    public int getGlobalDuration() {
        try {
            return Integer.parseInt(fieldDuration.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return 4;
        }
    }

//    public int getLoopCount()
//    {
//        return Integer.parseInt(fieldLoops.getText());
//    }

    public void rebuildTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline();
        int offset = 0;
        for (int i = 0; i < allFrames.size(); i += getFrameStep()) {
            final int index = i;
            EventHandler<ActionEvent> handler = event -> setImageFrame(index);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(offset * 10), handler, (KeyValue[]) null));
            offset += getGlobalDuration() * getFrameStep();
        }
        timeline.setAutoReverse(doMirror());
        timeline.setRate(1);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        playToggle.setSelected(true);
        playToggle.setDisable(true);
    }

    public void addFrame(ImageFrame frame, boolean updateView) {
        allFrames.add(selectedFrame + 1, frame);
        selectedFrame++;
        //listView.getItems().add((allFrames.size()) + ": " + frame.getName());
        if (updateView) {
            updateListview();
        }
    }

    public void onImport(int frame) {
        updateListview();
        select(frame);
        rebuildTimeline();
    }

    public boolean doMirror() {
        return toggleMirror.isSelected();
    }

    private ArrayList<BufferedImage> getProcessedFrames() {
        ArrayList<BufferedImage> output = new ArrayList<>();
        for (int i = 0; i < allFrames.size(); i++) {
            output.add(getProcessedFrame(i));
        }
        if (doMirror()) {
            for (int i = allFrames.size() - 2; i > 0; i--) {
                output.add(getProcessedFrame(i));
            }
        }
        return output;
    }

    private Task createExportWorker(ImageOutputStream out) {
        return new Task<Boolean>() {
            final int frameStep = getFrameStep();

            @Override
            protected Boolean call() throws Exception {
                ArrayList<BufferedImage> frames = getProcessedFrames();
                final int N_ITERATIONS = frames.size();
                final int msFrames = ((getGlobalDuration() * 10) * frameStep);
                Main.print("Time between frames ms: " + msFrames);
                GifSequenceWriter writer = new GifSequenceWriter(out,
                        BufferedImage.TYPE_INT_ARGB, msFrames, true);
                //writer.writeToSequence(firstFrame.getImage());
                for (int i = 0; i < N_ITERATIONS; i += frameStep) {
                    //Main.print("Writing frame " + i + " - " + allFrames.get(i).getImage().getType());
                    writer.writeToSequence(frames.get(i));
                    updateMessage((i + 1) + "/" + N_ITERATIONS);
                    updateProgress(i + 1, N_ITERATIONS);
                }
                writer.close();
                out.close();
                return true;
            }
        };
    }

    public void export(File file) {
        //File file = new File("C:/tmp/unigif.gif");
        if (file.exists()) {
            Main.print("Deleting");
            file.delete();
        }
        FileImageOutputStream out = null;
        try {
            out = new FileImageOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Task task = createExportWorker(out);

        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.progressProperty().bind(
                task.progressProperty()
        );
        timeline.pause();
//        task.progressProperty().addListener((observable, oldValue, newValue) ->
//        {
//            setCurrentImage(allFrames.get(newValue.intValue()).getImage());
//        });
        infoText.textProperty().unbind();
        infoText.textProperty().bind(task.messageProperty());
        new Thread(task).start();
        task.setOnSucceeded(event ->
        {
            infoText.textProperty().unbind();
            infoText.setText("Exported to " + file.getPath());
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            Main.print("Done");

            timeline.play();
        });
    }

    public void optimizeChooser(FileChooser fc, boolean export) {
        if (!export && dirImport != null) {
            fc.setInitialDirectory(dirImport);
        } else if (export && dirExport != null) {
            fc.setInitialDirectory(dirExport);
        }
    }

    public void clickExport(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        optimizeChooser(chooser, true);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GIF", "*.gif"));
        File returned = chooser.showSaveDialog(Main.mainStage);
        if (returned != null) {
            dirExport = returned.getParentFile();
            export(returned);
        }
    }

    public void showInfoInsert() {
        infoText.setText("Note that frames will always be inserted below the selected frame");
    }

    public void showInfoDefault() {
        infoText.setText(defaultInfo);
    }

    public void clickAddFramesImages(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        optimizeChooser(chooser, false);
        chooser.setTitle("Import images");
        //chooser.setFileFilter();
        //chooser.setVisible(true);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.bmp"));
        List<File> returned = chooser.showOpenMultipleDialog(Main.mainStage);
        if (returned != null) {
            dirImport = returned.get(0).getParentFile();
            Stage stage = new Stage();
            ImportController.setInfos(returned.toArray(new File[0]), this, stage, true);
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("resources/importsimple.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setTitle("Importing...");
            stage.initStyle(StageStyle.UTILITY);
            Scene scene = Main.initScene(new Scene(root, 200, 40));
            stage.setResizable(false);
            stage.initOwner(Main.mainStage);
            stage.setScene(scene);
            stage.show();
        }
    }

    public void clickAddFramesGIF(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        optimizeChooser(chooser, false);
        chooser.setTitle("Import GIF");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GIFs", "*.gif"));
        File returned = chooser.showOpenDialog(Main.mainStage);
        if (returned != null) {
            dirImport = returned.getParentFile();
            Stage stage = new Stage();
            ImportController.setInfos(new File[]{returned}, this, stage, false);
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("resources/import.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Import settings");
            Scene scene = Main.initScene(new Scene(root, 250, 80));
            stage.setResizable(false);
            stage.initOwner(Main.mainStage);
            stage.setScene(scene);
            if (allFrames.size() > 0) {
                stage.show();
            }
        }
    }

    public void updateListview() {
        listView.getItems().clear();
        for (int i = 0; i < allFrames.size(); i++) {
            listView.getItems().add((i + 1) + ": " + allFrames.get(i).getName());
        }
        boolean disable = allFrames.size() == 0;
        editMenu.setDisable(disable);
        frameTools.setDisable(disable);
        exportButton.setDisable(disable);
    }

    public void select(int index) {
        index = Math.max(0, index);
        Main.print("i: " + index);
        listView.getSelectionModel().clearSelection();
        listView.getSelectionModel().select(index);
        listView.scrollTo(index);
    }

    public void clickApply(ActionEvent actionEvent) {
        Main.print("lolz");
    }

    public void clickRemove(ActionEvent actionEvent) {
        if (selectedFrame >= 0) {
            final int remove = selectedFrame;
            allFrames.remove(remove);
            updateListview();
            if (remove >= allFrames.size()) {
                if (allFrames.size() > 0) {
                    select(allFrames.size() - 1);
                } else {
                    selectedFrame = -1;
                    resetImage();
                    Main.print("List is empty");
                }
            } else {
                select(remove);
            }
        }
    }

    private void resetImage() {
        setCurrentImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), "");
    }

    public void clickPlay(ActionEvent actionEvent) {
        rebuildTimeline();
    }

    public void clickMoveUp(ActionEvent actionEvent) {
        if (selectedFrame > 0) {
            final int index = selectedFrame;
            final ImageFrame toMove = allFrames.get(index);
            allFrames.set(index, allFrames.get(index - 1));
            allFrames.set(index - 1, toMove);
            updateListview();
            select(index - 1);
        }
    }

    public void clickMoveDown(ActionEvent actionEvent) {
        if (selectedFrame < allFrames.size() - 1) {
            final int index = selectedFrame;
            final ImageFrame toMove = allFrames.get(index);
            allFrames.set(index, allFrames.get(index + 1));
            allFrames.set(index + 1, toMove);
            updateListview();
            select(index + 1);
        }
    }

    public void clickSave(ActionEvent actionEvent) {
        if (selectedFrame >= 0) {
            FileChooser chooser = new FileChooser();
            optimizeChooser(chooser, true);
            chooser.setTitle("Save frame as image");
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png"));
            File save = chooser.showSaveDialog(Main.mainStage);
            if (save != null) {
                dirExport = save.getParentFile();

                try {
                    ImageIO.write(allFrames.get(selectedFrame).getImage(), "PNG", save);
                    infoText.setText("Saved frame to " + save.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    infoText.setText("Couldn't save frame: " + e.getMessage());
                }
            }
        }
    }

    public void clickDuplicate(ActionEvent actionEvent) {
        if (selectedFrame >= 0) {
            final int index = selectedFrame;
            allFrames.add(index, allFrames.get(index));
            updateListview();
            select(index + 1);
        }
    }

    public void clickAbout(ActionEvent actionEvent) {
        Stage stage = new Stage();
        AboutController.s = stage;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("resources/about.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.setTitle("About");
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = Main.initScene(new Scene(root, 100, 100));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.initOwner(Main.mainStage);
        stage.show();
    }

    public void clickReverseFrames(ActionEvent actionEvent) {
        final int index = selectedFrame;
        Collections.reverse(allFrames);
        updateListview();
        Platform.runLater(() -> {
            select(allFrames.size() - index - 1);
        });

    }

    public void clickStretch(ActionEvent actionEvent) {
        Stage stage = new Stage();
        StretchController.stage = stage;
        StretchController.ec = this;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("resources/stretch.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.setTitle("Stretch frames");
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = Main.initScene(new Scene(root, 170, 90));
        stage.initOwner(Main.mainStage);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public void openTextEditor(ActionEvent actionEvent) {
        if (!TextController.isOpen) {
            Stage stage = new Stage();
            TextController.stage = stage;
            TextController.ec = this;
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("resources/texteditor.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.setTitle("Text item editor");
            stage.initStyle(StageStyle.DECORATED);
            Scene scene = Main.initScene(new Scene(root, 300, 300));
            stage.initOwner(Main.mainStage);
            stage.setResizable(true);
            stage.setScene(scene);
            TextController.isOpen = true;
            stage.setOnHidden(event -> {
                Main.print("lol");
                TextController.isOpen = false;
            });
            stage.show();
        }
    }
}