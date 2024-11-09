package ssn.video.editor;

import ssn.video.features.VideoTrimmer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Editing Video...");
        try {
            // Define paths to the input and output files
            String inputFilePath = "src/main/resources/DemoAssets/GigaChad.mp4";
            String outputFilePath = "src/main/resources/DemoAssets/GigaChad_trimmed.mp4";

            // Set the start and end times in seconds (e.g., 10 to 20 seconds)
            int startSeconds = 5;
            int endSeconds = 10;

            // Call the VideoTrimmer to trim the video
            VideoTrimmer.trimVideo(inputFilePath, startSeconds, endSeconds);
            
            System.out.println("Video trimmed successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
