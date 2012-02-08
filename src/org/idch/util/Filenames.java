/* Created on Jan 17, 2005
 * Author: Neal Audenaert (neal@idch.org)
 * 
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH) 
 *           All Rights Reserved.
 */
package org.idch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class provides basic utilities for managing and manipulating file and
 * for specifying the files to be used in an application via a java properties
 * file. This expects to find the properties file 
 * <code>edu.tamu.csdl.filenames</code>.
 * 
 * @author Neal Audenaert
 */
public class Filenames {
    // I've imported Ryan Suander's utilities (org.idch.index.Utilities) class
    // into here. The result is a bit of a Frankenstien.
    // TODO unify these resources
    // TODO code review
    
    private static final String LOGGER = Filenames.class.getName();
    private static final String BUNDLE_NAME = "filenames";

    private static ResourceBundle RESOURCE_BUNDLE = null;
    
    static { 
        try {
            RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
        } catch (MissingResourceException mre) { }
    }
    private Filenames() { }

    /**
     * Indicates whether the base directory has a a subdirectory with the
     * specified name.
     * 
     * @param base The directory whose sub-directories are to be tested.
     * @param dirname Then name of the sub-directory to test for.
     * 
     * @return True if the indicated sub-directory exists, false if it does not
     */
    public static boolean hasSubdirectory(File base, String dirname) {
        File directory = new File(base, dirname);
        return (directory.exists() && directory.isDirectory());
    }
    
    /**
     * Takes a string based name and normalizes it for the purpose of creating 
     * a directory or file. This is used to remove non-white space or other 
     * characters which may not be well supported by the underlying filesystem.
     * 
     * @param name The name of the edition to be normalized.
     * @return The normalized name.
     */
    public static String normalize(String name) {
        String result = name.replaceAll("\\W", "_");
        return result;
    }
    
    /**
     * Returns the name of the file specified in the resource bundle 
     * <code>edu.tamu.csdl.filenames</code> for the provided key.
     *  
     * @param key The key identifying a filename in the resource bundle
     *      <code>edu.tamu.csdl.filenames</code>
     * @return The filename corresponding to the specified key or 
     *      <code>'!' + key + '!'</code> if there is no resource corresponding
     *      to the specified key.
     */
    public static String getFilename(String key) {
        String filename = null;
        try { 
            if (RESOURCE_BUNDLE != null) 
                filename = RESOURCE_BUNDLE.getString(key); 
        } catch (MissingResourceException e) { filename = null; }
        
        if (filename == null) {
            try {
                key = key.replaceAll("\\.", "/");
                Context initCtx = new InitialContext();
                Context envCtx  = (Context)initCtx.lookup("java:comp/env");
                filename = (String)envCtx.lookup(key);
            } catch (NamingException ne) { filename = '!' + key + '!'; }
        }
        
        return filename;
    }
    
    /**
     * Returns a <code>File</code> as specified in the resource bundle 
     * <code>edu.tamu.csdl.filenames</code> for the provided key.
     *  
     * @param key The key identifying a filename in the resource bundle
     *      <code>edu.tamu.csdl.filenames</code>
     * @return A <code>File</code> object for the filename corresponding to the 
     *      specified key or null if there is no resource corresponding to the
     *      specified key.
     */
    public static File getFile(String key) {
        
        String filename = getFilename(key);
        if (!filename.startsWith("!")) {
            return new File(filename);
        } else { 
            return null;
        }
    }
    
    /**
     * Copies the source file to the target destination. The srouce file must 
     * exist and the target file must not exist for this operation to be 
     * performed.
     * 
     * @param src The file to be copied.
     * @param target The destination to which the file should be copied.
     * @return True if the operation succeeds, false if it does not. Notice 
     *      that even if this returns false, the file may have been partially 
     *      copied and the target file created. 
     */
    public static boolean copyFile(File src, File target) {
        if (!src.exists() || target.exists()) return false;
        
        boolean success = false;
        FileInputStream  is = null;
        FileOutputStream os = null;
        try {
            // copy file
            is = new FileInputStream(src);
            FileChannel sourceChannel = is.getChannel();
            
            os = new FileOutputStream(target);
            FileChannel destinationChannel = os.getChannel();
            
            // copy the image and delete source
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
            success = true;
        } catch (IOException ioe) {
            LogService.logError("Could not copy file", LOGGER, ioe);
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (Exception ex) {}
        }
        
        return success;
    }
    
    /**
     * Removes the directory tree descending from the specified file. 
     * 
     * @param file The root of the tree to be deleted.
     */
    public static void deleteDirectory(File file) {
        if (!file.exists()) return;
        
        if (file.isFile()) file.delete();
        else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDirectory(files[i]);
            }
            file.delete();
        }
    }
    
    /**
     * Returns the canonical path to 'file' (which is both absolute and free 
     * of symbolic links), converted to POSIX style ('/' directory separators) 
     * if necessary.
     * 
     * @param file The file whose path name should be converted.
     * @return The canonical path to the specified file, converted to POSIX 
     *      style.
     */
    public static String getCanonicalPOSIXPath(File file) throws IOException {
        String canonicalPath = file.getCanonicalPath();
        if (File.separatorChar != '/')
            return canonicalPath.replace(File.separatorChar, '/');
        else
            return canonicalPath;
    }

    /**
     * Returns the path of a File relative to some directory. Both arguments 
     * must be POSIX-style (i.e., with '/' as  the directory separator), and 
     * both must be absolute paths, or else relative to the same directory. 
     * Note that both rootPath and subPath should be made "canonical" before 
     * feeding them to this function, as the presence of symbolic links in 
     * the filenames may cause confusion.
     *
     * @param rootPath the path to the "root" directory, to which 'subPath' 
     *      should be made relative
     * @param subPath  the path to the sub-item whose relative path from 
     *      'rootPath' is desired
     * @return the path from 'rootPath' to 'subPath', or null if it cannot be 
     *      determined
     *
     * @see getCanonicalPOSIXPath
     */
    public static String getRelativePath(String rootPath, String subPath) {
        // This should already be formatted to use /'s instead of \'s
        if (rootPath.indexOf('\\') != -1 || subPath.indexOf('\\') != -1) {
            LogService.logError("Windows-style path passed to function expecting POSIX-style path", LOGGER);
            return null;
        }

        // Ensure that the root directory path has a trailing /
        if (!rootPath.endsWith("/"))
            rootPath += "/";

        // Make sure 'subPath' is really underneath 
        if (!subPath.startsWith(rootPath)) {
            LogService.logWarn("Path \"" + subPath + "\" is not underneath root path \"" + rootPath + "\"", LOGGER);
            return null;
        }

        // OK! Whatever's after the 'rootPath' prefix is our relative path
        return subPath.substring(rootPath.length());
    }

    /** 
     * Returns the basename for a given filename (i.e. the name of the file
     * minus the directory path to that file and the extension). Note that 
     * the filename should be made "canonical" before feeding them to this 
     * function in order to normalize the directory separators. 
     * 
     * @param filename The filename whose basename is desired.
     * @return The basename for the given filename.
     * 
     * @see getCanonicalPOSIXPath
     */
    public static String getBasename(String filename) {
        int dirSeparatorIndex = findFinalDirSeparator(filename);
        int extSeparatorIndex = 
            findExtSeparator(filename, dirSeparatorIndex);

        return filename.substring(dirSeparatorIndex + 1, extSeparatorIndex);
    }

    /**
     * Returns the extension for a given filename. Note that the filename  
     * should be made "canonical" before feeding them to this function in order 
     * to normalize the directory separators. 
     * 
     * @param filename The filename whose extension is desired
     * @return The extension for the given filename
     * 
     * @see getCanonicalPOSIXPath 
     */
    public static String getExtension(String filename) {
        int dirSeparatorIndex = findFinalDirSeparator(filename);
        int extSeparatorIndex = findExtSeparator(filename, dirSeparatorIndex);

        return filename.substring(extSeparatorIndex + 1);
    }

    /** 
     * Returns the name of the path excluding the individual file for a given
     * filename. Note that the filename should be made "canonical" before 
     * feeding it to this function in order to normalize the directory 
     * separators. 
     *  
     * @param filename The filename whose directory is desired
     * @return The directory of the given filename
     * 
     * @see getCanonicalPOSIXPath 
     */
    public static String getDirectory(String filename) {
        int dirSeparatorIndex = findFinalDirSeparator(filename);
        return filename.substring(0, dirSeparatorIndex);
    }

    public static String getURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
    private static int findFinalDirSeparator(String filename) {
        // This should already be formatted to use /'s instead of \'s
        if (filename.indexOf('\\') != -1) {
            LogService.logError(
                    "Windows-style path passed to function expecting " +
                    "POSIX-style path", LOGGER);
        }
        
        // Find the final directory separator, if any
        return filename.lastIndexOf('/');
    }

    private static int findExtSeparator(String filename, int dirSeparatorIndex) {
        // Find the last '.', if any. This only delimits the extension if it's 
        // (a) _after_ the basename start, and
        // (b) has no following whitespace.
        int extSeparatorIndex = filename.lastIndexOf('.');
        if (extSeparatorIndex < dirSeparatorIndex || 
            filename.indexOf(' ', extSeparatorIndex) != -1) {
            
            extSeparatorIndex = filename.length();
        }
        
        return extSeparatorIndex;
    }
}