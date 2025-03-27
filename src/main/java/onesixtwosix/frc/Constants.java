package onesixtwosix.frc;

import java.nio.file.*;
import onesixtwosix.frc.Functions;

/**
 * The name is self explanatory
 */
public class Constants {
    /**
     * Messing around
     */
    public static class Chaos {
        /** Used for testing values at a max capacity */
        final static int Absurdity    = 100000000;
        /** Used for testing values at a -max capacity */
        final static int Logicalivity = -100000000;
    }   

    /** Filenames in String */
    public static class Filenames {
        final static String GLOBALDATA = Functions.getFilenamePath("global_data.json");
        
    }
}
