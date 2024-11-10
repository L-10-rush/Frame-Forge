package ssn.video.features.videosummary;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class VideoFrameDescription {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String videoPath = "src/main/resources/flowers.mp4";
        VideoCapture video = new VideoCapture(videoPath);

        if (!video.isOpened()) {
            System.out.println("Error opening video file.");
            return;
        }

        List<String> base64Frames = new ArrayList<>();
        Mat frame = new Mat();
        int frameCount = 0;

        while (video.read(frame)) {
            frameCount++;
            byte[] imageBytes = encodeFrameToJpeg(frame);
            if (imageBytes != null) {
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                base64Frames.add(base64Image);
            }
        }
        video.release();
        System.out.println(frameCount + " frames read.");

        // Select every 50th frame
        List<String> selectedFrames = IntStream.range(0, base64Frames.size())
                .filter(i -> i % 50 == 0)
                .mapToObj(base64Frames::get)
                .collect(Collectors.toList());

        try {
            String response = getVideoDescription(selectedFrames);
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error making OpenAI request: " + e.getMessage());
        }
    }

    private static byte[] encodeFrameToJpeg(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, buffer);
        return buffer.toArray();
    }

    private static String getVideoDescription(List<String> selectedFrames) throws IOException {
        String apiKey = "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"; // Replace with your actual API key
        StringBuilder response = new StringBuilder();
        
        try {
            // Create a URI and convert it to a URL
            URI uri = new URI("https://api.openai.com/v1/chat/completions");
            URL url = uri.toURL();
            
            // Open connection and set it as an HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Configure connection parameters
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
    
            // Construct JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-4o");
            JSONArray messages = new JSONArray();
    
            JSONObject message = new JSONObject();
            message.put("role", "user");
    
            JSONArray content = new JSONArray();
            content.put("These are frames from a video that I want to upload. Generate a compelling description that I can upload along with the video.");
            for (String frame : selectedFrames) {
                JSONObject imageContent = new JSONObject();
                imageContent.put("image", frame);
                imageContent.put("resize", 768);
                content.put(imageContent);
            }
            message.put("content", content);
            messages.put(message);
            jsonBody.put("messages", messages);
            jsonBody.put("max_tokens", 200);
    
            // Send JSON request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    
        return response.toString();
    }    
}
