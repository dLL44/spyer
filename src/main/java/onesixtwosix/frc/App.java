package onesixtwosix.frc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
// import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.*;

public class App {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Declare globals
        Scanner inputScanner = new Scanner(System.in);
        Integer cameraIndex = 0;

        System.out.println("spyer\n---\nget cooking");

        // Retrieve library info
        System.out.println("\nlibraries:");
        Functions.retrieveLibraries().forEach((lib, ver) ->
            System.out.println(lib + "::" + ver));

        // Find available video captures
        List<Integer> cameras = Functions.retrieveCameras();
        System.out.println("\ncameras:");
        System.out.println(cameras);

        // If multiple cameras exist, let the user pick one
        if (cameras.size() > 1) {
            System.err.println("Pick an index, or by default 0 will be chosen:");
            cameraIndex = inputScanner.nextInt();
            if (cameraIndex < 0 || cameraIndex >= cameras.size()) {
                System.err.println("Invalid, setting to 0.");
                cameraIndex = 0;
            }
        }

        // Create VideoCapture
        VideoCapture capture = new VideoCapture(cameras.get(cameraIndex));
        if (!capture.isOpened()) {
            System.err.println("Frame capture failed. (capture not opened)");
            return;
        }

        // Create JFrame with a custom JPanel
        JFrame mainFrame = new JFrame("spyer - camera feed");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1400, 700);
        mainFrame.setResizable(false);
        mainFrame.setAlwaysOnTop(false);
        mainFrame.setBackground(Color.DARK_GRAY);
        
        VideoPanel videoPanel = new VideoPanel(capture);
        mainFrame.add(videoPanel);
        mainFrame.setVisible(true);

        // Start video update loop
        new Thread(videoPanel).start();
    }
}

// Custom JPanel for displaying camera feeds
class VideoPanel extends JPanel implements Runnable {
    private final VideoCapture capture;
    private Image processedImage;
    private Image regularImage;
    private String res;

    public VideoPanel(VideoCapture capture) {
        this.capture = capture;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (regularImage != null && processedImage != null) {
            // Draw background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 1400, 700);


            // Draw regular feed
            g2d.drawImage(regularImage, 0, 0, 640, 640, this);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 140, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString("regular", 10, 15);

            // Draw processed feed
            g2d.drawImage(processedImage, 730, 0, 640, 640, this);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(730, 0, 180, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString("processed", 740, 15);

            // Draw OCR
            g2d.setColor(Color.WHITE);
            g2d.fillRect(730, 640, 500, 30); // White background for text
            g2d.setColor(Color.BLACK);
            g2d.drawString("OCR: " + res, 740, 660);

        }
    }

    @Override
    public void run() throws ArrayIndexOutOfBoundsException {
        ITesseract tess = new Tesseract();
        tess.setLanguage("eng");
        tess.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        Mat frame = new Mat();
        while (true) {
            capture.read(frame);
            if (frame.empty()) {
                System.err.println("Frame capture failed (frame is empty)");
                break;
            }

            // Convert frame to RGB
            Mat regRGB = new Mat();
            Imgproc.cvtColor(frame, regRGB, Imgproc.COLOR_BGR2RGB);

            // Convert to HSV for processing
            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

            // Red color range in HSV
            Scalar lowerRed1 = new Scalar(0, 120, 70);
            Scalar upperRed1 = new Scalar(10, 255, 255);
            Scalar lowerRed2 = new Scalar(170, 120, 70);
            Scalar upperRed2 = new Scalar(180, 255, 255);

            // Blue color range in HSV
            Scalar lowerBlue = new Scalar(100, 150, 0);
            Scalar upperBlue = new Scalar(140, 255, 255);
            Scalar lowerBlue2 = new Scalar(85, 150, 50);
            Scalar upperBlue2 = new Scalar(110, 255, 255);
            Scalar lowerBlue3 = new Scalar(105, 100, 20);
            Scalar upperBlue3 = new Scalar(130, 255, 255);


            // Create masks for red and blue colors
            Mat maskRed1 = new Mat();
            Mat maskRed2 = new Mat();
            Mat maskBlue = new Mat();
            
            Core.inRange(hsv, lowerRed1, upperRed1, maskRed1);
            Core.inRange(hsv, lowerRed2, upperRed2, maskRed2);
            Core.inRange(hsv, lowerBlue, upperBlue, maskBlue);

            Mat maskBlue2 = new Mat();
            Mat maskBlue3 = new Mat();

            Core.inRange(hsv, lowerBlue2, upperBlue2, maskBlue2);
            Core.inRange(hsv, lowerBlue3, upperBlue3, maskBlue3);

            Mat finalBlueMask = new Mat();
            Core.add(maskBlue, maskBlue2, finalBlueMask);
            Core.add(finalBlueMask, maskBlue3, finalBlueMask);

            Mat redMask = new Mat();
            Core.add(maskRed1, maskRed2, redMask);

            // Find contours for red and blue
            List<MatOfPoint> contoursRed = new ArrayList<>();
            Mat hierarchyRed = new Mat();
            Imgproc.findContours(redMask, contoursRed, hierarchyRed, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            List<MatOfPoint> contoursBlue = new ArrayList<>();
            Mat hierarchyBlue = new Mat();
            Imgproc.findContours(finalBlueMask, contoursBlue, hierarchyBlue, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Draw contours
            Imgproc.drawContours(frame, contoursRed, -1, new Scalar(0, 0, 255), 3); // Red contours in BGR
            Imgproc.drawContours(frame, contoursBlue, -1, new Scalar(255, 0, 0), 3); // Blue contours in BGR

            Mat grey = new Mat();
            Imgproc.cvtColor(frame, grey, Imgproc.COLOR_BGR2GRAY);
            Mat thresh = new Mat();
            Imgproc.threshold(grey, thresh, 150, 255, BufferedImage.TYPE_BYTE_GRAY);

            List<MatOfPoint> contours = new ArrayList<>();  
            Mat hierarchy = new Mat();
            Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            try {
                BufferedImage textImage = Functions.Mat2BufferedImage2(thresh); // ArrayIndexOutOfBounds - possibly buffer overflow protection?
                if (textImage == null) {
                    res = "ocr err (null image)";
                } else {
                    res = tess.doOCR(textImage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res = "ocr err";
            }

            Mat processedFrame = new Mat();
            Imgproc.cvtColor(frame, processedFrame, Imgproc.COLOR_BGR2RGB);

            // Update images for rendering
            regularImage = Functions.Mat2BufferedImage(regRGB);
            processedImage = Functions.Mat2BufferedImage(processedFrame);

            // Repaint the panel
            repaint();

            // Delay to control frame rate
            try {
                Thread.sleep(15); // around 40-50 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
