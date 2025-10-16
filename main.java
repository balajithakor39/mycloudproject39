package com.example.surveillance;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SurveillanceApp app = new SurveillanceApp();
            app.start();
        });
    }
}
