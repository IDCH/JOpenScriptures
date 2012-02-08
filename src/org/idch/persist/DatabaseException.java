package org.idch.persist;

/**
 * Last modified $Date: 2008-07-17 19:07:37 $
 * @version $Revision: 1.1 $
 * @author Neal Audenaert
 */
@SuppressWarnings("serial")
public class DatabaseException extends Exception {
    
    public static final String CONTEXT_NOT_FOUND    = "errors.db.contextNotFound";
    public static final String CIRCULAR_REFERENCE   = "errors.db.circularReference";
    public static final String NOT_A_DATA_SOURCE    = "errors.db.notADataSource";
    public static final String DB_CONNECTION_FAILED = "errors.db.dbConnectionFailed";
    public static final String DB_DRIVER_FAILED     = "errors.db.dbDriverFailed";
    public static final String MISSING_RESOURCE     = "errors.db.missingResource";
    public static final String INVALID_STRATEGY     = "errors.db.invalidStrategy";
    public static final String DOES_NOT_EXIST       = "errors.db.doesNotExist";
    
    /** The exception that led to this database problem. */
    private Exception cause  = null;
    private String errorCode = null;
    private String resource  = null;
    
    
    /**
     * Constructs a new database exception that appears to
     * have happened for new good readon whatsoever.
     */
    public DatabaseException() { super(); }

    /**
     * Constructs a new database exception with the specified explanation.
     * @param msg the explanation for the error
     */
    public DatabaseException(String msg) { 
        super(msg);
        this.errorCode = msg;
    }

    /**
     * Constructs a new database exception that results from the
     * specified data store exception.
     *
     * @param cse the cause for this persistence exception
     */
    public DatabaseException(Exception cse) {
        super(cse.getMessage());
        cause = cse;
    }
    
    
    /**
     * 
     * @param msg the explanation for the error
     * @param cse the cause for this persistence exception
     */
    public DatabaseException(String msg, Exception cse) {
        super(cse.getMessage());
        this.errorCode = msg;
        cause = cse;
    }
    
    /**
    * 
    * @param msg the explanation for the error
    * @param cse the cause for this persistence exception
    */
   public DatabaseException(String msg, String resource, Exception cse) {
       super(cse.getMessage());
       this.errorCode = msg;
       
       this.resource = resource;
       cause = cse;
   }

    /** Returns the error code. */
    public String getErrorCode() { return this.errorCode; }
    
    /** Returns the missing resource if the error is MISSING_RESOUCRE. */
    public String getMissingResource() { return this.resource; }
    
    /** @return the cause of this exception */
    public Throwable getCause() { return cause; }
    
    public String getLocalizedMessage() { return ""; } // TODO IMPLEMENT ME

}
