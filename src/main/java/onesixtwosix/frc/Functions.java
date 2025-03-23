package onesixtwosix.frc;

import java.awt.Image;
// import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
// import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;

import com.sun.jna.Pointer;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;

import org.opencv.imgcodecs.Imgcodecs; // For reading images
import org.opencv.imgproc.Imgproc;

import net.sourceforge.lept4j.*;
import net.sourceforge.lept4j.util.*;

/**
 * A class of functions used throughout App.java for images, testing, and libraries.
 */
public class Functions {
    /**
     * Retrieves libraries' verisons, nothing complex
     * @return LinkedHashMap<> with library's name and version
     */
    public static Map<String, String> retrieveLibraries() {
        Map<String, String> libraries = new LinkedHashMap<>();
        String leptversion          = Leptonica1.getLeptonicaVersion().getString(0);
        String leptimagelibversions = Leptonica1.getImagelibVersions().getString(0);
        libraries.put("opencv", Core.VERSION);
        libraries.put("tess4j", TessAPI1.TessVersion());
        libraries.put("lept4j", leptversion);
        libraries.put("lept4j_imagelib", leptimagelibversions);
        return libraries;
    }    

    /**
     * Retrieve available VideoCaptures
     * @return Array of available VCs
     */
    public static List<Integer> retrieveCameras() {
        List<Integer> cameralist = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            VideoCapture tempCapture = new VideoCapture(i);

            if (tempCapture.isOpened()) {
                cameralist.add(i);
                tempCapture.release();
            }
        }
        return cameralist;
    }

    /**
     * Convert a given mat to a buffered image
     * @param mat
     * @return a buffered image of provided mat
     */
    public static BufferedImage Mat2BufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    public static BufferedImage Mat2BufferedImage2(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        int bufferSize = width * height * channels;
    
        byte[] data = new byte[bufferSize];
        mat.get(0, 0, data);
    
        BufferedImage image;
    
        if (channels == 1) {  // Grayscale
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            image.getRaster().setDataElements(0, 0, width, height, data);
        } else if (channels == 3) {  // BGR
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            final byte[] bgrData = new byte[width * height * 3];
    
            // Convert OpenCV's BGR to BufferedImage's BGR layout
            for (int i = 0; i < width * height; i++) {
                bgrData[i * 3] = data[i * 3 + 2];     // Blue
                bgrData[i * 3 + 1] = data[i * 3 + 1]; // Green
                bgrData[i * 3 + 2] = data[i * 3];     // Red
            }
            image.getRaster().setDataElements(0, 0, width, height, bgrData);
        } else {
            throw new IllegalArgumentException("Unsupported channel count: " + channels);
        }
    
        return image;
    }
    /**
     * Return wait image
     * @param img
     * @return wait image
     */
    public static Image ReturnWaitImage(Image... img) {
        File imageFile = new File("meetme.png");
        try {
            if (!imageFile.exists()) {
                System.err.println("an essential resource cannot be found.");
                return null;
            }
    
            return ImageIO.read(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    /**
     * A class with functions for testing and etc.
     */
    public class Testing {
        /**
         * Test OpenCV and Tesseract with a single immage
         * @param imagePath
         * @return output.jpg
         */
        public void testWithImage(String imagePath) {
            // Load the test image
            Mat frame = Imgcodecs.imread(imagePath);
            if (frame.empty()) {
                System.out.println("Error: Could not load image.");
                return;
            }
        
            // Convert to HSV color space
            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        
            // Define HSV ranges for red and blue
            Scalar lowerRed = new Scalar(0, 100, 100);
            Scalar upperRed = new Scalar(10, 255, 255);
            Scalar lowerBlue = new Scalar(100, 100, 100);
            Scalar upperBlue = new Scalar(140, 255, 255);
        
            // Create masks
            Mat maskRed = new Mat();
            Mat maskBlue = new Mat();
            Core.inRange(hsv, lowerRed, upperRed, maskRed);
            Core.inRange(hsv, lowerBlue, upperBlue, maskBlue);
        
            // Find contours
            List<MatOfPoint> contoursRed = new ArrayList<>();
            List<MatOfPoint> contoursBlue = new ArrayList<>();
            Imgproc.findContours(maskRed, contoursRed, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.findContours(maskBlue, contoursBlue, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
            // Draw bounding rectangles instead of contours
            for (MatOfPoint contour : contoursRed) {
                if (Imgproc.contourArea(contour) > 500) { // Ignore small areas (noise)
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(frame, rect, new Scalar(0, 0, 255), 3); // Red box
                }
            }
        
            for (MatOfPoint contour : contoursBlue) {
                if (Imgproc.contourArea(contour) > 500) {
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(frame, rect, new Scalar(255, 0, 0), 3); // Blue box
                }
            }
        
            // Save or display the output image
            Imgcodecs.imwrite("output.jpg", frame);
            System.out.println("Processed image saved as output.jpg");
        }
    }
}
