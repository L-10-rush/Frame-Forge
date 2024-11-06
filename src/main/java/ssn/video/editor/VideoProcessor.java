// package ssn.video.editor;

// import java.util.List;

// import ssn.video.filters.Filter;

// import org.bytedeco.ffmpeg.global.avcodec;
// import org.bytedeco.ffmpeg.global.avutil;
// import org.bytedeco.javacv.FFmpegFrameGrabber;
// import org.bytedeco.javacv.FFmpegFrameRecorder;
// import org.bytedeco.javacv.Frame;
// import org.bytedeco.opencv.opencv_core.Mat;


// public class VideoProcessor<T extends Filter> {
//     private List<T> filters;
//     private final String outputPath;

//     public VideoProcessor(List<T> filters, String outputPath) {
//         this.filters = filters;
//         this.outputPath = outputPath;
//     }

//     public void processVideo(String inputVideoPath) {
//         try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputVideoPath)) {
//             grabber.start();

//             try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
//                 recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//                 recorder.setFormat("mp4");
//                 recorder.setFrameRate(grabber.getFrameRate());
//                 recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//                 recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

//                 recorder.start();

//                 OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//                 Frame frame;

//                 while ((frame = grabber.grabImage()) != null) {
//                     Mat matFrame = converter.convert(frame);

//                     // Apply each filter in the pipeline to the frame
//                     for (T filter : filters) {
//                         filter.applyFilter(matFrame);
//                     }

//                     // Record the processed frame
//                     recorder.record(converter.convert(matFrame));
//                 }

//                 System.out.println("Processed video saved at: " + outputPath);
//             } catch (FFmpegFrameRecorder.Exception e) {
//                 System.err.println("Error while recording: " + e.getMessage());
//                 e.printStackTrace();
//             }
//         } catch (Exception e) {
//             System.err.println("Error while processing video: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
// }
