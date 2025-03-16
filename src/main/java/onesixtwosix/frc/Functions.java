package onesixtwosix.frc;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import java.lang.String;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        int Index = 0;

        while (true) {
            VideoCapture tempCapture = new VideoCapture(Index);
            if (!tempCapture.isOpened()) {
                break;
            } else {
                cameralist.add(Index);
            }
            Index++;
        }
        return cameralist;
    }
}
