/* Created on       Aug 26, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *           ALL RIGHTS RESERVED. 
 */
package org.idch.persist;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;

public abstract class DBBackedRepository {
    protected static final String LOGGER = DBBackedRepository.class.getName();


    public final static String CREATE_SQL_FILE = "create.sql";
    public final static String CLEAN_SQL_FILE  = "clean.sql";
    public final static String DROP_SQL_FILE   = "drop.sql";
    public final static String INIT_SQL_FILE   = "init.sql";
    
    public final static String DB_SRCRIPTS_PROP = "repo.sqldir";
    public final static String DB_URL_PROP      = "repo.db.url";
    public final static String DB_DRIVER_PROP   = "repo.db.driver";
    public final static String DB_USER_PROP     = "repo.db.user";
    public final static String DB_PASS_PROP     = "repo.db.pass";
    
    public final static String DEFAULT_PROP_BUNDLE = "repositories";
    
    private final static Map<String, DBBackedRepository> s_repositories = 
        new HashMap<String, DBBackedRepository>();
    
    private static ResourceBundle s_bundle = null;
    
    public static void setPropertyBundle(String name) 
        throws RepositoryAccessException {
        
        try {
            s_bundle = ResourceBundle.getBundle(name);
        } catch (MissingResourceException ex) {
            String msg = null;
            RepositoryAccessException rae = 
                new RepositoryAccessException(msg);
            LogService.logError(msg, LOGGER, rae);
            
            throw rae;
        }
    }
    
    private static synchronized ResourceBundle getBundle()
        throws RepositoryAccessException {
        
        if (s_bundle == null) 
            setPropertyBundle(DEFAULT_PROP_BUNDLE);
        
        return s_bundle;
    }
    
    /** 
     * 
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public static final DBBackedRepository get(String module)
            throws RepositoryAccessException {
        
        DBBackedRepository repo = null;
        synchronized(s_repositories) {
            DBBackedRepository repository = s_repositories.get(module);
            if (repository != null) 
                return repository;
            
            ResourceBundle bundle = getBundle();
            try {
                String classname = bundle.getString(module + ".classname");
                
                repo = (DBBackedRepository)Class.forName(classname).newInstance();
                repo.initialize(module, bundle);
                
                s_repositories.put(module, repo);
                
            } catch (Exception ex) {
                String msg = "Could not load Repository.";
                throw new RepositoryAccessException(msg, ex);
            }
        }
        
        return repo;
    }
    
    private ConnectionProvider m_provider;
    
    private  File m_sqlDirectory; 
   
    private String m_module;
    
    protected ResourceBundle m_bundle;
    
    /**
     * 
     * @param bundle
     * @throws RepositoryAccessException
     */
    protected void initialize(String module, ResourceBundle bundle) 
            throws RepositoryAccessException {
        m_module = module;
        m_bundle = bundle;
        
        String url    = bundle.getString(DB_URL_PROP);
        String driver = bundle.getString(DB_DRIVER_PROP);
        String user   = bundle.getString(DB_USER_PROP);
        String pass   = bundle.getString(DB_PASS_PROP);
        
        try {
            m_provider = new ConnectionProvider(url, driver, user, pass);
        } catch (DatabaseException dbe) {
            throw new RepositoryAccessException("Could not connect to database", dbe);
        }
        File dir = new File(bundle.getString(DB_SRCRIPTS_PROP));
        if (dir.canRead() && dir.isDirectory()) {
            m_sqlDirectory = dir;
        } else { 
            m_sqlDirectory = null;
            LogService.logInfo("Could not load database configuration " +
                    "scripts.", LOGGER);
        } 
    }
    
    //========================================================================
    // HELPER METHODS
    //========================================================================
    protected final Connection openTransaction() 
    throws SQLException, DatabaseException {
        Connection conn = null; 
        synchronized (m_provider) {
            conn = m_provider.getConnection();
            conn.setAutoCommit(false);
        }
        
        return conn;
    }
    
    protected final Connection openReadOnly() 
    throws SQLException, DatabaseException {
        Connection conn = null; 
        synchronized (m_provider) {
            conn = m_provider.getConnection();
            conn.setReadOnly(true);
        }
        
        return conn;
    }
    
    /**
     * Helper method that attempts to rollback a transaction, logging and 
     * supressing any exceptions.
     *  
     * @param conn the connection to rollback
     */
    protected final void rollback(Connection conn) {
        String msg = "Could not rollback transaction.";
        try {
            if (conn != null)
                conn.rollback();
        } catch (SQLException sqe) {
            LogService.logError(msg, LOGGER, sqe );
            
            throw new RuntimeException(sqe);
        }
    }
    
    /**
     * Helper method that attempts to close a transaction, logging and 
     * supressing any exceptions.
     *  
     * @param conn the connection to close
     */
    protected final void close(Connection conn) {
        String msg = "Could not close connection.";
        try { 
            if (conn != null)
                conn.close();
        } catch (SQLException sqe) {
            LogService.logError(msg, LOGGER, sqe);
            
            throw new RuntimeException(sqe);
        }
    }
    
    //========================================================================
    // DATABASE MANIPULATION METHODS
    //========================================================================
    
    /**
     * Attempts to determine whether or not the proper tables are defined for 
     * use by the <code>PropertyRepository</code>. 
     * 
     * @return <code>true</code> if the required tables are defined, 
     *      <code>false</code> if they are not.
     */
    public abstract boolean probe();
    
    protected boolean probe(List<String> statements) {
        boolean success = false;
        
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = m_provider.getConnection();
            stmt = conn.createStatement();
            for (String sql : statements) 
                stmt.executeQuery(sql);
            success = true;
            
        } catch (Exception ex) {
            // not unexpected - we may test under scenarios when the database
            // doesn't exist. 
            
            success = false;
        } finally {
            if (conn != null) {
                try { conn.close(); }
                catch (Exception ex) { 
                    /* complain loudly. leaked resources are bad. */
                    assert false : "Failed to close database connection";
                    throw new RuntimeException("Failed to close database connection");
                }
            }
        }
        
        return success;
    }
    
    /**
     * Helper method that executes the provided script file and probes to see
     * if the appropriate database tables are defined for the repository. 
     * 
     * @param sqlFile The SQL script file to be executed.  
     * @param expectSuccess Indicates whether this method should expect that 
     *      probing for the database will be successful. For instance, after 
     *      deleting the database, the probe should fail. 
     *      
     * @return <code>true</code> if the script was executed and the probe 
     *      returned the expected results, <code>false</code> otherwise. 
     * @throws RepositoryAccessException On failure, typically caused by to 
     *      database and/or file access errors
     */
    private boolean executeScriptAndProbe(File sqlFile, boolean expectSuccess) 
            throws RepositoryAccessException {
        
        boolean success = false;
        if (!sqlFile.canRead() || !sqlFile.isFile()) {
            throw new RepositoryAccessException("Could not locate script file. " +
                    "The file I tried (" + sqlFile.getAbsolutePath() + ") " +
                    "either does not exist or cannot be read.");
        }
        
        try {
            m_provider.executeScript(sqlFile);
            success = (expectSuccess) ? probe() : !probe();
        } catch(Exception ex) {
            throw new RepositoryAccessException(ex);
        }
        
        return success;
    }

    /**
     * Create the database tables required for the MySQL PropertyRepository,
     * silently deleting any existing tables or data. Use with caution. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was created succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean create() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, m_module + "/" + CREATE_SQL_FILE);
        return executeScriptAndProbe(sqlFile, true);
    }
    
    /**
     * Deletes all data (but not the database tables) from the database. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was cleaned succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean clean() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, m_module + "/" + CLEAN_SQL_FILE);
        return executeScriptAndProbe(sqlFile, true);
    }
    
    /**
     * Drops all database tables and data associated with this 
     * PropertyRepsoitory. <code>probe</code> should return false after 
     * successfull completion of this method. 
     * 
     * @return <code>true</code> if the database was deleted succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException On database access errors.
     */
    public boolean drop() throws RepositoryAccessException {
        File sqlFile = new File(m_sqlDirectory, m_module + "/" + DROP_SQL_FILE);
        return executeScriptAndProbe(sqlFile, false);
    }
}
