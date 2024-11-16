package ssn.video.editor;

import ssn.video.features.VideoMerger;
import ssn.video.filters.GaussianBlur;

import java.util.*;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.ByteArrayInputStream;
import java.io.File;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyEvent;

import javafx.scene.input.MouseEvent;

public class GuiCopy extends Application {

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
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
        mediaView = new MediaView();
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

        Image playIcon = new Image("file:/Users/vbaalaadityaa/Downloads/MiniProject/src/main/resources/icons/play.png",24, 24, true, true); 
        Image pauseIcon = new Image("file:/Users/vbaalaadityaa/Downloads/MiniProject/src/main/resources/icons/pause.png",24, 24, true, true);
        Image stopIcon = new Image("file:/Users/vbaalaadityaa/Downloads/MiniProject/src/main/resources/icons/stop.png",24, 24, true, true);
        Image openFileIcon = new Image("file:/Users/vbaalaadityaa/Downloads/MiniProject/src/main/resources/icons/open_file.png",24, 24, true, true);
        Image applyFilterIcon = new Image("file:/Users/vbaalaadityaa/Downloads/MiniProject/src/main/resources/icons/filter.png",24, 24, true, true);

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


        Button exportButton = new Button("Export Final Video");



        

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
        HBox mergeButtonBox = new HBox(10, exportButton);
        mergeButtonBox.setAlignment(Pos.CENTER_RIGHT);
        mergeButtonBox.setSpacing(10);  // Adjust the spacing between elements
        mergeButtonBox.setPadding(new Insets(0, 10, 0, 0));

        // Open button and control buttons on the left
        HBox leftControlBox = new HBox(10, openButton, controlButtonsBox);
        leftControlBox.setAlignment(Pos.CENTER_LEFT);

        // Combine everything in the top-level HBox
        HBox topLevelControlBox = new HBox(10, leftControlBox, filterOptionsBox);
        topLevelControlBox.setAlignment(Pos.CENTER); // Center everything horizontally
        HBox.setHgrow(filterOptionsBox, Priority.ALWAYS); // Allow filterOptionsBox to take up remaining space

        

        // Set up the main layout using BorderPane
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topLevelControlBox);
        
        // Intensity Slider
        Label intensityLabel = new Label("Filter Intensity:");
        intensityLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
        Slider intensitySlider = new Slider(0, 100, 0); // Intensity range 0-100
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setShowTickMarks(true);

        Label startTimeLabel = new Label("Start Time (Seconds):");
        startTimeLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("Enter start time in seconds");

        Label endTimeLabel = new Label("End Time (Seconds):");
        endTimeLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("Enter end time in seconds");

        Button applyTimeRangeButton = new Button("Apply Time Range");
        applyTimeRangeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Timeframe Slider
         Label timeframeLabel = new Label("Time Frame (Seconds):");
         timeframeLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
         timeframeSlider = new Slider(0, 60, 0); // Adjust range based on video length
         timeframeSlider.setShowTickLabels(true);
         timeframeSlider.setShowTickMarks(true);

         
         ToolBar toolBar = new ToolBar(
            openButton, playButton, pauseButton, stopButton,
            new Label("Filters:"), filterComboBox, applyFilterButton, mergeButtonBox,
            new Label("Start Time:"), startTimeField, new Label("End Time:"), endTimeField, applyTimeRangeButton
    );

    Region emptyRegion = new Region();
    emptyRegion.setPrefHeight(10);  // Set the preferred height explicitly

    VBox slidersBox = new VBox(
             toolBar,
             new HBox(intensityLabel, intensitySlider),
           new HBox(timeframeLabel, timeframeSlider),
             new HBox(timestampLabel, currentFilterLabel),
             emptyRegion
             
     );

     slidersBox.setMinHeight(100);
    
    
    // Variables to store the start and end time values
    double[] startTime = new double[1];  // Using an array to allow modification in the lambda function
    double[] endTime = new double[1];

    // Apply time range button action: Update the start and end times
    applyTimeRangeButton.setOnAction(e -> {
        String startTimeText = startTimeField.getText();
        String endTimeText = endTimeField.getText();

        try {
            double newStartTime = Double.parseDouble(startTimeText);
            double newEndTime = Double.parseDouble(endTimeText);

            // Validate that start time is before end time
            if (newStartTime >= newEndTime) {
                showErrorDialog("Invalid Time Range", "Start time must be less than end time.");
            } else {
                // Update the start and end times
                startTime[0] = newStartTime;
                endTime[0] = newEndTime;

                // Optionally display or update the UI with the new times
                System.out.println("Start Time: " + startTime[0] + " seconds");
                System.out.println("End Time: " + endTime[0] + " seconds");

                // Update a label or display the new values (you can do this part optionally)
                // For example, you could have a label like:
                Label timeRangeLabel = new Label("Start Time: " + startTime[0] + " s - End Time: " + endTime[0] + " s");
                timeRangeLabel.setStyle("-fx-text-fill: #eddbcd; -fx-font-size: 14px;");
                root.setBottom(new VBox(timeRangeLabel, slidersBox));  // Update the layout if needed
            }
        } catch (NumberFormatException ex) {
            showErrorDialog("Invalid Input", "Please enter valid numerical values for the time.");
        }
    });

    HBox bottomControlsBox = new HBox(10, 
        exportButton,
        startTimeLabel, startTimeField,
        endTimeLabel, endTimeField,
        applyTimeRangeButton
    );
    bottomControlsBox.setAlignment(Pos.CENTER_RIGHT);
    bottomControlsBox.setPadding(new Insets(10));

    root.setBottom(bottomControlsBox);

//         // Frame Timeline HBox
         

         


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
                    adjustMediaViewSize(mediaView, root);
                    loadVideo(selectedFile, mediaView);
                    if(videoFiles.size() > 1)
                    {
                        fileChooser = new FileChooser();
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
                        String outputFile = "/Users/vbaalaadityaa/Downloads/MiniProject/src/main/temp/process.mp4";

                        if (outputFile != null) {
                            // Call the VideoMerger class to merge the videos
                            try {
                                VideoMerger.mergeVideos(videoFiles.toArray(new String[0]), outputFile);
                                System.out.println("Videos merged successfully! Output file: " + outputFile);
                                reloadVideo(outputFile);
                                videoFiles.clear();
                                videoFiles.add("/Users/vbaalaadityaa/Downloads/MiniProject/src/main/temp/process.mp4");
                            } catch (Exception ex) {
                                showErrorDialog("Merge Failed", "An error occurred while merging the videos: " + ex.getMessage());
                            }
            }
                    }
                } catch (MediaException es) {
                    showErrorDialog("Error loading the video", es.getMessage());
                }
            }
        });

        applyFilterButton.setOnAction(e -> {
            String selectedFilter = filterComboBox.getValue();
            int intensity = (int) intensitySlider.getValue();
            if(intensity % 2 == 0)
            {
                intensity++;
            }  
            double timeframe = timeframeSlider.getValue();
            currentFilterLabel.setText("Current Filter: " + selectedFilter + " (Intensity: " + (int)intensity + ")");
            System.out.println("Applying filter: " + selectedFilter + " with intensity: " + intensity + " for " + timeframe + " seconds.");
            applyFilter(selectedFilter,intensity);
            reloadVideo("/Users/vbaalaadityaa/Downloads/MiniProject/src/main/temp/process.mp4");
            videoFiles.clear();
            videoFiles.add("/Users/vbaalaadityaa/Downloads/MiniProject/src/main/temp/process.mp4");
        });


        exportButton.setOnAction(e -> {
            exportCurrentVideo(mediaView);
        });

        // Layout for control buttons and filter options
        

        root.setCenter(mediaView);
        root.setBottom(new VBox(slidersBox));
        root.setStyle("-fx-background-color: #192231;");

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        adjustMediaViewSize(mediaView, root);

        adjustMediaViewSize(mediaView, root);

// Add event listeners to resize the video when the window size changes
        scene.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            adjustMediaViewSize(mediaView, root);
        });
        scene.heightProperty().addListener((observable, oldHeight, newHeight) -> {
            adjustMediaViewSize(mediaView, root);
        });
        primaryStage.show();
    }

    private void adjustMediaViewSize(MediaView mediaView, BorderPane root) {
        // Ensure we have a media player and media
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        if (mediaPlayer == null) return;
    
        Media media = mediaPlayer.getMedia();
        if (media == null) return;
    
        // Get the current dimensions of the media (video)
        double mediaWidth = media.getWidth();
        double mediaHeight = media.getHeight();
    
        // Get the current size of the root (the window size)
        double windowWidth = root.getWidth();
        double windowHeight = root.getHeight();
    
        // Define the height of the bottom toolbar and controls (leave extra buffer space)
        double toolBarHeight = 100;  // Adjust based on your actual toolbar height
        double regionHeight = 100;   // The height of the empty region at the bottom (set to 100px in this example)
    
        // Calculate the available height for the video by subtracting the toolBarHeight and the regionHeight
        double availableHeight = windowHeight - toolBarHeight - regionHeight;
    
        // Calculate the aspect ratio of the video
        double aspectRatio = mediaWidth / mediaHeight;
    
        // Variables to store the new width and height for the MediaView
        double newWidth, newHeight;
    
        // If the video can fit within the available height, scale based on height
        if ((availableHeight * aspectRatio) <= windowWidth) {
            newHeight = availableHeight;
            newWidth = newHeight * aspectRatio;
        } else {
            // Otherwise, scale based on the available width
            newWidth = windowWidth;
            newHeight = newWidth / aspectRatio;
        }
    
        // Set the new size of the MediaView to the computed values
        mediaView.setFitWidth(newWidth);
        mediaView.setFitHeight(newHeight);
    
        // Create a new pane (StackPane) to manage the layout
        StackPane videoPane = new StackPane();
        videoPane.getChildren().add(mediaView);  // Add the media view to the pane
    
        // Add the video pane to the center of the root, so it's not stretched by BorderPane
        root.setCenter(videoPane);
    
        // Center the MediaView inside the video pane
        StackPane.setAlignment(mediaView, Pos.CENTER);  // Center it in the StackPane
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
        });


        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double current = newTime.toSeconds();
            double total = mediaPlayer.getTotalDuration().toSeconds();
            updateTimestampDisplay(current, total);
            timeframeSlider.setValue(current);
        });

        timeframeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeframeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });
    }

    

        public void exportCurrentVideo(MediaView mediaView) {
            // Open FileChooser to specify export location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
            File exportFile = fileChooser.showSaveDialog(null);  // Adjust this if a specific stage is needed

            if (exportFile != null) {
                try {
                    // Get the current video source from the MediaPlayer
                    MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
                    if (mediaPlayer == null || mediaPlayer.getMedia() == null) {
                        System.out.println("No video loaded to export.");
                        return;
                    }

                    String videoSource = mediaPlayer.getMedia().getSource();
                    videoSource = videoSource.replace("file:", "");  // Clean up URI path for FFmpeg

                    // Use FFmpeg to export the currently loaded video
                    String exportPath = exportFile.getAbsolutePath();
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg", "-i", videoSource, "-c:v", "copy", "-c:a", "copy", exportPath
                    );

                    Process process = processBuilder.start();
                    process.waitFor();  // Wait until FFmpeg finishes exporting
                    System.out.println("Video exported successfully to: " + exportPath);
                } catch (Exception e) {
                    System.err.println("Export failed: " + e.getMessage());
                }
            } else {
                System.out.println("Export cancelled by user.");
            }
        }
        private void reloadVideo(String videoPath) {
            // Stop and dispose of the current media player
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
        
            // Create a new media player with the updated video path
            Media newMedia = new Media(new File(videoPath).toURI().toString());
            mediaPlayer = new MediaPlayer(newMedia);
            mediaView.setMediaPlayer(mediaPlayer);
        
            // Reinitialize any listeners or settings (if required)
            mediaPlayer.setOnReady(() -> {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                timeframeSlider.setMax(duration);
                updateTimestampDisplay(0, duration);
            });
        
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                double current = newTime.toSeconds();
                double total = mediaPlayer.getTotalDuration().toSeconds();
                updateTimestampDisplay(current, total);
                timeframeSlider.setValue(current);
            });
        
        }

        
        public class FrameBasedSlider extends StackPane {
            private Canvas frameSliderCanvas;
            private List<Image> frames;
            private int currentFrameIndex = 0;
            private double sliderWidth;
        
            public FrameBasedSlider(List<Image> frames) {
                this.frames = frames;
                sliderWidth = 800; // Set to the desired width of the slider
                frameSliderCanvas = new Canvas(sliderWidth, 100); // Adjust height as needed
                drawFrameSlider();
                frameSliderCanvas.setOnMouseClicked(this::handleMouseClick);
        
                // Add the Canvas to the StackPane
                getChildren().add(frameSliderCanvas);
            }
        
            private void drawFrameSlider() {
                GraphicsContext gc = frameSliderCanvas.getGraphicsContext2D();
                gc.setFill(Color.GRAY);
                gc.fillRect(0, 0, sliderWidth, 100);
        
                // Calculate width per frame to fit all frames on the slider
                double frameWidth = sliderWidth / frames.size();
                for (int i = 0; i < frames.size(); i++) {
                    gc.drawImage(frames.get(i), i * frameWidth, 0, frameWidth, 100);
                }
        
                // Draw indicator for current frame
                gc.setFill(Color.RED);
                gc.fillRect(currentFrameIndex * frameWidth, 0, frameWidth, 100);
            }
        
            private void handleMouseClick(MouseEvent event) {
                double x = event.getX();
                int frameIndex = (int) (x / (sliderWidth / frames.size()));
                setCurrentFrame(frameIndex);
            }
        
            public void setCurrentFrame(int frameIndex) {
                currentFrameIndex = frameIndex;
                drawFrameSlider();
                // Use this index to update the main video player frame
            }
        
            public int getCurrentFrameIndex() {
                return currentFrameIndex;
            }
            
        
            private static List<Image> loadFramesFromVideo(String path) {
                // Implement the logic to load video frames into a List<Image>
                return List.of(); // Placeholder
            }
        }
        

    private void applyFilter(String filterName, int intensity) {
        if ("Gaussian Blur".equals(filterName)) {
            GaussianBlur blurFilter = new GaussianBlur(intensity);
            blurFilter.applyFilter(videoFiles.get(0));
        }
        // Add other filters as needed
        System.out.println("Applied filter" + filterName + " with intensity: " + intensity);
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



class VideoFrameLoader {

    // Load frames from video file and return as a list of frames
    public static List<Frame> loadFramesFromVideo(String videoPath) throws Exception {
        List<Frame> frames = new ArrayList<>();
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                frames.add(frame);  // Add each frame to the list
            }
            grabber.stop();
        }

        return frames;
    }
}






