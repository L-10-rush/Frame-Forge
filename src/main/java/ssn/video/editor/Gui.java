package ssn.video.editor;

import ssn.video.features.VideoMerger;
import ssn.video.filters.GaussianBlur;

import java.util.*;

import java.io.File;

import javafx.scene.layout.Region;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    private List<String> videoFiles = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        

        primaryStage.setTitle("Video Editor");
        MediaView mediaView = new MediaView();
        BorderPane root = new BorderPane();
        
        // Top bar with centered title
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #eddbcd;");
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER);  // Center the content inside the HBox
        
        Label titleLabel = new Label("Frame Forge");
        titleLabel.setStyle("-fx-font-size: 18px;");
        topBar.getChildren().add(titleLabel);
        root.setTop(topBar);
        
        // Center layout for labels, also centered
        VBox centerLayout = new VBox();
        centerLayout.setPadding(new Insets(20));
        centerLayout.setSpacing(10);
        centerLayout.setAlignment(Pos.CENTER);

        // Control Buttons

        Image playIcon = new Image("https://github.com/L-10-rush/Frame-Forge/blob/b66117977e56f206ed96f21879fc189af7bcb840/src/main/resources/play.png",24, 24, true, true); 
        Image pauseIcon = new Image("https://github.com/L-10-rush/Frame-Forge/blob/b66117977e56f206ed96f21879fc189af7bcb840/src/main/resources/pause.png",24, 24, true, true);
        Image stopIcon = new Image("https://github.com/L-10-rush/Frame-Forge/blob/b66117977e56f206ed96f21879fc189af7bcb840/src/main/resources/stop.png",24, 24, true, true);
        Image openFileIcon = new Image("https://github.com/L-10-rush/Frame-Forge/blob/b66117977e56f206ed96f21879fc189af7bcb840/src/main/resources/open_file.png",24, 24, true, true);
        Image applyFilterIcon = new Image("https://github.com/L-10-rush/Frame-Forge/blob/b66117977e56f206ed96f21879fc189af7bcb840/src/main/resources/filter.png",24, 24, true, true);

        Button playButton = new Button();
        playButton.setGraphic(new ImageView(playIcon));

        Button pauseButton = new Button();
        pauseButton.setGraphic(new ImageView(pauseIcon));

        Button stopButton = new Button();
        stopButton.setGraphic(new ImageView(stopIcon));

        Button openButton = new Button();
        openButton.setGraphic(new ImageView(openFileIcon));

        Button applyFilterButton = new Button();
        applyFilterButton.setGraphic(new ImageView(applyFilterIcon));

        Button mergeButton = new Button("Merge");
        mergeButton.setVisible(true);


        

        // Arrange buttons in a horizontal box

        // Timestamp Label
        timestampLabel = new Label("Time: 00:00 / 00:00             ");
        timestampLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");

        // Filter ComboBox and current filter display
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("None", "Gaussian Blur", "Brightness/Contrast", "Sepia", "Edge Detection");
        filterComboBox.setValue("None");
        currentFilterLabel = new Label("Current Filter: None");
        currentFilterLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");

        // Create HBox for the main control buttons
        HBox controlButtonsBox = new HBox(10, playButton, pauseButton, stopButton);
        controlButtonsBox.setAlignment(Pos.CENTER);

        // Filter ComboBox and Apply Filter button (right-aligned)
        HBox filterOptionsBox = new HBox(15, new Button("Filters:"), applyFilterButton);
        filterOptionsBox.setAlignment(Pos.CENTER_RIGHT);

        // Merge button (right-aligned)
        HBox mergeButtonBox = new HBox(mergeButton);
        mergeButtonBox.setAlignment(Pos.CENTER_RIGHT);
        mergeButtonBox.setSpacing(20);
        mergeButtonBox.setPadding(new Insets(0, 10, 0, 0));
        

        // Open button and control buttons on the left
        HBox leftControlBox = new HBox(10, openButton, controlButtonsBox);
        leftControlBox.setAlignment(Pos.CENTER_LEFT);

        // Combine everything in the top-level HBox
        HBox topLevelControlBox = new HBox(10, leftControlBox, mergeButtonBox);
        topLevelControlBox.setAlignment(Pos.CENTER);

        // Set up the main layout using BorderPane
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topLevelControlBox); // Place the top-level control box
        mainLayout.setPadding(new Insets(10)); // Add padding for some spacing

            // Place everything in a main container (e.g., BorderPane)
        // Intensity Slider
        Label intensityLabel = new Label("Filter Intensity:");
        intensityLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
        Slider intensitySlider = new Slider(0, 100, 50); // Intensity range 0-100
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setShowTickMarks(true);

        // Timeframe Slider
        Label timeframeLabel = new Label("Time Frame (Seconds):");
        timeframeLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
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
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.mov"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                videoFiles.add(selectedFile.getAbsolutePath()); // Add to list for merging
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
            applyFilter(selectedFilter,intensity);
            // Apply filter logic goes here

        });

        mergeButton.setOnAction(e -> {
            if (videoFiles.isEmpty()) {
                showErrorDialog("Error", "No video files selected to merge.");
                return;
            }
            
            // Let the user choose the output file path for the merged video
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
            File outputFile = fileChooser.showSaveDialog(primaryStage);

            if (outputFile != null) {
                // Call the VideoMerger class to merge the videos
                try {
                    VideoMerger.mergeVideos(videoFiles.toArray(new String[0]), "/Users/vbaalaadityaa/Downloads/merg.mp4");
                    System.out.println("Videos merged successfully! Output file: " + outputFile.getAbsolutePath());
                } catch (Exception ex) {
                    showErrorDialog("Merge Failed", "An error occurred while merging the videos: " + ex.getMessage());
                }
            }
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

        root.setCenter(mediaView);
        root.setBottom(new VBox(slidersBox, frameTimeline));
        root.setStyle("-fx-background-color: #192231;");

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

    private void applyFilter(String filterName, double intensity) {
        if ("Gaussian Blur".equals(filterName)) {
            GaussianBlur blurFilter = new GaussianBlur();
            blurFilter.applyFilter(videoFiles.get(0));
        }
        // Add other filters as needed
        System.out.println("Applied filter: " + filterName + " with intensity: " + intensity);
    }

    private void populateFrameTimeline(int totalSeconds) {
        frameTimeline.getChildren().clear();
        for (int i = 0; i < totalSeconds; i++) {
            Label frameLabel = new Label(Integer.toString(i));
            frameLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 5;");
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