package me.fellowhead.unigif;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImportController {
    public ProgressBar progressBar;
    //public TextArea fieldDuration;
    private static File[] files;
    private static EditorController ec;
    private static double progress = 0;
    private static Stage stage;
    public ChoiceBox choiceBox;
    private static boolean simple;

    private static final int IMP_OVERWRITE_SPEED = 0;
    private static final int IMP_STRETCH_OLD = 1;
    private static final int IMP_STRETCH_NEW = 2;
    private static final int IMP_ONLY_IMPORT = 3;
    private int frame = 0;
    //private boolean overwriteLsd = true;

    private static final Object[] options = new String[]
            {
                    "Overwrite current speed",
                    "Stretch current frames",
                    "Stretch new frames",
                    "No stretching"
            };

    public static void setInfos(File[] files, EditorController ec, Stage stage, boolean simple) {
        ImportController.files = files;
        ImportController.ec = ec;
        ImportController.stage = stage;
        ImportController.simple = simple;
    }

    public void initialize() {
        if (!simple) {
            choiceBox.getItems().setAll(options);
            if (ec.allFrames.size() == 0) {
                choiceBox.getSelectionModel().select(IMP_OVERWRITE_SPEED);
                Platform.runLater(() ->
                {
                    clickOK(null);
                });
            } else {
                choiceBox.getSelectionModel().select(IMP_STRETCH_NEW);
            }
        } else {
            importSimple();
        }
    }

    private void updateLSD(BufferedImage img) {
        EditorController.setLsWidth(img.getWidth());
        EditorController.setLsHeight(img.getHeight());
//        if (img.getWidth() > EditorController.getLsWidth()) {
//            EditorController.setLsWidth(img.getWidth());
//        }
//        if (img.getHeight() > EditorController.getLsHeight()) {
//            EditorController.setLsHeight(img.getHeight());
//        }
    }

    private Task createWorker() {
        return new Task<Boolean>() {
            final int N_ITERATIONS = files.length;
            final int index = ec.selectedFrame + 1;

            @Override
            protected Boolean call() throws Exception {
                for (int i = 0; i < N_ITERATIONS; i++) {
                    try {
                        BufferedImage img = convertImageType(ImageIO.read(files[i]));
                        updateLSD(img);
                        ec.addFrame(new ImageFrame(img, files[i].getName(), -1), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateProgress(i + 1, N_ITERATIONS);
                }
                return true;
            }
        };
    }

    private BufferedImage convertImageType(BufferedImage input) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        output.getGraphics().drawImage(input, 0, 0, null);
        return output;
    }

    private void importSimple() {
        //fieldDuration.setEditable(false);
        frame = ec.selectedFrame;
        Task task = createWorker();

        progressBar.setProgress(0);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(
                task.progressProperty()
        );
        new Thread(task).start();
        task.setOnSucceeded(event ->
        {
            finish();
        });
    }

    private void finish() {
        ec.onImport(frame);
        stage.close();
    }

    public void clickOK(ActionEvent actionEvent) {
        choiceBox.setDisable(true);
        frame = ec.selectedFrame;
        try {
            ImageFrame[] frames = Main.readGif(files[0].getPath());
            for (ImageFrame frame : frames) {
                updateLSD(frame.getImage());
            }
            int imp = choiceBox.getSelectionModel().getSelectedIndex();
            Main.print(imp + " selected");
            float avg = 0;
            if (imp != IMP_ONLY_IMPORT) { //Calculate average delay
                for (ImageFrame frame : frames) {
                    avg += frame.getDelay();
                }
                avg /= frames.length;
                Main.print("average delay: " + avg);
            }
            if (imp == IMP_OVERWRITE_SPEED) {
                ec.setGlobalDuration(Math.round(avg));
            } else if (imp == IMP_STRETCH_NEW) {
                for (float i = 0; i < frames.length; i += (ec.getGlobalDuration() / avg)) {
                    ec.addFrame(frames[(int) i], false);
                }
                finish();
                return;
            } else if (imp == IMP_STRETCH_OLD) {
                ec.stretch(avg);
            }

            for (ImageFrame frame : frames) {
                ec.addFrame(frame, false);
            }
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}