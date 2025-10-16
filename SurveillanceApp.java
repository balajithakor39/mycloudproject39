package com.example.surveillance;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SurveillanceApp {
    private JFrame frame;
    private JLabel videoLabel;
    private volatile boolean running = false;
    private MotionDetector motionDetector;
    private FaceDetector faceDetector;
    private VideoCapture capture;

    public SurveillanceApp() {
        motionDetector = new MotionDetector();
        faceDetector = new FaceDetector();
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("Simple AI Surveillance System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());

        videoLabel = new JLabel();
        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(videoLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton startBtn = new JButton("Start");
        JButton stopBtn = new JButton("Stop");
        bottom.add(startBtn);
        bottom.add(stopBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startCapture());
        stopBtn.addActionListener(e -> stopCapture());
    }

    public void start() {
        frame.setVisible(true);
    }

    private void startCapture() {
        if (running) return;
        running = true;
        new Thread(this::captureLoop).start();
    }

    private void stopCapture() {
        running = false;
        if (capture != null && capture.isOpened()) capture.release();
    }

    private void captureLoop() {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(frame, "Cannot open webcam", "Error", JOptionPane.ERROR_MESSAGE);
            running = false;
            return;
        }

        Mat mat = new Mat();
        File eventsDir = new File("events");
        if (!eventsDir.exists()) eventsDir.mkdirs();
        long lastEventTime = 0;

        while (running) {
            if (!capture.read(mat) || mat.empty()) continue;

            Mat resized = new Mat();
            opencv_imgproc.resize(mat, resized, new org.bytedeco.opencv.opencv_core.Size(640, 480));

            boolean motion = motionDetector.detect(resized);
            List<org.bytedeco.opencv.opencv_core.Rect> faces = faceDetector.detectFaces(resized);

            Mat display = resized.clone();
            if (motion) {
                opencv_imgproc.putText(display, "MOTION DETECTED", new org.bytedeco.opencv.opencv_core.Point(10, 30),
                        opencv_imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new org.bytedeco.opencv.opencv_core.Scalar(0, 0, 255, 0));
            }

            for (org.bytedeco.opencv.opencv_core.Rect face : faces) {
                opencv_imgproc.rectangle(display, face, new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0), 2, 8, 0);
            }

            BufferedImage img = Utils.matToBufferedImage(display);
            if (img != null) videoLabel.setIcon(new ImageIcon(img));

            long now = System.currentTimeMillis();
            if ((motion || !faces.isEmpty()) && now - lastEventTime > 2000) {
                lastEventTime = now;
                saveSnapshot(resized, motion, faces.size());
            }

            try { Thread.sleep(33); } catch (InterruptedException ignored) {}
        }

        capture.release();
    }

    private void saveSnapshot(Mat frame, boolean motion, int faces) {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String name = "events/event_" + ts + (motion ? "_motion" : "") + (faces > 0 ? "_face" + faces : "") + ".jpg";
            opencv_imgcodecs.imwrite(name, frame);
            System.out.println("Saved event: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
