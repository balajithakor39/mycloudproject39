package com.example.surveillance;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FaceDetector {
    private CascadeClassifier classifier;

    public FaceDetector() {
        try {
            InputStream is = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            File tmp = File.createTempFile("haarcascade_frontalface_default", ".xml");
            tmp.deleteOnExit();
            if (is != null) {
                try (FileOutputStream fos = new FileOutputStream(tmp)) {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = is.read(buf)) != -1) fos.write(buf, 0, r);
                }
                classifier = new CascadeClassifier(tmp.getAbsolutePath());
            } else {
                classifier = new CascadeClassifier();
            }
        } catch (Exception e) {
            e.printStackTrace();
            classifier = new CascadeClassifier();
        }
    }

    public List<org.bytedeco.opencv.opencv_core.Rect> detectFaces(Mat frame) {
        List<org.bytedeco.opencv.opencv_core.Rect> out = new ArrayList<>();
        try {
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);
            RectVector faces = new RectVector();
            classifier.detectMultiScale(gray, faces);
            for (int i = 0; i < faces.size(); i++) {
                out.add(faces.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}
