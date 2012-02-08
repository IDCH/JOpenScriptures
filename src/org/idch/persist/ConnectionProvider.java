/* Created on       Aug 7, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. PERMISSION TO USE THIS SOFTWARE MAY BE GRANTED 
 * TO INDIVIDUALS OR ORGANIZATIONS ON A CASE BY CASE BASIS. FOR MORE 
 * INFORMATION PLEASE CONTACT THE DIRECTOR OF THE CSDL. IN THE EVENT 
 * THAT SUCH PERMISSION IS GIVEN IT SHOULD BE UNDERSTOOD THAT THIS 
 * SOFTWARE IS PROVIDED ON AN AS IS BASIS. THIS CODE HAS BEEN DEVELOPED 
 * FOR USE WITHIN A PARTICULAR RESEARCH PROJECT AND NO CLAIM IS MADE AS 
 * TO IS CORRECTNESS, PERFORMANCE, OR SUITABILITY FOR ANY USE.
 */
package org.idch.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.idch.util.Filenames;
import org.idch.util.LogService;

public class ConnectionProvider {
    private static final String LOGGER = ConnectionProvider.class.getName();
    
    DataSource m_dataSource  = null;
    
    String name = "Simple Connection Provider";

    public ConnectionProvider() {
        
    }
    
    public ConnectionProvider(
            String url, String driver, String user, String pass) 
            throws DatabaseException {
        
        initialize(url, driver, user, pass);
    }
    
    public final void initialize(String jndiKey) throws DatabaseException {
        try {
            Context initCtx = new InitialContext();
            Context envCtx  = (Context) initCtx.lookup("java:comp/env");
            m_dataSource = (DataSource)envCtx.lookup(jndiKey);
            
        } catch (NamingException nex) {
            String msg = this.name + ": Could not access JNDI resource: " +
                jndiKey;
            LogService.logError(msg, LOGGER, nex);
            throw new DatabaseException(msg, nex);
        } 
    }
    
    @SuppressWarnings("unused")
    public final void initialize(
            String url, String driver, String user, String pass) 
        throws DatabaseException {
        LogService.logInfo("Initializing ConnectionProvider for database '" + 
                url + "' using driver '" + driver + "'. User: " + user, LOGGER);
        
        try { 
            Class.forName(driver).newInstance(); 
        } catch (Exception ex) {
            String msg = "Could not load driver: " + driver;
            LogService.logError(msg, LOGGER, ex);
            throw new DatabaseException(msg, ex);
        }
        
        ObjectPool pool = new GenericObjectPool(null);
        ConnectionFactory factory = new DriverManagerConnectionFactory(
                url, user, pass);
        PoolableConnectionFactory pFactory = new PoolableConnectionFactory(
                factory, pool, null, null, false, true);
        if (pFactory == null) {
            throw new DatabaseException(
                    "Failed to initialize poolable connection factory.");
        }
        
        m_dataSource = new PoolingDataSource(pool);
    }
    
    /** 
     * Returns a connection to the database configured for this manager. 
     */
    public final Connection getConnection() throws DatabaseException {
        Connection connection = null;
        String errmsg = "Connection provider has not been initialized";
        assert (m_dataSource != null) : errmsg;
        if (m_dataSource == null) 
            throw new DatabaseException(errmsg);
        
        if (m_dataSource != null) {
            try { 
                connection = m_dataSource.getConnection(); 
            } catch (SQLException sqle) {
                String msg = "Could not access database"; 
              
                LogService.logError(msg, LOGGER, sqle);
                throw new DatabaseException(msg, sqle);
            }
            
        } else {
            String msg = this.name + ": Attempted to get database connection" +
                " but no database connection properties are specified.";
            
            LogService.logWarn(msg, LOGGER);
            connection = null;
        }
        
        return connection;
    }
    
    public final int executeScript(File file) 
            throws SQLException, IOException  {
        String delimeter = ";";
        Statement stmt = null;
        Connection conn = null;
        
        // initialize the connection
        try {
            conn = this.getConnection();
            conn.setAutoCommit(false);
        } catch (DatabaseException ex) {
            String msg = "Could not execute script. Failed to establish " +
            		"database connection.";
            LogService.logError(msg, LOGGER, ex);
        } catch (SQLException ex) {
            String msg = "Could not set autocommit to false. Statements may " +
            		"be committed as they are encountered.";
            LogService.logWarn(msg, LOGGER);
        }
        
        if (conn == null) {
            return -1;
        }
        
        // process the file
        int ct = 0;
        String currentLine = "";
        String path = "";
        String sql = "";
        BufferedReader reader = null;
        try {
            stmt = conn.createStatement();
            path = Filenames.getCanonicalPOSIXPath(file);
            reader = new BufferedReader(new FileReader(file));
            while ((currentLine = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (StringUtils.isBlank(currentLine)) 
                    continue;
                
                int ix = currentLine.indexOf("//");
                if (ix >= 0)
                    currentLine = currentLine.substring(0, ix);
                
                currentLine = StringUtils.strip(currentLine);
                if (currentLine.startsWith("--") || 
                    currentLine.startsWith("#"))
                    continue;

                // update query string
                sql += " " + currentLine;
                
                // If one command complete
                ix = sql.indexOf(delimeter);
                if (ix >= 0) {
                    sql = sql.substring(0, ix);
                    LogService.logInfo("Executing Query: " + sql, LOGGER);
                    
                    stmt.execute(sql);
                    ct++;
                    sql = "";
                }
            }
            
            // commit all statements
            conn.commit();
            String msg = "Finished executing script: '" + path + "'. " +
                ct + " statements executed.";
            LogService.logInfo(msg, LOGGER);
            
        } catch (IOException ex) {
            String msg = "Failed to execute script: '" + path + "'. " +
                "There was an error reading the file. ";
            try {
                conn.rollback();
                msg += ct + " statements were rolled back";
            } catch (SQLException ex2) {
                msg += "failed to rollback " + ct + " statements";
            }
            
            LogService.logError(msg, LOGGER, ex);
            throw ex;
        } catch (SQLException ex) {
            String msg = "Failed to execute script: '" + path + "'. " +
                "An SQL error occured. ";
            try { 
                conn.rollback();
                msg += ct + " statements were rolled back";
            } catch (SQLException ex2) {
                msg += "failed to rollback " + ct + " statements";
            }
            
            LogService.logError(msg, LOGGER, ex);
            throw ex;
        } finally {
            try {
                if (reader != null) reader.close();
                if (conn != null)   conn.close(); } 
            catch (Exception ex) { /* nothing to do */ }
        }
        
        return ct;
    }
}
