package ssn.video.filters;

import java.io.File;
import java.io.IOException;

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

public class GaussianBlur extends BaseFilter{
    private int kernelSize;
    private int startTime;
    private int endTime;

    public GaussianBlur(int kernelSize){
        if (kernelSize%2==0) throw new IllegalArgumentException("KERNEL SIZE MUST BE ODD");
        this.kernelSize = kernelSize;
        this.startTime = 0;
        this.endTime = 0;
    }
    public GaussianBlur(int kernelSize, int startTime, int endTime){
        if (kernelSize%2==0) throw new IllegalArgumentException("KERNEL SIZE MUST BE ODD");
        this.kernelSize = kernelSize;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public GaussianBlur(int startTime, int endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public GaussianBlur(){
        this.kernelSize = 15;
        this.startTime = 0;
        this.endTime = 0;
    }

    @Override
    public void applyFilter(String inputFilePath){
        File inputFile = null;
        try {
            inputFile = getFileFromResources(inputFilePath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (inputFile == null || !inputFile.exists()) {
            System.out.println("File not found: " + inputFilePath);
            return;
        }

        String extension = getFileExtension(inputFile);
        createOutputDir();
        String outputFilePath = "output/"+inputFilePath+"_processed"+extension;

        if (isImageFile(extension)) {
            applyBlurToImage(inputFile, outputFilePath, kernelSize);
        } else if (isVideoFile(extension) && startTime-endTime == 0) {
            applyBlurToVideo(inputFile, outputFilePath, kernelSize);
        } else if (isVideoFile(extension) && startTime-endTime != 0) {
            applyBlurToVideoWithTime(inputFile, outputFilePath,startTime, endTime, kernelSize);
        } else {
            System.out.println("Unsupported file type. Please provide a valid image or video file.");
        }
    }

    private void applyBlurToImage(File imageFile, String outputFilePath, int kernelSize) {
        Mat image = opencv_imgcodecs.imread(imageFile.getAbsolutePath());
        if (image.empty()) {
            System.out.println("Image not found or couldn't be loaded!");
            return;
        }

        Mat blurredImage = new Mat();
        opencv_imgproc.GaussianBlur(image, blurredImage, new Size(kernelSize, kernelSize), 0);

        opencv_imgcodecs.imwrite(outputFilePath, blurredImage);
        System.out.println("Gaussian blurred image saved at: " + outputFilePath);
    }

    private void applyBlurToVideo(File videoFile, String outputVideoPath, int kernelSize) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideoPath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
            
            grabber.start();
            setupRecorder(recorder, grabber.getFrameRate());

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                Mat matFrame = converter.convert(frame);
                Mat blurredFrame = new Mat();
                opencv_imgproc.GaussianBlur(matFrame, blurredFrame, new Size(kernelSize, kernelSize), 0);
                recorder.record(converter.convert(blurredFrame));
            }

            System.out.println("Gaussian blurred video saved at: " + outputVideoPath);
        } catch (Exception e) {
            System.err.println("Error while applying blur to video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void applyBlurToVideoWithTime(File inputFile, String outputFilePath, int startSeconds, int endSeconds, int kernelSize) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
    
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                int frameRate = (int) (grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 30);
                recorder.setFrameRate(frameRate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
    
                recorder.start();
    
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;
                long xstartTime = startSeconds * 1_000_000L;
                long xendTime = endSeconds * 1_000_000L;
    
                while ((frame = grabber.grabImage()) != null) {
                    long timestamp = grabber.getTimestamp();
                    Mat matFrame = converter.convert(frame);
                    Mat outputFrame = new Mat();
    
                    if (timestamp >= xstartTime && timestamp <= xendTime) {
                        // Apply Gaussian blur
                        opencv_imgproc.GaussianBlur(matFrame, outputFrame, new Size(kernelSize, kernelSize), 0);
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
}
