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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple Video Editor");

        MediaView mediaView = new MediaView();

        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Button openButton = new Button("Open File");

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
                    new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.mov",".png",".jpg")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                try{
                    loadVideo(selectedFile, mediaView);
                }catch(MediaException es){
                    System.err.println("Error loading the video: " + es.getMessage());
                }
                
            }
        });

        // Arrange buttons in a toolbar
        ToolBar toolBar = new ToolBar(openButton, playButton, pauseButton, stopButton);

        // Add sliders for width and height adjustments
        Label widthLabel = new Label("Width:");
        Slider widthSlider = new Slider(100, 1600, 800);
        widthSlider.setShowTickLabels(true);

        Label heightLabel = new Label("Height:");
        Slider heightSlider = new Slider(100, 1200, 600);
        heightSlider.setShowTickLabels(true);

        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            mediaView.setFitWidth(newValue.doubleValue());
        });

        heightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            mediaView.setFitHeight(newValue.doubleValue());
        });

        VBox slidersBox = new VBox(widthLabel, widthSlider, heightLabel, heightSlider);

        // Layout the GUI
        try{
            BorderPane root = new BorderPane();
            root.setCenter(mediaView);
            root.setTop(toolBar);
            root.setBottom(slidersBox);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(MediaException es){
            System.err.println("Error playing the video: " + es.getMessage());
        }
    }

    private void loadVideo(File file, MediaView mediaView) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose(); // Stop and release current media if playing
        }
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
    }
}
