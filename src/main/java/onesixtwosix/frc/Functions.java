package onesixtwosix.frc;

import java.awt.Image;
// import java.awt.*;
import java.awt.image.BufferedImage;

// import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Functions {
    /**
     * Retrieves libraries' verisons, nothing complex
     * @return LinkedHashMap<> with library's name and version
     */
    public static Map<String, String> retrieveLibraries() {
        Map<String, String> libraries = new LinkedHashMap<>();
        libraries.put("opencv", Core.VERSION);
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
    
}
