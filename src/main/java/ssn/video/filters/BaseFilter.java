package ssn.video.filters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public abstract class BaseFilter implements Filter{
    protected int startTime;
    protected int endTime;

    protected void setupRecorder(FFmpegFrameRecorder recorder, double frameRate) throws Exception {
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setFrameRate(frameRate);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.start();
    }

    protected void createOutputDir() {
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    protected boolean isImageFile(String extension) {
        return extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") ||
               extension.equalsIgnoreCase(".png") || extension.equalsIgnoreCase(".bmp");
    }

    protected boolean isVideoFile(String extension) {
        return extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") ||
               extension.equalsIgnoreCase(".mkv") || extension.equalsIgnoreCase(".mov");
    }

    protected String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return lastIndex == -1 ? "" : name.substring(lastIndex);
    }

    protected File getFileFromResources(String resourceName) throws IOException {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resourceStream == null) {
            System.out.println("Resource not found: " + resourceName);
            return null;
        }
        
        String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
        File tempFile = Files.createTempFile(fileName, "").toFile();
        Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        return tempFile;
    }
    

    @Override
    public abstract void applyFilter(String inputFilePath);
}
