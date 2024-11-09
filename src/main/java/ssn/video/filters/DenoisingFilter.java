package ssn.video.filters;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_photo;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber.Exception;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

public class DenoisingFilter extends BaseFilter {

    private final int templateWindowSize; // Template window size
    private final int searchWindowSize;  // Search window size

    public DenoisingFilter(int templateWindowSize, int searchWindowSize) {
        this.templateWindowSize = templateWindowSize;
        this.searchWindowSize = searchWindowSize;
    }

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

        String extension = getFileExtension(inputFile);
        createOutputDir();
        File outputFile = new File("output/" + inputFilePath);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            int totalDurationInSeconds = (int) grabber.getLengthInTime() / 1000000; // Duration in seconds

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
                recorder.start();

                Frame frame;
                int currentFrame = 0;
                int frameRate = (int) grabber.getFrameRate();
                int startFrame = startTime * frameRate;
                int endFrame = endTime * frameRate;

                while ((frame = grabber.grabFrame()) != null) {
                    if (currentFrame < startFrame) {
                        currentFrame++;
                        continue;
                    }

                    if (currentFrame > endFrame) {
                        break;
                    }

                    if (frame.image != null) {
                        // Ensure image[0] is not null
                        ByteBuffer byteBuffer = (ByteBuffer) frame.image[0];

                        if (byteBuffer != null && byteBuffer.remaining() > 0) {
                            byte[] byteArray = new byte[byteBuffer.remaining()];
                            byteBuffer.get(byteArray);

                            // Convert byteArray to Mat
                            Mat mat = new Mat(grabber.getImageHeight(), grabber.getImageWidth(), CV_8UC3);
                            mat.data().put(byteArray);

                            // Apply FastNlMeansDenoising for denoising
                            Mat denoisedMat = new Mat();
                            opencv_photo.fastNlMeansDenoising(mat, denoisedMat, 30, templateWindowSize, searchWindowSize);

                            // Convert denoised Mat back to Frame
                            Frame denoisedFrame = new Frame(grabber.getImageWidth(), grabber.getImageHeight(), Frame.DEPTH_BYTE, 3);
                            byte[] outputBytes = new byte[(int) (denoisedMat.total() * denoisedMat.channels())];
                            denoisedMat.data().get(outputBytes);
                            ByteBuffer outputBuffer = ByteBuffer.wrap(outputBytes);
                            denoisedFrame.image[0] = outputBuffer;

                            // Record the frame to output file
                            recorder.record(denoisedFrame);
                        }
                    }
                    currentFrame++;
                }

                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
