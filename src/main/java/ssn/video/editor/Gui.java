package ssn.video.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Gui extends Application {

    private MediaPlayer mediaPlayer;
    private Slider timeframeSlider;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FRAME FORGE");

        MediaView mediaView = new MediaView();

        // Control Buttons
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Button openButton = new Button("Open File");
        Button applyFilterButton = new Button("Apply Filter");
        
        // Filter ComboBox
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Gaussian Blur", "Brightness/Contrast", "Sepia", "Edge Detection");
        filterComboBox.setValue("Gaussian Blur");

        // Intensity Slider
        Label intensityLabel = new Label("Filter Intensity:");
        Slider intensitySlider = new Slider(0, 100, 50); // Intensity range 0-100
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setShowTickMarks(true);
        
        // Timeframe Slider
        Label timeframeLabel = new Label("Time Frame (Seconds):");
        timeframeSlider = new Slider(0, 60, 0); // Adjust the range based on video length
        timeframeSlider.setShowTickLabels(true);
        timeframeSlider.setShowTickMarks(true);
        
        // Set button actions
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
            // Implement filter application logic here
            System.out.println("Applying filter: " + selectedFilter + " with intensity: " + intensity + " for " + timeframe + " seconds.");
            // Example: applyFilter(selectedFilter, intensity, timeframe);
        });

        // Arrange buttons in a toolbar
        ToolBar toolBar = new ToolBar(openButton, playButton, pauseButton, stopButton, new Label("Filters:"), filterComboBox, applyFilterButton);

        // Layout for sliders
        VBox slidersBox = new VBox(
                toolBar,
                intensityLabel, intensitySlider,
                timeframeLabel, timeframeSlider
        );

        // Layout the GUI
        try {
            BorderPane root = new BorderPane();
            root.setCenter(mediaView);
            root.setBottom(slidersBox);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (MediaException es) {
            showErrorDialog("Error playing the video", es.getMessage());
        }
    }

    private void loadVideo(File file, MediaView mediaView) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose(); // Stop and release current media if playing
        }
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        
        // Set the timeframe slider max to the video duration when loaded
        mediaPlayer.setOnReady(() -> {
            timeframeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        });
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
