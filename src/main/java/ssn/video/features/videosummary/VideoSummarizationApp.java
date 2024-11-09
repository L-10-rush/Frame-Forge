package ssn.video.features.videosummary;

public class VideoSummarizationApp {
    public static void main(String[] args) {
        String modelPath = "model/best_model.onnx";
        String videoPath = "src/main/resources/Video-Placeholder.mp4";
        String outputPath = "output";

        // Initialize the model
        Model model = new Model(modelPath);

        // Initialize VideoProcessor with model
        VideoProcessor videoProcessor = new VideoProcessor(model);

        // Summarize the video
        videoProcessor.summarizeVideo(videoPath, outputPath);

        // Clean up resources
        model.close();
    }
}
