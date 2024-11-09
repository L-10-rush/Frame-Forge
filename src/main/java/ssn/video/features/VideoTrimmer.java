package ssn.video.features;

import java.io.File;
import java.io.IOException;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class VideoTrimmer {
    public static void trimVideo(String inputFilePath, double startSeconds, double endSeconds) throws Exception {
        String outputFilePath = "output/"+inputFilePath;
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
            grabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
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
}

/*
 * package ssn.video.editor;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class VideoTrimmer {

    public static void trimVideo(String inputFilePath, String outputFilePath, int startSeconds, int endSeconds) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath);
        grabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // H264 codec
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // AAC codec for audio
        recorder.start();

        // Convert start and end times to microseconds
        long startTime = startSeconds * 1_000_000L;
        long endTime = endSeconds * 1_000_000L;

        Frame frame;
        while ((frame = grabber.grab()) != null) {
            long timestamp = grabber.getTimestamp(); // Get the current timestamp in microseconds
            if (timestamp >= startTime && timestamp <= endTime) {
                recorder.setTimestamp(timestamp); // Set the recorder's timestamp to maintain sync
                recorder.record(frame);
            }
        }

        grabber.stop();
        recorder.stop();
    }
}
 */