import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArray;
import org.tensorflow.types.TFloat32;
import org.tensorflow.ndarray.NdArrays;
//import org.tensorflow.types.TFloat32;

// Assuming `floatArray` is your float[]



import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VideoDeblur {
    private SavedModelBundle model;

    public VideoDeblur(String modelUrl) {
        // Download and load the model
        String modelPath = downloadModel(modelUrl);
        if (modelPath != null) {
            model = SavedModelBundle.load(modelPath, "serve");
        } else {
            throw new RuntimeException("Failed to load model.");
        }
    }

    private String downloadModel(String modelUrl) {
        try {
            // Create a temporary directory for the model
            File tempDir = Files.createTempDirectory("deblur_model").toFile();
            tempDir.deleteOnExit(); // Delete when the program exits

            // Open a connection to the model URL
            HttpURLConnection connection = (HttpURLConnection) new URL(modelUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Download the model zip file
            File modelZip = new File(tempDir, "model.zip");
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(modelZip)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Unzip the model files
            unzip(modelZip.getPath(), tempDir.getPath());

            // Return the path to the unzipped model directory
            return tempDir.getPath();
        } catch (Exception e) {
            System.err.println("Error downloading model: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    public Mat deblurFrame(Mat frame) {
        try (Tensor inputTensor = preprocessFrame(frame)) {
            Tensor outputTensor = model.session().runner()
                    .feed("input", inputTensor)
                    .fetch("output")
                    .run()
                    .get(0);
            return postProcessOutput(outputTensor, frame.size());
        }
    }

    private Tensor preprocessFrame(Mat frame) {
        FloatIndexer indexer = frame.createIndexer();
        int rows = frame.rows();
        int cols = frame.cols();
        float[] data = new float[rows * cols * 3];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int baseIndex = (i * cols + j) * 3;
                data[baseIndex] = indexer.get(i, j, 0) / 255.0f;
                data[baseIndex + 1] = indexer.get(i, j, 1) / 255.0f;
                data[baseIndex + 2] = indexer.get(i, j, 2) / 255.0f;
            }
        }

        return TFloat32.tensorOf(NdArrays.vectorOf(data)).reshape(1, rows, cols, 3);

    }

    private Mat postProcessOutput(Tensor outputTensor, Size originalSize) {
        float[] deblurredData = outputTensor.copyTo(new float[originalSize.height() * originalSize.width() * 3]);

        Mat deblurredMat = new Mat(originalSize, opencv_imgproc.COLOR_RGB2BGR);
        FloatIndexer indexer = deblurredMat.createIndexer();

        for (int i = 0; i < originalSize.height(); i++) {
            for (int j = 0; j < originalSize.width(); j++) {
                int baseIndex = (i * originalSize.width() + j) * 3;
                indexer.put(i, j, 0, deblurredData[baseIndex] * 255.0f);
                indexer.put(i, j, 1, deblurredData[baseIndex + 1] * 255.0f);
                indexer.put(i, j, 2, deblurredData[baseIndex + 2] * 255.0f);
            }
        }

        return deblurredMat;
    }

    public void deblurVideo(String inputFile, String outputFile) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new File(inputFile));
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new File(outputFile),
                     grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {

            grabber.start();
            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setFormat("mp4");
            recorder.start();

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                Mat matFrame = converter.convert(frame);
                Mat deblurredMat = deblurFrame(matFrame);
                recorder.record(converter.convert(deblurredMat));
            }

            System.out.println("Deblurring completed and saved to: " + outputFile);
        } catch (Exception e) {
            System.err.println("Error during video deblurring: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Replace with your model URL
        VideoDeblur deblurer = new VideoDeblur("https://github.com/VITA-Group/DeblurGANv2.git");
        deblurer.deblurVideo("src/main/resources/bad.mp4", "output_deblurred_video.mp4");
    }
}
