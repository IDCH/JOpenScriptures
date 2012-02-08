/**
 * 
 */
package org.idch.texts;

import java.util.UUID;


/**
 * An atomic unit of text, such as a word, punctuation mark, or whitespace. 
 * Corresponds to OSIS w elements.
 * 
 * What to do about tokens that don't appear in a verse? i.e. when an osisID isn't fully 
 * specified or doesn't exist at all? For example, introductory material or titles? We could 
 * just use the best available osisID, e.g. it could be as basic as just bookName or 
 * bookName.chapterNumber.
 * 
 * If a hash is a duplicate with a previous hash, should the subsequent word merely increase 
 * the look-behind for the n-gram? This way, the subsequent change won't ripple. But if we 
 * change a word in the middle of a verse, and that ID conflicts with a subsequent one, do we 
 * really want to change the subsequent ID alone? It wasn't even the token that changed in the 
 * first place.
 * 
 * @author Neal Audenaert
 */
public class Token implements Comparable<Token> {
    // TODO restrict access to setters probably through proxy objects and protected methods
    
    public static enum Type {
        WORD, PUNCTUATION, WHITESPACE
    }
    
    //====================================================================================
    // SYMBOLIC CONSTANTS
    //====================================================================================

    /** RegEx for representing Unicode word characters. */
    public static final String UNICODE_WORDSCHARS = 
        "[\\p{L}\\p{Sk}\\d]";
    
    /** RegEx for representing Unicode punctuation characters. */
    public static final String UNICODE_PUNCTUATION = 
        "[\\p{P}]";
    
    /** RegEx for representing Unicode whitespace characters. */
    public static final String UNICODE_WHITESPACE = 
        "[\\p{Z}\\u000a\\u0009]";
    
    /** RegEx for representing unknown characters (not words, whitespace or punctuation). */
    public static final String UNICODE_UNKNOWN = 
        "[^\\p{L}\\p{Sk}\\d\\p{P}\\p{Z}]";
    
    /** RegEx for tokenizing text. */
    public static final String TOKENIZATION_PATTERN =
        UNICODE_WORDSCHARS + "+|" +                 // Unicode letters
        UNICODE_PUNCTUATION + "+|" +                // Unicode punctuation
        UNICODE_WHITESPACE + "+|" +                 // Java whitespace (becase I can't figure out Unicode whitespace)
        UNICODE_UNKNOWN + "+";                      // everything else (typically ignored).
    

    //====================================================================================
    // STATIC METHODS
    //====================================================================================

    /**
     * 
     * @param string
     * @return
     */
    public static Token.Type classify(String string) {
        Token.Type type = null;
        if (string.matches(UNICODE_WORDSCHARS + "+")) {
            type = Token.Type.WORD;
        } else if (string.matches(UNICODE_PUNCTUATION + "+")) {
            type = Token.Type.PUNCTUATION;
        } else if (string.matches(UNICODE_WHITESPACE + "+")) {
            type = Token.Type.WHITESPACE;
        }
        
        return type;
    }
    
    //====================================================================================
    // MEMBER VARIABLES
    //====================================================================================
    
    private Long id;
	private UUID uuid;
	
	private Work work;
	private Token.Type type;
	private String value;
	private int position;
	
	//====================================================================================
    // CONSTRUCTORS
    //====================================================================================
	
	/**
	 * 
	 */
	Token() {
	    
	}

	/**
	 * 
	 * @param work
	 * @param position
	 * @param text
	 */
	public Token(Work work, int position, String text) {
		this.uuid = UUID.randomUUID();
		
		this.work = work;
		this.value = text;
		this.type = classify(text);
		this.position = position;
	}
	
	
	//====================================================================================
    // ACCESSORS & MUTATORS
    //====================================================================================
	
	/** Returns the unique persistent identifier for this work. */
    public Long getId() { return this.id; }
    /** Sets the unique persistent identifier for this work. */
    public void setId(Long id) { this.id = id; }
    
	/** Returns the unique identifier for this token. */
	public UUID getUUID() { return this.uuid; }
	public void setUUID(UUID uuid) { this.uuid = uuid; }
	
	/** Returns the unique identifier for this token as a string. */
	public String getUUIDString() { return this.uuid.toString(); }
	/** Used by the persistence framework to set the unique identifier for this token. */
	public void setUUIDString(String value) { this.uuid = UUID.fromString(value); }
	
	public Work getWork() { return work; }
	public void setWork(Work w) { this.work = w; }
	
	/** Returns the textual value of this token. */
    public String getText() { return this.value; }
    /** Used by the persistence layer to set this token's textual value. */
    void setText(String value) { this.value = value; }
    
    /** Returns the position of this token in the associated work's token stream. */
    public int getPosition() { return this.position; }
    /** Used by persistence layer to set the position of this token in the token stream. */
    public void setPosition(int pos) { this.position = pos; }

    /** Returns the type of token. */
    public Token.Type getType() { return this.type; }
    /** Used by the persistence layer to set this token's type. */
    public void setType(Token.Type t) { this.type = t; }
    
    //====================================================================================
    // NEXT & PREV METHODS
    //====================================================================================
    
    /** Checks to see if there are more tokens following this one in the associated work. */
    public boolean hasNext() {
        return position < (work.getEnd() - 1);
    }
    
    /** Returns the next token in this work. */
    public Token next() {
        if (!this.hasNext())
            return null;
        
        return work.get(position + 1);
    }
    
    /** Returns the next token in this work, optionally ignoring whitespace tokens. */
    public Token next(boolean ignoreWhitespace) {
        if (!ignoreWhitespace) 
            return this.next();
        
        Token t = this;
        while (t.hasNext()) {
            t = t.next();
            
            if (t.getType() != Token.Type.WHITESPACE) 
                return t;
            
        }
        
        return null;
    }
    
    /** Checks to see if there are more tokens preceding this one in the associated work. */
    public boolean hasPrev() {
        return position > 0;
    }
    
    /** Returns the next token in this work. */
    public Token prev() {
        if (!this.hasPrev())
            return null;
        
        return work.get(position - 1);
    }
    
    /** Returns the next token in this work, optionally ignoring whitespace tokens. */
    public Token prev(boolean ignoreWhitespace) {
        if (!ignoreWhitespace) 
            return this.prev();
        
        Token t = this;
        while (t.hasPrev()) {
            t = t.prev();
            
            if (t.getType() != Token.Type.WHITESPACE) 
                return t;
            
        }
        
        return null;
    }
    
    
    //====================================================================================
    // UTILITY METHODS
    //====================================================================================
    
    /** Returns a string based representation of this token. */
    public String toString() {
        return this.value;
    }
    
    /** Compares this token to another token in the same work.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Token token) {
        // TODO check to make sure this is the same work.
        
        if (this.position < token.getPosition()) return -1;
        else if (this.position > token.getPosition()) return 1;
        else return 0;
    }

}
