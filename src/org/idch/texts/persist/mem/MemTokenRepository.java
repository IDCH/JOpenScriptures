/**
 * 
 */
package org.idch.texts.persist.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.idch.texts.Structure;
import org.idch.texts.Token;
import org.idch.texts.TokenRepository;
import org.idch.texts.Work;


/**
 * @author Neal Audenaert
 */
class MemTokenRepository implements TokenRepository {
    
    
    MemTextModule repo = null;
    
    Map<Long, List<Token>> tokens = new HashMap<Long, List<Token>>();
    Map<String, Token> tokensByUUID = new HashMap<String, Token>();
    
    MemTokenRepository(MemTextModule repo) {
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
    
    
    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#create(openscriptures.text.Token)
     */
    @Override
    public Token create(Token t) {
        Long wId = getWorkId(t.getWork());
        List<Token> tokenList = this.tokens.get(wId);
        if (tokenList == null) {
            tokenList = new ArrayList<Token>();
            this.tokens.put(wId, tokenList);
        }
        
        t.setPosition(tokenList.size());
        
        System.out.print(t.getText());
        this.tokensByUUID.put(t.getUUID().toString(), t);
        return tokenList.add(t) ? t : null;
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
        List<Token> tokenList = this.tokens.get(wId);
        if (tokenList == null) {
            tokenList = new ArrayList<Token>();
            this.tokens.put(wId, tokenList);
        }
        
        for (Token t : tokens) {
            t.setPosition(tokenList.size());
            
            this.tokensByUUID.put(t.getUUID().toString(), t);
            tokenList.add(t);
        }
        
        return tokens;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#getMaxPosition(openscriptures.text.Work)
     */
    @Override
    public int getNumberOfTokens(Work work) {
        Long wId = getWorkId(work);
        List<Token> tokenList = this.tokens.get(wId);
        if (tokenList == null) {
            tokenList = new ArrayList<Token>();
            this.tokens.put(wId, tokenList);
        }
        
        return tokenList.size();
    }

    @Override
    public Token find(UUID uuid) {
        return tokensByUUID.get(uuid.toString());
    }

    /* (non-Javadoc)
     * @see openscriptures.text.TokenRepository#find(openscriptures.text.Work, int)
     */
    @Override
    public Token find(Work w, int pos) {
        assert pos >= 0 : "Position must be non-negative";
        
        Long wId = getWorkId(w);
        List<Token> tokenList = this.tokens.get(wId);
        if (tokenList == null) {
            tokenList = new ArrayList<Token>();
            this.tokens.put(wId, tokenList);
        }
        
        if (pos >= 0 && pos < tokenList.size())
            return tokenList.get(pos);
        else return null;
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
        assert start >= 0 : "Starting position must be non-negative";
        assert end > start : "The ending position must be greater than the starting position";
        
        List<Token> tokens = new ArrayList<Token>(end - start);
        Long wId = getWorkId(w);
        List<Token> tokenList = this.tokens.get(wId);
        if (tokenList == null) {
            tokenList = new ArrayList<Token>();
            this.tokens.put(wId, tokenList);
        }
        
        for (int i = start; i < end && i < tokenList.size(); i++) {
            tokens.add(tokenList.get(i));
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
        Work w = repo.getWorkRepository().find(uuid);
        
        int start = s.getStart();
        int end = s.getEnd();
        if (start < 0) {
            return results;     
        }
        
        if (end <= start) {
            results.add(find(w, start)); 
        } else {
            results = find(w, start, end);
        }
        
        return results;
    }
}
