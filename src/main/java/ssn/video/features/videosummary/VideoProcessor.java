package ssn.video.features.videosummary;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgproc;

import java.util.ArrayList;
import java.util.List;

public class VideoProcessor {
    private Model model;

    public VideoProcessor(Model model) {
        this.model = model;
    }

    public void summarizeVideo(String inputFile, String outputDir) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            try(FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputDir + "/summary.mp4", grabber.getImageWidth(), grabber.getImageHeight())){

                recorder.setFrameRate(grabber.getFrameRate());
                recorder.start();

                Frame frame;
                List<Frame> selectedFrames = new ArrayList<>();

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

                // Process each frame for summarization
                while ((frame = grabber.grabImage()) != null) {
                    // Convert Frame to a format suitable for model prediction
                    Mat matFrame = converter.convert(frame);
                    float[] inputData = matToFloatArray(matFrame); // Convert Mat to float array

                    // Use model to predict importance score
                    float[] scores = model.predict(inputData);
                    float importanceScore = scores[0]; // Assuming the model outputs a single importance score

                    // Select frame if importance score exceeds threshold
                    if (importanceScore > 0.5) {  // Set threshold as needed
                        selectedFrames.add(frame.clone()); // Clone to avoid overwriting frame data
                    }
                }

                // Record selected frames
                for (Frame selectedFrame : selectedFrames) {
                    recorder.record(selectedFrame);
                }

                recorder.stop();
            }
            grabber.stop();
            System.out.println("Video summarization completed and saved to: " + outputDir + "/summary.mp4");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper function to convert Frame to Mat (required for processing with OpenCV)
    // private Mat convertFrameToMat(Frame frame) {
    //     return new Mat(frame.imageHeight, frame.imageWidth, opencv_imgproc.CV_32F);
    // }

    // Convert Mat to float array for model prediction
    private float[] matToFloatArray(Mat mat) {
        int totalElements = (int) (mat.total() * mat.channels());
        float[] data = new float[totalElements];
    
        // Normalize and convert pixel values to float
        for (int i = 0; i < totalElements; i++) {
            // Assuming mat is in CV_8UC3 (8-bit unsigned char with 3 channels)
            // Normalize to [0, 1] by dividing by 255.0
            data[i] = (float) (mat.ptr(0).get(i) & 0xFF) / 255.0f; // Convert to float and normalize
        }
    
        return data;
    }
}
