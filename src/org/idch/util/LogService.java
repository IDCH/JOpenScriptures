package org.idch.util;

// This code is adapted from Checkpoint by Shannon Hardt et al.
// The project is available at sourceforge. This has been adapted
// for use by the CSDL by Neal Audenaert

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>LogService</code> class hides the complexities of the underlying logging
 * implementation from the framework user.  The api is deliberately kept as simple
 * as possible to minimize both the effort in using it and also the structural impact it
 * has on code.  The framework logging service is mainly a simple wrapper around log4j.
 * NOTE: log4j.properties MUST be in the classpath
 *
 * @see <a href="http://logging.apache.org/">log4j</a>
 */
public class LogService {
    /**
     * Categories are a fundamental part of the log4j architecture, the service
     * as is basically wraps one and feeds it with the requests it gets to log 
     * information.
     */
    // Initialize logging service
    static {
        BootLogger.logDebug("LogService:: Initialization Begin");
        
        File conf = Filenames.getFile("log4j");
        if (conf == null) {
            // If we can't get the file this way - just look for the default file
            conf = new File("log4j.properties");
        }
        System.out.println("Using log configuration file:" + conf.getAbsolutePath());
        Properties props = new Properties();
        if (conf.exists()) {
            try { props.load(new FileInputStream(conf)); }
            catch (IOException ioe) { props = null; }
        } else props = null;
          
        if (props == null || props.isEmpty()) {
            PropertyConfigurator.configure("log4j.properties");
        } else {
            PropertyConfigurator.configure(props);
        }
        
        //this class from log4j does all the work involved in setting up the file
        //appenders etc. using the configuration as specified in the lcf file
        //the file will be checked periodically for changes - default 60 seconds
        
        BootLogger.logDebug("LogService:: Initialization Success");
    }

    /**
     * Logs a debug message.  The action taken will depend on how log4j has been set up.
     * By default, debug messages are printed to both stdout and to file.
     * @param inMessage - the String debug message to log
     * @deprecated Use logDebug(String inMessage, String logger)
     */
    public static void logDebug(String inMessage) {
        Logger.getRootLogger().debug(inMessage);
    }
    
    /**
     * Logs an informational message. The action taken will depend on how log4j has been set up.
     * By default, info messages are printed both to stdout and to file.
     * @param inMessage - the String info message to log
     * @deprecated Use logInfo(String inMessage, String logger)
     */
    public static void logInfo(String inMessage) {
        Logger.getRootLogger().info(inMessage);
    }


    /**
     * Logs a warning message.  The action taken will depend on how log4j has been set up.
     * By default, warn messages are printed to both stdout and to file.
     * @param inMessage - the String warn message to log
     * @deprecated Use logWarn(String inMessage, String logger)
     */
    public static void logWarn(String inMessage) {
        Logger.getRootLogger().warn(inMessage);
    }


    /**
     * Logs an error message.  The action taken will depend on how log4j has been set up.
     * By default, error messages are printed to both stdout and to file.
     * @param inMessage - the String error message to log
     * @deprecated Use logError(String inMessage, String logger)
     */
    public static void logError(String inMessage) {
        Logger.getRootLogger().error(inMessage);
    }
    
    /**
     * Logs an error message and stack trace for a Throwable.  
     * The action taken will depend on how log4j has been set up.
     * By default, error messages are printed to both stdout and to file.
     * @param inMessage - the String error message to log
     * @param e - the Throwable whose stack trace will be logged
     * @deprecated Use logError(String inMessage, String logger, Throwable e)
     */
    public static void logError(String inMessage, Throwable e) {
        Logger.getRootLogger().error(inMessage, e);
    }
    
    ////////////////////////////////////////////////////////////////////////
    //  specify logger to use
    ////////////////////////////////////////////////////////////////////////
    /**
     * Logs a debug message.  The action taken will depend on how log4j has been set up.
     * By default, debug messages are printed to both stdout and to file.
     * @param inMessage - the String debug message to log
     * @param logger - the Logger to use for logging: convention is class name
     */
    public static void logDebug(String inMessage, String logger) {
//        System.out.println("DEBUG - " + logger + " :: " + inMessage);
        Logger.getLogger(logger).debug(inMessage);
    }
    
    /**
     * Logs an informational message. The action taken will depend on how log4j has been set up.
     * By default, info messages are printed both to stdout and to file.
     * @param inMessage - the String info message to log
     * @param logger - the Logger to use for logging: convention is class name
     */
    public static void logInfo(String inMessage, String logger) {
//        System.out.println("INFO  - " + logger + " :: " + inMessage);
        Logger.getLogger(logger).info(inMessage);
    }


    /**
     * Logs a warning message.  The action taken will depend on how log4j has been set up.
     * By default, warn messages are printed to both stdout and to file.
     * @param inMessage - the String warn message to log
     * @param logger - the Logger to use for logging: convention is class name
     */
    public static void logWarn(String inMessage, String logger) {
//        System.out.println("WARN  - " + logger + " :: " + inMessage);
        Logger.getLogger(logger).warn(inMessage);
    }
    /**
     * Logs a warning message and stack trace for a Throwable.  
     * The action taken will depend on how log4j has been set up.
     * 
     * @param inMessage - the String error message to log
     * @param logger - the Logger to use for logging: convention is class name
     * @param e - the Throwable whose stack trace will be logged
     */
    public static void logWarn(String inMessage, String logger, Throwable e) {
        Logger.getLogger(logger).warn(inMessage, e);
    }

    /**
     * Logs an error message.  The action taken will depend on how log4j has been set up.
     *
     * @param inMessage - the String error message to log
     * @param logger - the Logger to use for logging: convention is class name
     */
    public static void logError(String inMessage, String logger) {
//        System.out.println("ERROR - " + logger + " :: " + inMessage);
        Logger.getLogger(logger).error(inMessage);
    }
    
    /**
     * Logs an error message and stack trace for a Throwable.  
     * The action taken will depend on how log4j has been set up.
     * 
     * @param inMessage - the String error message to log
     * @param logger - the Logger to use for logging: convention is class name
     * @param e - the Throwable whose stack trace will be logged
     */
    public static void logError(String inMessage, String logger, Throwable e) {
//        System.out.println("ERROR - " + logger + " :: " + inMessage);
        e.printStackTrace();
        Logger.getLogger(logger).error(inMessage, e);
    }
    
    /**
     * Alias for {@link #logDebug(String, String)}. 
     * 
     * @param inMessage the String message to log
     * @param logger the Logger to use for logging: convention is class name
     */
    public static void debug(String inMessage, String logger) {
        logDebug(inMessage, logger);
    }
    
    /**
     * Alias for {@link #logInfo(String, String)}. 
     * 
     * @param inMessage the String message to log
     * @param logger the Logger to use for logging: convention is class name
     */
    public static void info(String inMessage, String logger) {
        logInfo(inMessage, logger);
    }
    
    /**
     * Alias for {@link #logWarn(String, String)}. 
     * 
     * @param inMessage the String message to log
     * @param logger the Logger to use for logging: convention is class name
     */
    public static void warn(String inMessage, String logger) {
        logWarn(inMessage, logger);
    }
    
    /**
     * Alias for {@link #logError(String, String)}. 
     * 
     * @param inMessage the String message to log
     * @param logger the Logger to use for logging: convention is class name
     */
    public static void error(String inMessage, String logger) {
        logError(inMessage, logger);
    }
    
    /**
     * Alias for {@link #logError(String, String, Throwable)}. 
     * 
     * @param inMessage the String message to log
     * @param logger the Logger to use for logging: convention is class name
     * @param e the Throwable whose stack trace will be logged
     */
    public static void debug(String inMessage, String logger, Throwable e) {
        logError(inMessage, logger, e);
    }
}
