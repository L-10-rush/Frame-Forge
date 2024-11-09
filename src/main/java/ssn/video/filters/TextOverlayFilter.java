package ssn.video.filters;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.global.opencv_imgproc;

public class TextOverlayFilter extends BaseFilter{
    private String overlayText;
    private Scalar color;
    private Point textPosition;

    private final static Scalar
    RED = new Scalar(255, 0, 0, 255),
    GREEN = new Scalar(0, 255, 0, 255),
    BLUE = new Scalar(0, 0, 255, 255),
    WHITE = new Scalar(255, 255, 255, 255),
    BLACK = new Scalar(0, 0, 0, 255),
    YELLOW = new Scalar(255, 255, 0, 255),
    CYAN = new Scalar(0, 255, 255, 255),
    MAGENTA = new Scalar(255, 0, 255, 255),
    ORANGE = new Scalar(255, 165, 0, 255),
    GRAY = new Scalar(128, 128, 128, 255);

    private Scalar getColor(String color) {
    return switch (color.toLowerCase()) {
        case "red" -> RED;
        case "green" -> GREEN;
        case "blue" -> BLUE;
        case "white" -> WHITE;
        case "black" -> BLACK;
        case "yellow" -> YELLOW;
        case "cyan" -> CYAN;
        case "magenta" -> MAGENTA;
        case "orange" -> ORANGE;
        case "gray" -> GRAY;
        default -> WHITE;
        };
    }

    public TextOverlayFilter(int startTime, int endTime, String overlayText, String color, int x, int y){
        this.startTime = startTime;
        this.endTime = endTime;
        this.overlayText = overlayText;
        this.color = getColor(color);
        this.textPosition = new Point(x, y);
    }

    @Override
    public void applyFilter(String inputFilePath) {
        File inputFile = null;

        try {
            inputFile = getFileFromResources(inputFilePath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        File outputFile = new File("output/"+"textoverlay_"+inputFilePath);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
                recorder.start();

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;
                long xstartTime = startTime * 1_000_000L;
                long xendTime = endTime * 1_000_000L;

                while ((frame = grabber.grabImage()) != null) {
                    long timestamp = grabber.getTimestamp();
                    Mat matFrame = converter.convert(frame);
                    Frame outputFrame;

                    int fontFace = opencv_imgproc.FONT_HERSHEY_SIMPLEX;
                    double fontScale = 1;
                    int thickness = 1;

                    
                    if (timestamp >= xstartTime && timestamp <= xendTime) {
                        opencv_imgproc.putText(matFrame, overlayText, textPosition, fontFace, fontScale, color, thickness, 8, false);
                        outputFrame = converter.convert(matFrame);
                    } else {
                        outputFrame = frame;
                    }
                    
    
                    recorder.setTimestamp(timestamp); // Maintain sync
                    recorder.record(outputFrame);
                }
                recorder.stop();
            }
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
