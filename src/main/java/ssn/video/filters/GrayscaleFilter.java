package ssn.video.filters;


import java.io.File;
import java.io.IOException;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

public class GrayscaleFilter extends BaseFilter {
    
    @Override
    public void applyFilter(String inputFilePath) {
        File inputFile = new File(inputFilePath);

        if (!inputFile.exists()) {
            System.out.println("File not found: " + inputFilePath);
            return;
        }

        String outputFilePath = "output/" + inputFile.getName().replace(".mp4", "_grayscale.mp4");
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.start();

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;

                while ((frame = grabber.grabImage()) != null) {
                    Mat matFrame = converter.convert(frame);
                    Mat grayscaleFrame = new Mat();

                    // Convert the frame to grayscale
                    opencv_imgproc.cvtColor(matFrame, grayscaleFrame, opencv_imgproc.COLOR_BGR2GRAY);
                    
                    // Convert back to BGR format for recording
                    opencv_imgproc.cvtColor(grayscaleFrame, grayscaleFrame, opencv_imgproc.COLOR_GRAY2BGR);

                    recorder.record(converter.convert(grayscaleFrame));
                }
            }

            System.out.println("Grayscale video saved at: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error while applying grayscale filter to video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

