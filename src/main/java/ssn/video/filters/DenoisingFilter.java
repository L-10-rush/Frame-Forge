package ssn.video.filters;


import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_highgui;
import org.bytedeco.opencv.global.opencv_imgcodecs;


public class DenoisingFilter extends BaseFilter {
    @Override
    public void applyFilter(String inputFilePath) {
        Mat imgIn = imread(inputFilePath);

        
        // Ensure the width and height are even by using bitwise AND
        int evenWidth = imgIn.cols() & -2;
        int evenHeight = imgIn.rows() & -2;

        // Define the ROI with even dimensions
        Rect roi = new Rect(0, 0, evenWidth, evenHeight);

        // Crop the image to this ROI
        imgIn = new Mat(imgIn, roi);

        Mat imgPSD = calcPSD(imgIn, 0);
        imgPSD = fftshift(imgPSD);
        
        opencv_core.normalize(imgPSD, imgPSD, 0, 255, opencv_core.NORM_MINMAX, -1, null);
        
        Mat H = new Mat(roi.size(), opencv_core.CV_32F, new Scalar(1));
        int r = 21;
        synthesizeFilterH(H, new Point(705, 458), r);
        synthesizeFilterH(H, new Point(850, 391), r);
        synthesizeFilterH(H, new Point(993, 325), r);
        
        
        Mat imgOut;
        H = fftshift(H);
        imgOut = filter2Dfreq(imgIn, H);

        imgOut.convertTo(imgOut, opencv_core.CV_8U);
        opencv_core.normalize(imgOut, imgOut, 0, 255, opencv_core.NORM_MINMAX, -1 , null);
        opencv_imgcodecs.imwrite("output/denoised_result.jpg", imgOut);
        opencv_imgcodecs.imwrite("output/denoised_result_PSD.jpg", imgPSD);

        H = fftshift(H);
        opencv_core.normalize(H, H, 0, 255, opencv_core.NORM_MINMAX, -1 , null);
        opencv_highgui.imshow("Deblurring", imgOut);
        opencv_imgcodecs.imwrite("output/denoised_filter.jpg", H);
    }

    private Mat filter2Dfreq(Mat inputImg, Mat H){
        if (inputImg.channels() > 1) {
            opencv_imgproc.cvtColor(inputImg, inputImg, opencv_imgproc.COLOR_BGR2GRAY);
        }
        inputImg.convertTo(inputImg, opencv_core.CV_32F);

        Mat planes[] = new Mat[2];
        planes[0] = inputImg.clone();
        
        planes[1] = new Mat(inputImg.size(), opencv_core.CV_32F, new Scalar(0));
        MatVector matVec = new MatVector(planes);
        
        Mat complexI = new Mat();
        opencv_core.merge(matVec, complexI);
        opencv_core.dft(complexI, complexI, opencv_core.DFT_SCALE, 0);
        
        Mat planesH[] = new Mat[2];
        planesH[0] = H.clone();
        planesH[1] = new Mat(H.size(), opencv_core.CV_32F, new Scalar(0));
        
        Mat complexH = new Mat();
        opencv_core.merge(new MatVector(planesH), complexH);
        
        Mat complexIH = new Mat();
        opencv_core.mulSpectrums(complexI, complexH, complexIH, 0);

        opencv_core.idft(complexIH, complexIH);
        opencv_core.split(complexIH, matVec);

        return matVec.get(0);
    }

    private Mat synthesizeFilterH(Mat inputOutput_H, Point center, int radius){
        Point c2 = center;
        Point c3 = center;
        Point c4 = center;

        c2.y(inputOutput_H.rows() - center.y());
        c3.x(inputOutput_H.cols() - center.x());

        c4 = new Point(c3.x(), c2.y());

        opencv_imgproc.circle(inputOutput_H, center, radius, new Scalar(0), opencv_imgproc.FILLED, 8, 0);
        opencv_imgproc.circle(inputOutput_H, c2, radius, new Scalar(0), opencv_imgproc.FILLED, 8, 0);
        opencv_imgproc.circle(inputOutput_H, c3, radius, new Scalar(0), opencv_imgproc.FILLED, 8, 0);
        opencv_imgproc.circle(inputOutput_H, c4, radius, new Scalar(0), opencv_imgproc.FILLED, 8, 0);

        return inputOutput_H;
    }

    private Mat fftshift(Mat inputImg){
        Mat outputImg = inputImg.clone();
        int cx = outputImg.cols()/2;
        int cy = outputImg.rows()/2;

        Mat q0 = new Mat(outputImg, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(outputImg, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(outputImg, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(outputImg, new Rect(cx, cy, cx, cy));

        Mat tmp =  new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);

        return outputImg;
    }

    private Mat calcPSD(Mat inputImg, int flag){
        Mat imgFloat = new Mat();
        inputImg.convertTo(imgFloat, opencv_core.CV_32F);

        Mat planes[] = new Mat[2];
        planes[0] = imgFloat;
        planes[1] = new Mat(inputImg.size(), opencv_core.CV_32F);

        MatVector matVec = new MatVector(planes);

        Mat complexI = new Mat(inputImg.size(), opencv_core.CV_32FC2);
        opencv_core.merge(matVec, complexI);

        if (complexI.type() != opencv_core.CV_32FC2) {
            MatVector channels = new MatVector();
            opencv_core.split(complexI, channels);
        
            MatVector twoChannelVector = new MatVector(channels.get(0), channels.get(1));
            Mat newComplexI = new Mat();
            opencv_core.merge(twoChannelVector, newComplexI);

            complexI = newComplexI; ////LESGOOOO
        }
        opencv_core.dft(complexI, complexI);

        MatVector splitPlanes = new MatVector(2);
        opencv_core.split(complexI, splitPlanes);

        planes[0] = splitPlanes.get(0);
        planes[1] = splitPlanes.get(1);

        Mat imgPSD = new Mat();
        opencv_core.magnitude(planes[0], planes[1], imgPSD);

        opencv_core.pow(imgPSD, 2, imgPSD);

        Mat outputImg = imgPSD;
        
        if (flag == 1){
            Mat imglogPSD;
            imglogPSD = new Mat(opencv_core.add(imgPSD, new Scalar(1.0)));
            opencv_core.log(imglogPSD, imglogPSD);
            outputImg = imglogPSD;
        }

        return outputImg;
    }

    public static void main(String[] args) {
        DenoisingFilter defo = new DenoisingFilter();
        try {
            defo.applyFilter("src\\main\\resources\\period_input.jpg");
        } finally {
            System.out.println("RRRRRARAAAAAAAAAHHHHH");
        }
    }
}
