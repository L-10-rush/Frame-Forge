package ssn.video.filters;

import java.io.File;
import java.io.IOException;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class BlackAndWhiteFilter extends BaseFilter {

    /**
     * Applies a black and white filter to a video.
     *
     * @param inputFilePath The path to the input video file.
     */
    @Override
    public void applyFilter(String inputFilePath) {
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

        File outputFile = new File("output/"+inputFilePath);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.start();

                FFmpegFrameFilter filter = new FFmpegFrameFilter("hue=s=0" + ",format=yuv420p", grabber.getImageWidth(), grabber.getImageHeight());
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

    private void applyFilterToVideo(File inputFile, File outputFile){}
}
