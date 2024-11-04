package ssn.video.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

public class GaussianBlurExample {

    public void GBlur(String inputFilePath) throws IOException {
        File inputFile = getFileFromResources(inputFilePath);
        
        // Check if input file exists
        if (inputFile == null || !inputFile.exists()) {
            System.out.println("File not found: " + inputFilePath);
            return;
        }

        // Determine if it's an image or video based on extension
        String extension = getFileExtension(inputFile);
        if (isImageFile(extension)) {
            applyBlurToImage(inputFile); // Pass file directly
        } else if (isVideoFile(extension)) {
            applyBlurToVideo(inputFile); // Pass file directly
        } else {
            System.out.println("Unsupported file type. Please provide a valid image or video file.");
        }
    }

    public File getFileFromResources(String resourceName) throws IOException {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resourceStream == null) {
            System.out.println("Resource not found: " + resourceName);
            return null;
        }

        // Create a temporary file with the correct extension
        String extension = resourceName.substring(resourceName.lastIndexOf("."));
        File tempFile = Files.createTempFile("temp-", extension).toFile();

        Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    private boolean isImageFile(String extension) {
        return extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") ||
               extension.equalsIgnoreCase(".png") || extension.equalsIgnoreCase(".bmp");
    }

    private boolean isVideoFile(String extension) {
        return extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") ||
               extension.equalsIgnoreCase(".mkv") || extension.equalsIgnoreCase(".mov");
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return lastIndex == -1 ? "" : name.substring(lastIndex);
    }

    private void applyBlurToImage(File imageFile) {
        Mat image = opencv_imgcodecs.imread(imageFile.getAbsolutePath());

        if (image.empty()) {
            System.out.println("Image not found or couldn't be loaded!");
            return;
        }

        Mat blurredImage = new Mat();
        opencv_imgproc.GaussianBlur(image, blurredImage, new Size(49, 49), 0);

        String outputImagePath = "output/blurred_image.jpg";
        createOutputDir();
        opencv_imgcodecs.imwrite(outputImagePath, blurredImage);

        System.out.println("Gaussian blurred image saved at: " + outputImagePath);
    }

    private void applyBlurToVideo(File videoFile) {
        String outputVideoPath = "output/blurred_video.mp4";
        createOutputDir();  // Ensure the output directory exists
    
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
            grabber.start();
    
            // Set up the recorder with necessary configurations
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideoPath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // H264 codec
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // Ensure compatibility
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // Set audio codec if you want to include audio
    
                recorder.start();
    
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;
    
                while ((frame = grabber.grabImage()) != null) {
                    Mat matFrame = converter.convert(frame);
                    Mat blurredFrame = new Mat();
    
                    // Apply Gaussian blur
                    opencv_imgproc.GaussianBlur(matFrame, blurredFrame, new Size(49, 49), 0);
                    
                    // Convert blurred Mat back to Frame and write to output
                    recorder.record(converter.convert(blurredFrame));
                }
    
                System.out.println("Gaussian blurred video saved at: " + outputVideoPath);
            } catch (FFmpegFrameRecorder.Exception e) {
                System.err.println("Error while recording: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error while applying blur to video: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    public void applyBlurToVideoWithTime(String inputFilePath, String outputFilePath, int startSeconds, int endSeconds) {
        File inputFile = null;
        createOutputDir();
        try{inputFile = getFileFromResources(inputFilePath);}
        catch(IOException e){
            System.err.println("Error while loading inputFile in function -- applying blur to video with time: "+e.getMessage());
            e.printStackTrace();
        }
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
    
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("output/"+outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
    
                recorder.start();
    
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;
                long startTime = startSeconds * 1_000_000L;
                long endTime = endSeconds * 1_000_000L;
    
                while ((frame = grabber.grabImage()) != null) {
                    long timestamp = grabber.getTimestamp();
                    Mat matFrame = converter.convert(frame);
                    Mat outputFrame = new Mat();
    
                    if (timestamp >= startTime && timestamp <= endTime) {
                        // Apply Gaussian blur
                        opencv_imgproc.GaussianBlur(matFrame, outputFrame, new Size(49, 49), 0);
                    } else {
                        outputFrame = matFrame;
                    }
    
                    recorder.setTimestamp(timestamp); // Maintain sync
                    recorder.record(converter.convert(outputFrame));
                }
    
                System.out.println("Gaussian blur applied successfully from " + startSeconds + "s to " + endSeconds + "s and saved as " + outputFilePath);
            }
        } catch (Exception e) {
            System.err.println("Error while applying blur to video: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void createOutputDir() {
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
}
