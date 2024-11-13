package ssn.video.features.videosummary;

// import org.opencv.core.Core;
// import org.opencv.core.Mat;
// import org.opencv.core.MatOfByte;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.videoio.VideoCapture;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import java.nio.file.Files;
import java.nio.file.Path;
//import java.nio.file.Paths;


import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.json.JSONArray;
import org.json.JSONObject;

public class VideoFrameDescription {

    static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        //String videoPath = "src/main/resources/flowers.mp4";
        //VideoCapture video = new VideoCapture(videoPath);

        File videoFile = new File("src/main/resources/flowers.mp4");
        System.out.println(videoFile.getAbsolutePath());
        
        String videoPath = "src/main/resources/flowers.mp4";

        List<String> base64Frames = new ArrayList<>();
        int frameCount = 0;

        // Use FFmpegFrameGrabber to open the video file
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame;

            // Read and process each frame
            while ((frame = grabber.grabImage()) != null) {
                Mat matFrame = converter.convert(frame);
                if (matFrame != null) {
                    frameCount++;
                    byte[] imageBytes = encodeFrameToJpeg(matFrame);
                    if (imageBytes != null) {
                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                        base64Frames.add(base64Image);
                    }
                }
            }
            grabber.stop();
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

        } catch (Exception e) {
            System.out.println("Error opening video file: " + e.getMessage());
        }
    }

    // private static byte[] encodeFrameToJpeg(Mat frame) {
    //     MatOfByte buffer = new MatOfByte();
    //     Imgcodecs.imencode(".jpg", frame, buffer);
    //     return buffer.toArray();
    // }

    public static byte[] encodeFrameToJpeg(Mat frame) {
        BytePointer buffer = new BytePointer();
        opencv_imgcodecs.imencode(".jpg", frame, buffer);
        byte[] byteArray = new byte[(int) buffer.limit()];
        buffer.get(byteArray);
        buffer.deallocate(); // Free memory
        return byteArray;
    }

// 
private static String getVideoDescription(List<String> selectedFrames) throws IOException {
    Path apikeyFile = Paths.get("src/main/java/ssn/video/features/videosummary/apikey.txt");
    String apiKey = Files.readString(apikeyFile).trim();
    StringBuilder response = new StringBuilder();

    try {
        URI uri = new URI("https://api.openai.com/v1/chat/completions");
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", "gpt-3.5-turbo");  // Corrected model name
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

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                System.out.println("Error response from API: " + errorResponse.toString());
            }
            return ""; // Return early if there's an error
        }

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

