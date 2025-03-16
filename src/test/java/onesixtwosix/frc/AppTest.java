package onesixtwosix.frc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static List<Integer> retrieveCameras2() {
        File f = new File("/dev");
        File[] cameras = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("video");
            }
        });

        // Create a list to store the indices of the cameras
        List<Integer> cameraIndices = new ArrayList<>();

        // Check if the cameras array is not null and has elements
        if (cameras != null) {
            // Iterate through the cameras and add indices to the list
            for (int i = 0; i < cameras.length; i++) {
                cameraIndices.add(i);  // Add the index to the list
            }
        }

        // Return the list of indices
        return cameraIndices;
    }
}
