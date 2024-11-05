package ssn.video.features;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class VideoTrimmer {
    public static void trimVideo(String inputFilePath, String outputFilePath, double startSeconds, double endSeconds) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
            grabber.start();

            // Create the recorder with the same properties as the grabber
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // Use H264 codec for video
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // Use AAC codec for audio
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.start();

                // Convert start and end times to microseconds
                long startMicroseconds = (long) (startSeconds * 1_000_000L);
                long endMicroseconds = (long) (endSeconds * 1_000_000L);

                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    long timestamp = grabber.getTimestamp();
                    if (timestamp >= startMicroseconds && timestamp <= endMicroseconds) {
                        recorder.setTimestamp(timestamp - startMicroseconds); // Sync with trimmed start time
                        recorder.record(frame);
                    } else if (timestamp > endMicroseconds) {
                        break; // Stop reading frames once we exceed the end time
                    }
                }
            }

            grabber.stop();
            System.out.println("Video trimmed successfully: " + outputFilePath);
        }
    }

    public static void main(String[] args) {
        try {
            trimVideo("src/main/resources/Video-Placeholder.mp4", "output/outputty.mp4", 4.0, 9.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
