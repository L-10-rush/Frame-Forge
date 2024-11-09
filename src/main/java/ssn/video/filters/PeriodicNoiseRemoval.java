package ssn.video.filters;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.utils.Converters;

public class PeriodicNoiseRemoval {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    // Colors (Scalar) used for the filter
    private final static Scalar RED = new Scalar(255, 0, 0, 255),
            GREEN = new Scalar(0, 255, 0, 255),
            BLUE = new Scalar(0, 0, 255, 255),
            WHITE = new Scalar(255, 255, 255, 255),
            BLACK = new Scalar(0, 0, 0, 255),
            YELLOW = new Scalar(255, 255, 0, 255),
            CYAN = new Scalar(0, 255, 255, 255),
            MAGENTA = new Scalar(255, 0, 255, 255),
            ORANGE = new Scalar(255, 165, 0, 255),
            GRAY = new Scalar(128, 128, 128, 255);

    // Main processing method
    public static void main(String[] args) {
        String strInFileName = "period_input.jpg";  // Replace with command line argument if needed

        Mat imgIn = Imgcodecs.imread(strInFileName, Imgcodecs.IMREAD_GRAYSCALE);
        if (imgIn.empty()) {
            System.out.println("ERROR : Image cannot be loaded..!!");
            return;
        }

        // Show original image
        Imgproc.imshow("Image corrupted", imgIn);
        imgIn.convertTo(imgIn, CvType.CV_32F);

        // It needs to process even-sized images only
        Rect roi = new Rect(0, 0, imgIn.cols() & -2, imgIn.rows() & -2);
        imgIn = new Mat(imgIn, roi);

        // PSD calculation (start)
        Mat imgPSD = new Mat();
        calcPSD(imgIn, imgPSD, 0);
        fftshift(imgPSD, imgPSD);
        Core.normalize(imgPSD, imgPSD, 0, 255, Core.NORM_MINMAX);
        // PSD calculation (stop)

        // H calculation (start)
        Mat H = Mat.ones(roi.size(), CvType.CV_32F);
        int r = 21;
        synthesizeFilterH(H, new Point(705, 458), r);
        synthesizeFilterH(H, new Point(850, 391), r);
        synthesizeFilterH(H, new Point(993, 325), r);
        // H calculation (stop)

        // Filtering (start)
        Mat imgOut = new Mat();
        fftshift(H, H);
        filter2DFreq(imgIn, imgOut, H);
        // Filtering (stop)

        imgOut.convertTo(imgOut, CvType.CV_8U);
        Core.normalize(imgOut, imgOut, 0, 255, Core.NORM_MINMAX);
        Imgcodecs.imwrite("result.jpg", imgOut);
        Imgcodecs.imwrite("PSD.jpg", imgPSD);

        fftshift(H, H);
        Core.normalize(H, H, 0, 255, Core.NORM_MINMAX);
        Imgproc.imshow("Debluring", imgOut);
        Imgcodecs.imwrite("filter.jpg", H);

        // Wait for key press
        Imgproc.wait0(0);
    }

    // FFT shift function
    private static void fftshift(Mat inputImg, Mat outputImg) {
        outputImg = inputImg.clone();
        int cx = outputImg.cols() / 2;
        int cy = outputImg.rows() / 2;
        Mat q0 = new Mat(outputImg, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(outputImg, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(outputImg, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(outputImg, new Rect(cx, cy, cx, cy));
        Mat tmp = new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }

    // 2D frequency filtering
    private static void filter2DFreq(Mat inputImg, Mat outputImg, Mat H) {
        Mat planes[] = { new Mat(inputImg.clone(), CvType.CV_32F), Mat.zeros(inputImg.size(), CvType.CV_32F) };
        Mat complexI = new Mat();
        Core.merge(planes, 2, complexI);
        Core.dft(complexI, complexI, Core.DFT_SCALE);

        Mat planesH[] = { new Mat(H.clone(), CvType.CV_32F), Mat.zeros(H.size(), CvType.CV_32F) };
        Mat complexH = new Mat();
        Core.merge(planesH, 2, complexH);
        Mat complexIH = new Mat();
        Core.mulSpectrums(complexI, complexH, complexIH, 0);

        Core.idft(complexIH, complexIH);
        Core.split(complexIH, planes);
        outputImg = planes[0];
    }

    // Synthesizing the filter H
    private static void synthesizeFilterH(Mat inputOutput_H, Point center, int radius) {
        Point c2 = new Point(center.x, center.y);
        Point c3 = new Point(center.x, center.y);
        Point c4 = new Point(center.x, center.y);
        c2.y = inputOutput_H.rows() - center.y;
        c3.x = inputOutput_H.cols() - center.x;
        c4 = new Point(c3.x, c2.y);

        Imgproc.circle(inputOutput_H, center, radius, new Scalar(0), -1);
        Imgproc.circle(inputOutput_H, c2, radius, new Scalar(0), -1);
        Imgproc.circle(inputOutput_H, c3, radius, new Scalar(0), -1);
        Imgproc.circle(inputOutput_H, c4, radius, new Scalar(0), -1);
    }

    // Power Spectrum Density calculation
    private static void calcPSD(Mat inputImg, Mat outputImg, int flag) {
        Mat planes[] = { new Mat(inputImg.clone(), CvType.CV_32F), Mat.zeros(inputImg.size(), CvType.CV_32F) };
        Mat complexI = new Mat();
        Core.merge(planes, 2, complexI);
        Core.dft(complexI, complexI);

        Mat[] planesSplit = new Mat[2];
        Core.split(complexI, planesSplit);

        planesSplit[0].at(0, 0, 0);
        planesSplit[1].at(0, 0, 0);

        Mat imgPSD = new Mat();
        Core.magnitude(planesSplit[0], planesSplit[1], imgPSD);
        Core.pow(imgPSD, 2, imgPSD);
        outputImg = imgPSD;

        if (flag == 1) {
            Mat imglogPSD = new Mat();
            Core.add(imgPSD, new Scalar(1), imglogPSD);
            Core.log(imglogPSD, imglogPSD);
            outputImg = imglogPSD;
        }
    }
}
