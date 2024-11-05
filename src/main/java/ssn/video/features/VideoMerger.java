package ssn.video.features;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class VideoMerger {

    public static void mergeVideos(String[] inputFilePaths, String outputFilePath) throws Exception {
        FFmpegFrameRecorder recorder = null;

        try {
            // Loop through each input file
            for (String inputFilePath : inputFilePaths) {
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
                    grabber.start();

                    // Initialize the recorder with properties of the first video
                    if (recorder == null) {
                        recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // Set H264 video codec
                        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // Set AAC audio codec
                        recorder.setFrameRate(grabber.getFrameRate());
                        recorder.start();
                    }

                    Frame frame;
                    while ((frame = grabber.grab()) != null) {
                        recorder.record(frame); // Record each frame to the output file
                    }

                    grabber.stop(); // Stop the grabber for this video
                }
            }

            System.out.println("Videos merged successfully into: " + outputFilePath);
        } finally {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Paths to the videos you want to merge
            String[] inputVideos = {
                "output/outputty.mp4",
                "src/main/resources/bad.mp4"
                // Add more paths as needed
            };
            
            // Output path for the merged video
            String outputFilePath = "output/mergedTrial1woaahhhhh.mp4";

            mergeVideos(inputVideos, outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

