package ssn.video.filters;

import java.io.File;
import java.io.IOException;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;

public class FadeFilter extends BaseFilter {
    private final String fadeType; // "in" for fade-in, "out" for fade-out

    public FadeFilter(int startTime, int endTime, String fadeType){
        this.startTime = startTime;
        this.endTime = endTime;
        this.fadeType = fadeType;
    }

    @Override
    public void applyFilter(String inputFilePath){
        File inputFile = null;

        try {
            inputFile = getFileFromResources(inputFilePath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        if (inputFile == null || !inputFile.exists()) {
            System.out.println("File not found: " + inputFilePath);
            return;
        }

        File outputFile = new File("output/"+inputFilePath);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                int frameRate = (int) grabber.getFrameRate();
                recorder.setFrameRate(frameRate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setMaxBFrames(100); // Allow for more buffering
                recorder.setVideoBitrate(2000000); // Increase bitrate to allow more time for encoding
                recorder.setGopSize(12); // Reduce GOP size to ensure keyframes are inserted more frequently
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // Ensure the correct audio codec
                recorder.setSampleRate(grabber.getSampleRate()); // Match sample rate to input
                recorder.setAudioChannels(grabber.getAudioChannels()); // Ensure correct number of audio channels

                recorder.start();

                int startFrame = startTime * frameRate;
                int endFrame = endTime * frameRate;
                
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

                Frame frame;
                int currentFrame = 0;
                while ((frame = grabber.grabFrame()) != null) {
                    if (frame.image != null) {
                        Mat matFrame = converter.convert(frame);
                        
                        // Check if we're within the fade time frame
                        if (currentFrame >= startFrame && currentFrame <= endFrame) {
                            float fadeFactor = calculateFadeFactor(currentFrame, startFrame, endFrame);
                            applyFadeEffect(matFrame, fadeFactor);
                        }
                
                        // Ensure to record the frame, even if it's processed later
                        if (frame.image != null) {
                            recorder.record(converter.convert(matFrame));
                        }
                    } else if (frame.samples != null) {
                        recorder.recordSamples(frame.samples);  // Record audio samples
                    }
                
                    // Increment the frame counter
                    currentFrame++;
                }
                
            }

            System.out.println("Fade filter applied successfully. Output saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate the fade factor between 0 and 1.
     */
    private float calculateFadeFactor(int currentFrame, int startFrame, int endFrame) {
        int totalFrames = endFrame - startFrame;
        float progress = (currentFrame - startFrame) / (float) totalFrames;

        if ("in".equals(fadeType)) {
            return progress;  // Fade in
        } else if ("out".equals(fadeType)) {
            return 1 - progress;  // Fade out
        }
        return 1.0f;
    }

    /**
     * Apply the fade effect to the frame based on the fade factor.
     */
    private void applyFadeEffect(Mat frame, float fadeFactor) {
        // Create a black frame (or blank) with the same size as the current frame
        Mat blackFrame = new Mat(frame.size(), frame.type(), Scalar.all(0));
        
        // Blend the black frame with the original frame using fadeFactor
        // The formula for blending: result = frame * fadeFactor + blackFrame * (1 - fadeFactor)
        // As fadeFactor goes from 0 to 1, we mix more of the original frame.
        // When fadeFactor is 0, we have the black frame, when fadeFactor is 1, we have the original frame.
        opencv_core.addWeighted(frame, fadeFactor, blackFrame, 1 - fadeFactor, 0, frame);
    }
    
}
