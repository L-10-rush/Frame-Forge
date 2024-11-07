package ssn.video.filters;

import java.io.File;
import java.io.IOException;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class FadeFilter extends BaseFilter {
    private int startTime;
    private int endTime;
    private String fadeType;

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

        File outputFile = new File("output/IAMFAADEEEDDD.mp4");

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                int frameRate = (int) grabber.getFrameRate();
                recorder.setFrameRate(frameRate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUVJ420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.start();

                int startFrame = startTime * frameRate;
                int endFrame = endTime * frameRate;

                FFmpegFrameFilter filter = new FFmpegFrameFilter("fade=" + fadeType + ":" + startFrame + ":" + endFrame + 
                                                            ",format=yuv420p", grabber.getImageWidth(), grabber.getImageHeight());
                filter.start();

                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    if (frame.image != null) { // Process video frames only
                        filter.push(frame);
                        Frame filteredFrame = filter.pull();
                        recorder.record(filteredFrame);
                    } else if (frame.samples != null) { // Handle audio frames directly
                        recorder.recordSamples(frame.samples);
                    }
                }

                filter.stop();
                recorder.stop();
            }

            System.out.println("Black and white filter applied successfully. Output saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FadeFilter fader = new FadeFilter(0, 3, "in");
        fader.applyFilter("kratosyan.mp4");
    }

}
