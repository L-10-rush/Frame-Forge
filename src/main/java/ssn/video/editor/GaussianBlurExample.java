package ssn.video.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

public class GaussianBlurExample {

    public void GBlur() throws IOException {
        // Load the image from the resources folder and save it temporarily
        String imagePath = saveResourceToTempFile("DemoAssets/SkinnyChad.jpg"); // Replace with your image name
        System.out.println("Resolved image path: " + imagePath);
        Mat image = opencv_imgcodecs.imread(imagePath);

        if (image.empty()) {
            System.out.println("Image not found or couldn't be loaded!");
            return;
        }

        // Create a Mat to store the result
        Mat blurredImage = new Mat();

        // Apply Gaussian blur (kernel size 15x15)
        opencv_imgproc.GaussianBlur(image, blurredImage, new org.bytedeco.opencv.opencv_core.Size(49, 49), 0);

        // Ensure the output directory exists
        String outputImagePath = "output/blurred_image.jpg";
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the output directory if it doesn't exist
        }

        // Save the result to the output file
        opencv_imgcodecs.imwrite(outputImagePath, blurredImage);

        System.out.println("Gaussian blurred image saved at: " + outputImagePath);
    }

    // Helper method to save a resource file to a temporary location
    private static String saveResourceToTempFile(String resourceName) throws IOException {
        // Load the image as a stream from resources
        InputStream inputStream = GaussianBlurExample.class.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        // Save the image to a temporary file
        File tempFile = Files.createTempFile("image-", ".jpg").toFile();
        Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFile.getAbsolutePath();
    }
}
