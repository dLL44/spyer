package onesixtwosix.frc;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import onesixtwosix.frc.Functions;
import java.util.*;

public class App 
{
    public static void main( String[] args )
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Scanner inputScanner = new Scanner(System.in);

        System.out.println("spyer\n---\nget cooking");
        
        // retrieve library info
        System.out.println("\nlibraries:");
        Functions.retrieveLibraries().forEach((lib, ver) ->
            System.out.println(lib + "::" + ver));
        
        // find available video captures
        List<Integer> cameras = Functions.retrieveCameras();
        System.out.println("\ncameras:");
        System.out.println(cameras);

        if (cameras.size() > 1) {
            System.out.println("pick an index, or by default 0 will be chosen:");
            
        }
    }
}
 