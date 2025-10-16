package com.example.surveillance;

import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Utils {
    public static BufferedImage matToBufferedImage(Mat mat) {
        try {
            org.bytedeco.opencv.opencv_core.Mat matRgb = new org.bytedeco.opencv.opencv_core.Mat();
            org.bytedeco.opencv.global.opencv_imgproc.cvtColor(mat, matRgb, org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2RGB);
            byte[] data = new byte[(int) (matRgb.total() * matRgb.channels())];
            matRgb.data().get(data);
            BufferedImage image = new BufferedImage(matRgb.cols(), matRgb.rows(), BufferedImage.TYPE_3BYTE_BGR);
            final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(data, 0, targetPixels, 0, data.length);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
