package org.idch.util;

// This code is adapted from Checkpoint by Shannon Hardt et al.
// The project is available at sourceforge. This has been adapted
// for use by the CSDL by Neal Audenaert
 
/**
 * This logging service is to be used by the framework, 
 * before the logging service has been initialized.
 */
public class BootLogger  {
    public static final int SYSTEM_OUT   = 1;
    public static final int SYSTEM_ERROR = 2;
    public static final int SYSTEM_BOTH  = 3;

    private static int log_output = BootLogger.SYSTEM_OUT;
    private static boolean DEBUG  = true;
    
    /** Creates new BootLogger */
    public BootLogger() {}
    
    public static void setDebug( boolean val ) { BootLogger.DEBUG = val;  }
    
    public static void setOutput( int val ) { BootLogger.log_output = val; }
    
    public static void log( String message )  {
        switch ( BootLogger.log_output ) {
            case BootLogger.SYSTEM_OUT:
                System.out.println( message );
                break;
            case BootLogger.SYSTEM_ERROR:
                System.err.println( message );
                break;
            default:
                System.out.println( message );
                System.err.println( message );
                break;
        }
    }
    
    public static void logDebug( String message ) {
        if ( BootLogger.DEBUG ) {
            switch ( BootLogger.log_output ) {
                case BootLogger.SYSTEM_OUT:
                    System.out.println( message );
                    break;
                case BootLogger.SYSTEM_ERROR:
                    System.err.println( message );
                    break;
                default:
                    System.out.println( message );
                    System.err.println( message );
                    break;
            }
        }
    }

}
