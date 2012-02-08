/**
 * 
 */
package org.idch.texts.persist.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.idch.persist.DatabaseException;
import org.idch.texts.StructureRepository;
import org.idch.texts.TokenRepository;
import org.idch.texts.WorkRepository;
import org.idch.texts.persist.AbstractTextModule;

/**
 * @author Neal Audenaert
 */
public class MySQLTextModule extends AbstractTextModule {

    private WorkRepository m_worksRepo = new MySQLWorkRepository(this);
    private TokenRepository m_tokensRepo = new MySQLTokenRepository(this);;
    private StructureRepository m_structuresRepo = new MySQLStructureRepository(this);;
    
    //========================================================================
    // REPOSITORY GETTERS
    //========================================================================
    public WorkRepository getWorkRepository() {
        return this.m_worksRepo;
    }
    
    public TokenRepository getTokenRepository() {
        return this.m_tokensRepo;
    }
    
    public StructureRepository getStructureRepository() {
        return this.m_structuresRepo;
    }
    
    //========================================================================
    // DATABASE MANIPULATION METHODS
    //========================================================================
    public Connection openConnection() throws SQLException, DatabaseException {
        return super.openTransaction();
    }
    
    public Connection openReadOnlyConnection() throws SQLException, DatabaseException {
        return super.openReadOnly();
    }

    public void rollbackConnection(Connection conn) {
        super.rollback(conn);
    }
    
    public void closeConnection(Connection conn) {
        super.close(conn);
    }

    /**
     * Attempts to determine whether or not the proper tables are defined for 
     * use by the <code>PropertyRepository</code>. 
     * 
     * @return <code>true</code> if the required tables are defined, 
     *      <code>false</code> if they are not.
     */
    public boolean probe() {
        List<String> sql = new ArrayList<String>(3);
        sql.add("SELECT work_id FROM texts_sorks;");
        sql.add("SELECT token_id FROM texts_tokens");
        sql.add("SELECT structure_id FROM texts_structures");
        return probe(sql);
    }
}