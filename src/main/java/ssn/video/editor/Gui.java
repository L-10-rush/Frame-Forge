package ssn.video.editor;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Gui extends Application {

    private MediaPlayer mediaPlayer;
    private Slider timeframeSlider;
    private Label timestampLabel;
    private Label currentFilterLabel;
    private HBox frameTimeline;
    private Line currentFrameLine;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FRAME FORGE - Video Editor");

        MediaView mediaView = new MediaView();

        // Control Buttons
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Button openButton = new Button("Open File");
        Button applyFilterButton = new Button("Apply Filter");

        // Timestamp Label
        timestampLabel = new Label("Time: 00:00 / 00:00");

        // Filter ComboBox and current filter display
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Gaussian Blur", "Brightness/Contrast", "Sepia", "Edge Detection");
        filterComboBox.setValue("Gaussian Blur");
        currentFilterLabel = new Label("Current Filter: None");

        // Intensity Slider
        Label intensityLabel = new Label("Filter Intensity:");
        Slider intensitySlider = new Slider(0, 100, 50); // Intensity range 0-100
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setShowTickMarks(true);

        // Timeframe Slider
        Label timeframeLabel = new Label("Time Frame (Seconds):");
        timeframeSlider = new Slider(0, 60, 0); // Adjust range based on video length
        timeframeSlider.setShowTickLabels(true);
        timeframeSlider.setShowTickMarks(true);

        // Frame Timeline HBox
        frameTimeline = new HBox();
        frameTimeline.setSpacing(2);
        frameTimeline.setStyle("-fx-border-color: gray; -fx-padding: 5;");
        
        // Blue line to indicate the current frame
        currentFrameLine = new Line(0, 0, 0, 100);
        currentFrameLine.setStroke(Color.BLUE);
        currentFrameLine.setStrokeWidth(2);

        // Button actions
        playButton.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });

        pauseButton.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });

        stopButton.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
        });

        openButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.mov")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                try {
                    loadVideo(selectedFile, mediaView);
                } catch (MediaException es) {
                    showErrorDialog("Error loading the video", es.getMessage());
                }
            }
        });

        applyFilterButton.setOnAction(e -> {
            String selectedFilter = filterComboBox.getValue();
            double intensity = intensitySlider.getValue();
            double timeframe = timeframeSlider.getValue();
            currentFilterLabel.setText("Current Filter: " + selectedFilter + " (Intensity: " + (int)intensity + ")");
            System.out.println("Applying filter: " + selectedFilter + " with intensity: " + intensity + " for " + timeframe + " seconds.");
            // Apply filter logic goes here
        });

        // Layout for control buttons and filter options
        ToolBar toolBar = new ToolBar(
                openButton, playButton, pauseButton, stopButton,
                new Label("Filters:"), filterComboBox, applyFilterButton
        );

        VBox slidersBox = new VBox(
                toolBar,
                new HBox(intensityLabel, intensitySlider),
                new HBox(timeframeLabel, timeframeSlider),
                new HBox(timestampLabel, currentFilterLabel)
        );

        BorderPane root = new BorderPane();
        root.setCenter(mediaView);
        root.setBottom(new VBox(slidersBox, frameTimeline));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadVideo(File file, MediaView mediaView) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose(); // Release current media if playing
        }
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.setOnReady(() -> {
            double duration = mediaPlayer.getTotalDuration().toSeconds();
            timeframeSlider.setMax(duration);
            updateTimestampDisplay(0, duration);
            populateFrameTimeline((int) duration);
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double current = newTime.toSeconds();
            double total = mediaPlayer.getTotalDuration().toSeconds();
            updateTimestampDisplay(current, total);
            timeframeSlider.setValue(current);
            updateCurrentFramePosition(current, total);
        });

        timeframeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeframeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });
    }

    private void populateFrameTimeline(int totalSeconds) {
        frameTimeline.getChildren().clear();
        for (int i = 0; i < totalSeconds; i++) {
            Label frameLabel = new Label(Integer.toString(i));
            frameLabel.setStyle("-fx-background-color: blue; -fx-padding: 5;");
            frameTimeline.getChildren().add(frameLabel);
        }
        frameTimeline.getChildren().add(currentFrameLine);
    }

    private void updateCurrentFramePosition(double currentSeconds, double totalSeconds) {
        double proportion = currentSeconds / totalSeconds;
        double position = proportion * frameTimeline.getWidth();
        currentFrameLine.setLayoutX(position);
    }

    private void updateTimestampDisplay(double currentSeconds, double totalSeconds) {
        String currentTime = formatTime(currentSeconds);
        String totalTime = formatTime(totalSeconds);
        timestampLabel.setText("Time: " + currentTime + " / " + totalTime);
    }

    private String formatTime(double seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}