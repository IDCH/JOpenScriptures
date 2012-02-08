/**
 * 
 */
package org.idch.texts;

import java.util.List;
import java.util.UUID;


/**
 * @author Neal Audenaert
 */
public interface TokenRepository {
    
    public Token create(Token t);
    
    public List<Token> create(List<Token> tokens);
    
    public int getNumberOfTokens(Work work);
    
    public Token find(UUID id);
    
    public Token find(Work w, int pos);
    
    public List<Token> find(Work w, int start, int end);
    
    public List<Token> find(Structure s);
    
//    public void save(Token t);
}
