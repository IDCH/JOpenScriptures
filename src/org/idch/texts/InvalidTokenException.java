/**
 * 
 */
package org.idch.texts;

/**
 * @author Neal Audenaert
 */
public class InvalidTokenException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8404931272777227773L;

    private final Token t;
    
    @SuppressWarnings("unused")
    private final String msg;
    
    /**
     * @param string
     */
    public InvalidTokenException(String msg) {
        super("InvalidToken: " + msg);
        
        this.t = null;
        this.msg = msg;
    }
    
    public InvalidTokenException(String msg, Token t) {
        super("InvalidToken: " + msg);
        
        this.t = t;
        this.msg = msg;
    }
    
    public Token getToken() {
        return this.t;
    }
}
