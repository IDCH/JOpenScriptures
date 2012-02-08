/**
 * 
 */
package org.idch.texts.persist.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.Token;
import org.idch.texts.TokenRepository;
import org.idch.texts.Work;
import org.idch.texts.WorkRepository;
import org.idch.util.Cache;
import org.idch.util.StopWatch;


/**
 * @author Neal_2
 */
public class MySQLTokenRepository implements TokenRepository {
    private final static Logger LOGGER = Logger.getLogger(MySQLTokenRepository.class);
    
    MySQLTextModule repo = null;
    
    private final static int ID = 1;
    private final static int UUID = 2;
    private final static int WORK_ID = 3;
    private final static int POS = 4;
    private final static int TEXT = 5;
    private final static int TYPE = 6;
    
    private final static String FIELDS = 
            "token_id, uuid, work_id, token_pos, token_text, token_type ";
    
    private static final String CREATE_SQL = 
            "INSERT INTO texts_tokens_Tokens (uuid, work_id, token_pos, token_text, token_type) " +
                    "VALUES (?, ?, ?, ?, ?)";
    
    private Cache<Long, Token> cache = new Cache<Long, Token>("Tokens", 1000);
    
    MySQLTokenRepository(MySQLTextModule repo) {
        this.repo = repo;
    }

    /**
     * Returns the id of a work. If the supplied work does not have an id set, this throws 
     * a runtime exception. This handles the case when a client uses a Work that has not 
     * yet been synchronized with the database.
     * 
     * @param w The work whose ID is to be retrieved
     * @return The work's ID
     * @throws RuntimeException If the work does not have an assigned ID.
     */
    private long getWorkId(Work w) {
        Long wId = w.getId();
        if (wId == null) {
            // TODO throw RepositoryAccessException
            throw new RuntimeException("Invalid work: no ID specified.");
        }
        
        return wId;
    }
    
    /**
     * Restores a token from the provide result set. This assumes that the result set 
     * cursor is positioned at the row to be restored. This will not update the cursor.
     * 
     * @param results The result set to use to restore the token.
     * @return The restored token.
     * @throws SQLException if there are problems accessing the result set.
     */
    private Token restore(ResultSet results) throws SQLException {

        Token token = null;
        synchronized (cache) {
            long id = results.getLong(ID);
            token = cache.get(id);
            if (token == null) {
                // Retrieve the work instance (eager loading supported by caching in the WorkRepo).
                long workId = results.getLong(WORK_ID);
                WorkRepository works = repo.getWorkRepository();
                Work w = works.find(workId);

                // Retrieve the token parameters
                int pos = results.getInt(POS);
                String uuid = results.getString(UUID);
                String text = results.getString(TEXT);
                String type = results.getString(TYPE);

                // create the token and update the properties.
                token = new Token(w, pos, text);
                token.setId(id);
                token.setUUIDString(uuid);
                token.setType(Token.Type.valueOf(type));
                
                cache.cache(id, token);
            }
        }
        return token;
    }
    
    StopWatch timer = new StopWatch("Appending", 100);
    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#create(openscriptures.text.Token)
     */
    @Override
    public Token create(Token t) {
        

        Long wId = getWorkId(t.getWork());
        Connection conn = null;
        try {
            conn = repo.openConnection();

            PreparedStatement stmt = conn.prepareStatement(CREATE_SQL, 
                    PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, t.getUUID().toString());
            stmt.setLong(2, wId);
            stmt.setLong(3, t.getPosition());
            stmt.setString(4, t.getText());
            stmt.setString(5, t.getType().toString());
            
            int numRowsChanged = stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if (numRowsChanged == 1 && results.next()) {
                long id = results.getLong(1);
                t.setId(id);
            }
            
            conn.commit();
        } catch (Exception ex) {
            repo.rollbackConnection(conn);
            String msg = "Could not create token: " + t.getUUID() + ". " + ex.getMessage();
        
            LOGGER.warn(msg, ex);
            t = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return t;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#create(java.util.List)
     */
    @Override
    public List<Token> create(List<Token> tokens) {
        if ((tokens == null) || (tokens.size() == 0)) {
            return tokens;
        }

        Long wId = getWorkId(tokens.get(0).getWork());
        Connection conn = null;
        try {
            conn = repo.openConnection();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO texts_tokens (uuid, work_id, token_pos, token_text, token_type) VALUES");
            
            boolean first = true;
            int sz = tokens.size();
            for (int i = 0; i < sz; i++) {
                if (first) first = false;
                else 
                    sqlBuilder.append(", ");
                
                sqlBuilder.append("(?, ").append(wId).append(", ?, ?, ?)");
            }
                    
            PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString(), 
                    PreparedStatement.RETURN_GENERATED_KEYS);
            
            Token t = null;
            for (int i = 0; i < sz; i++) {
                t = tokens.get(i);
                
                int offset = i * 4;
                stmt.setString(offset + 1, t.getUUID().toString());
                stmt.setLong(offset + 2, t.getPosition());
                stmt.setString(offset + 3, t.getText());
                stmt.setString(offset + 4, t.getType().toString());
            }
            
            int numRowsChanged = stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if (numRowsChanged != sz) {
                // THEN WE HAVE A PROBLEM
                throw new Exception("Unexpected number of rows changed") ;
            }
            
            for (int i = 0; i < sz; i++) {
                if (!results.next()) {
                    throw new Exception("No ID found: " + i);
                    // THEN WE HAVE A PROBLEM
                }
                
                t = tokens.get(i);
                long id = results.getLong(1);
                t.setId(id);
            }
            
            timer.start();
            conn.commit();
            timer.pause();
        } catch (Exception ex) {
            repo.rollbackConnection(conn);
            String msg = "Could not create tokens: " + ex.getMessage();
        
            LOGGER.warn(msg, ex);
            tokens = null;      // should throw exception
        } finally {
            repo.closeConnection(conn);
        }
        
        return tokens;
    }

    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#getMaxPosition(openscriptures.text.Work)
     */
    @Override
    public int getNumberOfTokens(Work work) {
        int WORK_ID = 1, NEXT_POS = 1;
        String sql = 
            "SELECT COALESCE(MAX(token_pos), -1) + 1 " +
            "  FROM texts_tokens" +
            " WHERE work_id = ?";
        
        int pos = -1;
        Long wId = getWorkId(work);
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(WORK_ID, wId);
            
            ResultSet results = stmt.executeQuery();
            if (results.next())
                pos = results.getInt(NEXT_POS);
            
        } catch (Exception ex) {
            String msg = "Could not retrieve max position for work " +
            		"(id=" + wId + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            pos = -1;
        } finally {
            repo.closeConnection(conn);
        }
        
        return pos;
    }

    @Override
    public Token find(UUID uuid) {
        String sql = "SELECT " + FIELDS + "FROM texts_tokens WHERE uuid = ?"; 
        
        Token token = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid.toString());
            
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                token = restore(results);
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve token (" + uuid.toString() + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            token = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return token;
    }

    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#find(openscriptures.text.Work, int)
     */
    @Override
    public Token find(Work w, int pos) {
        Long wId = getWorkId(w);
        String sql = "SELECT " + FIELDS + 
                     "  FROM texts_tokens" +
                     " WHERE work_id = " + wId + " AND token_pos = " + pos;
        
        assert pos >= 0 : "Position must be non-negative";
        
        Token token = null;
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(sql);
            if (results.next()) {
                long id = results.getLong(ID);
                String uuidStr = results.getString(UUID);
                String text = results.getString(TEXT);
                String type = results.getString(TYPE);
                
                token = new Token(w, pos, text);
                token.setId(id);
                token.setUUIDString(uuidStr);
                token.setType(Token.Type.valueOf(type));
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve token " +
                    "(workId=" + wId + ", pos=" + pos + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            token = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return token;
    }
    
    /**
     * Returns all tokens for a specified work in the range <tt>[start, end)</tt>.
     * 
     * @param w The work for which the tokens should be retrieved.
     * @param start The starting position (inclusive) of the tokens to retrieve.
     * @param end The ending position (exclusive) of the tokens to retrieve.
     * 
     * @see org.idch.texts.TokenRepository#find(org.idch.texts.Work, int, int)
     */
    @Override
    public List<Token> find(Work w, int start, int end) {
        int WORK = 1, START = 2, END = 3;
        String sql = "SELECT " + FIELDS + "FROM texts_tokens " +
        		     " WHERE work_id = ? " +
        		     "   AND token_pos >= ? AND token_pos < ?" +
        		     " ORDER BY token_pos"; 
        
        assert start >= 0 : "Starting position must be non-negative";
        assert end > start : "The ending position must be greater than the starting position";
        
        List<Token> tokens = new ArrayList<Token>(end - start);
        Connection conn = null;
        try {
            conn = repo.openReadOnlyConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(WORK, getWorkId(w));
            stmt.setInt(START, start);
            stmt.setInt(END, end);
            
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                tokens.add(restore(results));
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve tokens (" + "): " + ex.getMessage();
            LOGGER.warn(msg, ex);
            tokens = null;
        } finally {
            repo.closeConnection(conn);
        }
        
        return tokens;
    }

    /**
     * Looks up the set of tokens associated with a particular structure. 
     * 
     * @param s The structure whose tokens should be retrieved.
     * @return The list of tokens associated with that structure.
     * @see org.idch.texts.TokenRepository#find(org.idch.texts.Structure)
     */
    @Override
    public List<Token> find(Structure s) {
        List<Token> results = new ArrayList<Token>();
        
        // first, we need to get this structure's work
        UUID uuid = s.getWorkUUID();
        WorkRepository works = repo.getWorkRepository();
        Work w = works.find(uuid);
        
        int start = s.getStart();
        int end = s.getEnd();
        if (start < 0) {
            return results;     // TODO bad data
        }
        
        if (end <= start) {
            results.add(find(w, start)); 
        } else {
            results = find(w, start, end);
        }
        
        return results;
    }
}
