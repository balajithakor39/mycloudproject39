package com.example.surveillance;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

public class MotionDetector {
    private Mat prevGray = null;
    private final double THRESHOLD = 30.0;
    private final double MOTION_PERCENT = 0.01;

    public synchronized boolean detect(Mat frame) {
        try {
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);
            opencv_imgproc.GaussianBlur(gray, gray, new org.bytedeco.opencv.opencv_core.Size(21,21), 0);

            if (prevGray == null) {
                prevGray = gray.clone();
                return false;
            }

            Mat diff = new Mat();
            opencv_core.absdiff(prevGray, gray, diff);
            opencv_imgproc.threshold(diff, diff, THRESHOLD, 255, opencv_imgproc.THRESH_BINARY);

            int nonZero = opencv_core.countNonZero(diff);
            int total = diff.rows() * diff.cols();
            double frac = (double) nonZero / (double) total;

            prevGray = gray.clone();
            return frac > MOTION_PERCENT;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
