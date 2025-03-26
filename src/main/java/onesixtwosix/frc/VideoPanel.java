package onesixtwosix.frc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import net.sourceforge.tess4j.*;

// Custom JPanel for displaying camera feeds
public class VideoPanel extends JPanel implements Runnable {
    private final VideoCapture capture;
    private Image processedImage;
    private Image regularImage;
    private String res;
    private Mat frame = new Mat();
    private int teamNumberFilter; // set to ours as an example
    public  int threadSleep;

    public VideoPanel(VideoCapture capture, int teamNoFilter, int threadSleep) {
        this.capture = capture;
        this.teamNumberFilter = teamNoFilter;
        this.threadSleep = threadSleep;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Going off of #3b3f42
        Color bgColor = new Color(0x3b, 0x3f, 0x42);
        // Convert RGB to HSB
        float[] bgHSB = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);


        if (regularImage != null && processedImage != null) {
            if (!frame.empty()) {
                // Draw background
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(0, 0, 1400, 700);


                // Draw regular feed
                g2d.drawImage(regularImage, 0, 0, 640, 640, this);
                g2d.setColor(Color.getHSBColor(bgHSB[0], bgHSB[1], bgHSB[2]));
                g2d.fillRect(0, 0, 140, 20);
                g2d.setColor(Color.white);
                g2d.drawString("regular", 10, 15);

                // Draw processed feed
                g2d.drawImage(processedImage, 660, 0, 640, 640, this);
                g2d.setColor(Color.getHSBColor(bgHSB[0], bgHSB[1], bgHSB[2]));
                g2d.fillRect(660, 0, 180, 20);
                g2d.setColor(Color.white);
                g2d.drawString("processed", 670, 15);

                // Draw OCR
                g2d.setColor(Color.getHSBColor(bgHSB[0], bgHSB[1], bgHSB[2]));
                g2d.fillRect(0, 620, 1400, 30);
                g2d.setColor(Color.white);
                g2d.drawString("OCR: " + res, 10, 635);
            } 
        }
    }

    @Override
    public void run() {
        ITesseract tess = new Tesseract();
        tess.setLanguage("eng");
        tess.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tess.setVariable("tessedit_char_whitelist", Integer.toString(teamNumberFilter));

        final int SIZEMAXRECT    = 1000;
        final int SIZEMINCONTOUR = 100;
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
            Mat maskBlue2 = new Mat();
            Mat maskBlue3 = new Mat();

            Core.inRange(hsv, lowerRed1, upperRed1, maskRed1);
            Core.inRange(hsv, lowerRed2, upperRed2, maskRed2);
            Core.inRange(hsv, lowerBlue, upperBlue, maskBlue);
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
    
            // Draw contours (outline of the robot)
            Imgproc.drawContours(frame, contoursRed, -1, new Scalar(0, 0, 255), 3); // Red contours in BGR
            Imgproc.drawContours(frame, contoursBlue, -1, new Scalar(255, 0, 0), 3); // Blue contours in BGR
    
            // Filter out small contours (noise reduction)
            List<MatOfPoint> filteredContoursRed = new ArrayList<>();
            for (MatOfPoint contour : contoursRed) {
                if (Imgproc.contourArea(contour) > SIZEMINCONTOUR) { // Minimum area threshold
                    filteredContoursRed.add(contour);
                }
            }
    
            List<MatOfPoint> filteredContoursBlue = new ArrayList<>();
            for (MatOfPoint contour : contoursBlue) {
                if (Imgproc.contourArea(contour) > SIZEMINCONTOUR) { // Minimum area threshold
                    filteredContoursBlue.add(contour);
                }
            }
    
            // Draw filtered contours (outline of the robot)
            Imgproc.drawContours(frame, filteredContoursRed, -1, new Scalar(0, 0, 255), 3); // Red contours in BGR
            Imgproc.drawContours(frame, filteredContoursBlue, -1, new Scalar(255, 0, 0), 3); // Blue contours in BGR


            for (MatOfPoint contour : contoursRed) {
                if (Imgproc.contourArea(contour) > SIZEMAXRECT) { // Ignore small areas (noise)
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(frame, rect, new Scalar(0, 0, 255), 3); // Red box
                }
            }
            
            for (MatOfPoint contour : contoursBlue) {
                if (Imgproc.contourArea(contour) > SIZEMAXRECT) {
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(frame, rect, new Scalar(255, 0, 0), 3); // Blue box
                }
            }
            
                
            Mat grey = new Mat();
            Imgproc.cvtColor(frame, grey, Imgproc.COLOR_BGR2GRAY);
            Mat thresh = new Mat();
            Imgproc.threshold(grey, thresh, 150, 255, BufferedImage.TYPE_BYTE_GRAY);

            List<MatOfPoint> contours = new ArrayList<>();  
            Mat hierarchy = new Mat();
            Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Update processed image
            Mat processedFrame = new Mat();
            Imgproc.cvtColor(frame, processedFrame, Imgproc.COLOR_BGR2RGB);

            try {
                BufferedImage textImage = Functions.Mat2BufferedImage2(thresh);
                if (textImage == null) {
                    res = "ocr err";
                } else {
                    res = tess.doOCR(textImage);
                }
                res = res.replaceAll("[^0-9]", "");
                if (res.contains(Integer.toString(teamNumberFilter))) {
                    res = Integer.toString(teamNumberFilter);
                } else {
                    res = res.replaceAll("[^0-9]", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
                res = "ocr err (check stacktrace)";
            }

            // try {
            //     BufferedImage textImage = Functions.Mat2BufferedImage2(thresh); // ArrayIndexOutOfBounds - possibly buffer overflow protection?
            //     if (textImage == null) {
            //         res = "ocr err (null image)";
            //     } else {
            //         res = tess.doOCR(textImage);
            //         // Filter and get numbers
            //         res = res.replaceAll("[^0-9]", "");
                    
            //         if (!res.isEmpty()) {
            //             int resInt = Integer.parseInt(res);
            //             int[] res25 = new int[10];

            //             for (int i=0;i>10;i++) {
            //                 res25[i] = resInt;
            //             }

            //             System.out.println(Arrays.toString(res25));
            //         } else {
            //             System.err.println("no valid numbers found");
            //         }
            //     }
            // } catch (Exception e) {
            //     e.printStackTrace();
            //     res = "ocr err";
            // }


    
            // Update images for rendering
            regularImage = Functions.Mat2BufferedImage(regRGB);             // DO NOT
            processedImage = Functions.Mat2BufferedImage(processedFrame);   //  CONVERT TO Mat2
    
            // Repaint the panel
            repaint();
    
            // Delay to control frame rate
            try {
                Thread.sleep(1); // around 40-50 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
        
}
