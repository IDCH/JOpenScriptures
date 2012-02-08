/**
 * 
 */
package org.idch.texts.persist.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.StructureComparator;
import org.idch.texts.StructureRepository;
import org.idch.texts.Token;
import org.idch.texts.Work;


/**
 * @author Neal Audenaert
 */
public class MySQLStructureRepository implements StructureRepository {
    
    //===================================================================================
    // SYMBOLIC CONSTANTS
    //===================================================================================
    private final static Logger LOGGER = Logger.getLogger(MySQLStructureRepository.class);
    
    private static final int S_UUID       = 1;
    private static final int WORK_UUID    = 2;
    private static final int NAME         = 3;
    private static final int PERSPECTIVE  = 4;
    private static final int START        = 5;
    private static final int END          = 6;
    
    private static final int STRUCTURE_ID = 7;
    
    private static final String FIELDS =
            "uuid, work_uuid, structure_name, perspective, " + 
            "start_pos, end_pos ";
    
    //===================================================================================
    // STATIC METHODS
    //===================================================================================
    
    /**
     * 
     * @param stmt
     * @param param
     * @param value
     * @throws SQLException
     */
    public static void setParameter(PreparedStatement stmt, int param, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(param, value);
        } else { 
            stmt.setNull(param, Types.INTEGER);
        }
    }
    
    public static Integer getParameter(ResultSet results, int param, Class<Integer> clazz) 
            throws SQLException {
        Integer result = results.getInt(param);
        if (results.wasNull()) {
            result = null;
        }
        
        return result;
    }
    
    //===================================================================================
    // MEMBER VARIABLES
    //===================================================================================
    
    private MySQLTextModule repo = null;
    private AttrRepo attrs = null;
    
    //===================================================================================
    // CONSTRUCTORS
    //===================================================================================
    
    /**
     * 
     * @param repo
     */
    MySQLStructureRepository(MySQLTextModule repo) {
        this.repo = repo;
        this.attrs = new AttrRepo();
    }

    //===================================================================================
    // HELPER METHODS
    //===================================================================================

    private void saveAttributes(Connection conn, Structure s) throws SQLException {
        assert s.getId() != null;
        if (s.getId() == null) {
            // FIXME need to use RepositoryAccessException.
            throw new SQLException("Structure not yet save -- FIXME this should be a different exception class.");
        }
        
        Map<String, String> attributes = s.getAttributes();
        if (attributes != null && attributes.size() > 0) {
            this.attrs.createOrUpdate(conn, s.getId(), s.getAttributes());
        }
    }

    private boolean update(Structure s) {
        int NAME = 1, PERSPECTIVE = 2, START = 3, END = 4, ID = 5;
        String sql = 
                "UPDATE texts_structures SET " +
                "    structure_name = ?, " +
                "    perspective = ?, " + 
                "    start_pos = ?, " + 
                "    end_pos = ? " +
                "WHERE structure_id = ?";
        
        boolean success = false;
        Connection conn = null;
        try {
            conn = repo.openConnection();
            
            // save the attributes
            saveAttributes(conn, s);
            
            // build the statement
            PreparedStatement stmt = conn.prepareStatement(sql); 
            
            stmt.setString(NAME, s.getName());
            stmt.setString(PERSPECTIVE, s.getPerspective());
            setParameter(stmt, START, s.getStartTokenPosition());
            setParameter(stmt, END, s.getEndTokenPosition());
            stmt.setLong(ID, s.getId());

            // execute the query
            int numRowsChanged = stmt.executeUpdate();
            success = (numRowsChanged != 1);
            if (numRowsChanged > 1) {
                LOGGER.warn("Bizarre number of rows changed (" + numRowsChanged + ") " + 
                            "while saving a structure (" + s.getUUID() + "). Expected 1.");
                repo.rollbackConnection(conn);
            } else {
                conn.commit();
            }
        } catch (Exception ex) {
            repo.rollbackConnection(conn);
            
            String msg = "Could not save structure: " + s.getName() + ". " + ex.getMessage();
            LOGGER.warn(msg, ex);
            success = false;
        } finally {
            repo.closeConnection(conn);
        }
        
        return success;
    }
    
    //===================================================================================
    // CREATION AND UPDATE METHOS
    //===================================================================================

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#create(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public Structure create(Work work, String name) {
        return create(new Structure(work.getUUID(), name));
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#create(openscriptures.text.Work, java.lang.String, openscriptures.text.Token, openscriptures.text.Token)
     */
    @Override
    public Structure create(Work work, String name, Token start, Token end) {
        return create(new Structure(work.getUUID(), name, start, end));
    }

        
    public Structure create(Structure s) {
        // TODO change to return boolean
        assert (s.getId() == null) : "This structure has already been created.";
        if (s.getId() != null)
            return null;
        
        String sql = "INSERT INTO texts_structures (" + FIELDS + ") " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        boolean success = false;
        Connection conn = null;
        try {
            // build the statement
            conn = repo.openConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, 
                    PreparedStatement.RETURN_GENERATED_KEYS);
            
            stmt.setString(S_UUID, s.getUUID().toString());
            stmt.setString(WORK_UUID, s.getWorkUUID().toString());
            stmt.setString(NAME, s.getName());
            stmt.setString(PERSPECTIVE, s.getPerspective());
            setParameter(stmt, START, s.getStartTokenPosition());
            setParameter(stmt, END, s.getEndTokenPosition());
            
            // execute the query
            int numRowsChanged = stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if (numRowsChanged == 1 && results.next()) {
                long id = results.getLong(1);
                s.setId(id);
                saveAttributes(conn, s);
                success = true;
            }
            
            conn.commit();
        } catch (Exception ex) {
            repo.rollbackConnection(conn);
            
            String msg = "Could not create structure: " + s.getName() + ". " + ex.getMessage();
            LOGGER.warn(msg, ex);
            success = false;
            s = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return success ? s : null;
    }
    
    
    
    /**
     * 
     * @see org.idch.texts.StructureRepository#save(org.idch.texts.Structure)
     */
    @Override
    public boolean save(Structure s) {
        // NOTE this should be the default method for saving/creating structures.
        assert s != null : "Cannot save a null structure.";
        if (s == null) {
            throw new NullPointerException("Cannot save a null structure");
        }
        
        // This is a new structure. We should create it instead.
        if (s.getId() == null) {
            return create(s) != null;
        } else {
            return update(s);
        }
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#hasStructuresFor(openscriptures.text.Work)
     */
    @Override
    public boolean hasStructuresFor(UUID workId) {
        String sql = "SELECT structure_id FROM texts_structures WHERE work_uuid = ? LIMIT 1";

        boolean hasStructures = false;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, workId.toString());
            ResultSet results = stmt.executeQuery();
            hasStructures = results.next(); 
        } catch (Exception ex) {
            String msg = "Failed to determine if structures exist for the specified work " +
            		"(" + workId.toString() + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            hasStructures = false;
        } finally {
            repo.closeConnection(conn);
        }
        
        return hasStructures;
    }
    

    //===================================================================================
    // RETRIEVAL METHOS
    //===================================================================================

    /**
     * 
     * @param s
     * @param results
     * @return
     * @throws SQLException
     */
    private Structure restore(Structure s, ResultSet results) throws SQLException {
        s.setUUIDString(results.getString(S_UUID));
        s.setName(results.getString(NAME));
        s.setPerspective(results.getString(PERSPECTIVE));
        s.setStartTokenPosition(getParameter(results, START, Integer.class));
        s.setEndTokenPosition(getParameter(results, END, Integer.class));
        
        String wId = results.getString(WORK_UUID);
        assert wId != null : "No identifier for this token's work.";         // should be enforced by DB constraints
        s.setWork(UUID.fromString(wId));

        // retrieve the attributes
        Connection conn = results.getStatement().getConnection();
        s.setAttributes(attrs.get(conn, s.getId()));
        
        return s;
    }
    
    private void restore(Map<UUID, SortedSet<Structure>> structures, ResultSet results) 
            throws SQLException {
        Structure s = new Structure(results.getLong(STRUCTURE_ID));
        s = restore(s, results);
        
        UUID uuid = s.getWorkUUID();
        SortedSet<Structure> structs = structures.get(uuid);
        if (structs == null) {
            structs = new TreeSet<Structure>(new StructureComparator());
            structures.put(uuid, structs);
        }
        
        structs.add(s);
    }
    
    private SortedSet<Structure> find(PreparedStatement stmt) throws SQLException {
        SortedSet<Structure> structures = new TreeSet<Structure>(new StructureComparator()); 
        
        Structure s; 
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            s = new Structure(results.getLong(STRUCTURE_ID));
            structures.add(restore(s, results));
        }
        
        return structures;
    }
    
    
    
    
    /**
     * 
     * @param s
     * @return
     */
    public Structure synchronize(Structure s) {
        String sql = null;
        String ident = null;
        boolean useUUID = false;
        if (s.getId() != null) {
            sql = "SELECT " + FIELDS + " FROM texts_structures WHERE structure_id = ?";
            ident = s.getId().toString();
        } else if (s.getUUID() != null) {
            ident = s.getUUID().toString();
            sql = "SELECT " + FIELDS + ", structure_id FROM texts_structures WHERE uuid = ?";
            useUUID = true;
        } else {
            String msg = "Could not retrieve work. No identifier supplied.";
            LOGGER.warn(msg);
            return null;
        }
        
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (useUUID) {
                stmt.setString(1, s.getUUID().toString());
            } else {
                stmt.setLong(1, s.getId());
            }
            
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                if (useUUID) 
                    s.setId(results.getLong(STRUCTURE_ID));
                
                s = restore(s, results);
            } else {
                s = null;
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve structure (" + ident + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            s = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return s;
    }
    
    /**
     * 
     * @param id
     * @return
     */
    public Structure find(long id) {
        return synchronize(new Structure(id));
    }
    
    public Structure find(UUID id) {
        return synchronize(new Structure(id));
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name) {
        // TODO LOTS of duplicated code. Refactor into delgate class.
        int WORK_ID = 1, NAME = 2;
        String sql = 
                "SELECT " + FIELDS + ", structure_id " +
                "  FROM texts_structures" + 
                " WHERE work_uuid = ? AND structure_name = ?" +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        SortedSet<Structure> structures = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(WORK_ID, w.getUUID().toString());
            stmt.setString(NAME,  name);
        
            structures = find(stmt);
        } catch (Exception ex) {
            String msg = "Could not retrieve structures (" + name + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, int position) {
        int WORK_ID = 1, START_POS = 2, END_POS = 3;
        String sql =
                "SELECT " + FIELDS + ", structure_id " +
                "  FROM texts_structures" + 
                " WHERE work_uuid = ? AND " +
                "       start_pos <= ? AND end_pos > ?" +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        SortedSet<Structure> structures = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(WORK_ID, w.getUUID().toString());
            stmt.setInt(START_POS,  position);
            stmt.setInt(END_POS,  position);
        
            structures = find(stmt);
        } catch (Exception ex) {
            String msg = "Could not retrieve structures: " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String, int, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, int start, int end) {
        int WORK_ID = 1, NAME = 2, START_POS = 3, END_POS = 4;
        String sql =
                "SELECT " + FIELDS + ", structure_id " +
                "  FROM texts_structures" + 
                " WHERE work_uuid = ? AND " +
                "       structure_name = ? AND start_pos >= ? AND end_pos < ?" +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        SortedSet<Structure> structures = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(WORK_ID, w.getUUID().toString());
            stmt.setString(NAME,  name);
            stmt.setInt(START_POS,  start);
            stmt.setInt(END_POS,  end);
        
            structures = find(stmt);
        } catch (Exception ex) {
            String msg = "Could not retrieve structures: " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String, int, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, int start, int end, boolean strict) {
        int WORK_ID = 1, NAME = 2, 
            AFTER = 3, BEFORE = 4,
            END_AFTER = 5, END_BEFORE = 6;
        String sql =
                "SELECT " + FIELDS + ", structure_id " +
                "  FROM texts_structures" + 
                " WHERE work_uuid = ? AND " +
                "       structure_name = ? AND " +
                "       (start_pos <= ? AND end_pos > ?) " +
    ((strict) ? "    OR (start_pos < ? AND end_pos > ?)" : "") +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        if (!strict)
            return this.find(w, name, start, end);
        
        SortedSet<Structure> structures = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(WORK_ID, w.getUUID().toString());
            stmt.setString(NAME,  name);
            
            if (strict) {
                stmt.setInt(AFTER, start);
                stmt.setInt(BEFORE, start);
                stmt.setInt(END_AFTER,  end);
                stmt.setInt(END_BEFORE,  end);
            } else {
                stmt.setInt(AFTER, start);
                stmt.setInt(BEFORE, END);
            }
        
            structures = find(stmt);
        } catch (Exception ex) {
            String msg = "Could not retrieve structures: " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public Map<UUID, SortedSet<Structure>> find(String name, String attribute, String value) {
        // TODO LOTS of duplicated code. Refactor into delgate class.
        int NAME = 1, ATTR = 2, VALUE = 3;
        String sql = 
                "SELECT " + FIELDS + ", S.structure_id AS structure_id" +
                "  FROM texts_structures AS S, texts_structureattributes AS A" + 
                " WHERE S.structure_id = A.structure_id AND" +
                "       S.structure_name = ? AND" +
                "       A.attr_key = ? AND" +
                "       A.attr_value = ?" +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        Map<UUID, SortedSet<Structure>> structures = 
                new HashMap<UUID, SortedSet<Structure>>();
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(NAME,  name);
            stmt.setString(ATTR,  attribute);
            stmt.setString(VALUE,  value);
            
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                restore(structures, results);
            }
            
        } catch (Exception ex) {
            String msg = "Could not retrieve structures (" + name + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, String attribute, String value) {
        // TODO LOTS of duplicated code. Refactor into delgate class.
        int WORK_ID = 1, NAME = 2, ATTR = 3, VALUE = 4;
        String sql = 
                "SELECT " + FIELDS + ", S.structure_id AS structure_id" +
                "  FROM texts_structures AS S, texts_structureattributes AS A" + 
                " WHERE S.structure_id = A.structure_id AND" +
                "       S.work_uuid = ? AND " +
                "       S.structure_name = ? AND" +
                "       A.attr_key = ? AND" +
                "       A.attr_value = ?" +
                " ORDER BY start_pos ASC, end_pos DESC";
        
        SortedSet<Structure> structures = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(WORK_ID, w.getUUID().toString());
            stmt.setString(NAME,  name);
            stmt.setString(ATTR,  attribute);
            stmt.setString(VALUE,  value);
            structures = find(stmt);
        } catch (Exception ex) {
            String msg = "Could not retrieve structures (" + name + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            structures.clear();
        } finally {
            repo.closeConnection(conn);
        }
        
        return structures;
    }
    

    
    
    //===================================================================================
    // INNER CLASS FOR PERSISTING ATTRIBUTES
    //===================================================================================
    
    private static class AttrRepo {
        private static final String DROP_SQL = 
                "DELETE FROM texts_structureattributes WHERE structure_id = ?";
        private static final String INSERT_SQL = 
                "INSERT INTO texts_structureattributes (structure_id, attr_key, attr_value) " +
                        "VALUES (?, ?, ?)";
        private static final String SELECT_SQL = 
                "SELECT attr_key, attr_value " +
                "  FROM texts_structureattributes " +
                " WHERE structure_id = ?";        
        
        public AttrRepo() {  }
        
        /**
         * 
         * @param conn
         * @param structureId
         * @param attrs
         * @throws SQLException
         */
        public void createOrUpdate(
                Connection conn, long structureId, Map<String, String> attrs) 
        throws SQLException {
            int ID = 1, KEY = 2, VALUE = 3;
            
            PreparedStatement stmt = null;
            stmt = conn.prepareStatement(DROP_SQL);
            stmt.setLong(ID, structureId);
            stmt.executeUpdate();
            
            stmt = conn.prepareStatement(INSERT_SQL); 
            stmt.setLong(ID, structureId);
            for (String k : attrs.keySet()) {
                stmt.setString(KEY, k);
                stmt.setString(VALUE, attrs.get(k));
                
                int numberOfRows = stmt.executeUpdate();
                assert numberOfRows == 1 : "Unexpected number of rows inserted.";
                
            }
        }
        
        /**
         * 
         * @param conn
         * @param structureId
         * @return
         * @throws SQLException
         */
        public Map<String, String> get(Connection conn, long structureId) throws SQLException {
            int ID = 1, KEY = 1, VALUE = 2;
            
            Map<String, String> attrs = new HashMap<String, String>();
            PreparedStatement stmt = conn.prepareStatement(SELECT_SQL); 
                
            stmt.setLong(ID, structureId);
            ResultSet results = stmt.executeQuery();
            while(results.next()) {
                attrs.put(results.getString(KEY), results.getString(VALUE));
            }
            
            return attrs;
        }
        
    }   // END AttrRepo class
}
