/**
 * 
 */
package org.idch.texts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import org.idch.persist.RepositoryAccessException;
import org.idch.texts.Work;
import org.idch.texts.WorkId.Type;
import org.idch.texts.util.Contributor;
import org.idch.texts.util.Language;
import org.idch.texts.util.License;
import org.idch.util.Cache;

/**
 * 
 * 
 * @author Neal Audenaert
 */
public class Work extends AbstractTokenSequence {

    private static final String WHITESPACE = (" ").intern();
    
	//============================================================================================
	// MEMBER VARIABLES
	//============================================================================================  
	    
	protected Long id;
	protected UUID uuid;
	
	protected String title;             /** The title of this work. */
	protected String abbreviation;      /** An abbreviated title for this work. */
	protected String description;       /** A description of this work. */
	
	protected String creator;		    /** The individual or institution primarily responsible for creating this work. */ 
	protected String publisher;         /** The publisher of this work */
	protected String language;          /** The primary language of this work. */
	protected String type;              /** The type of this work (e.g., Bible, Commentary, Quran). TODO need better semantics for this. */
	protected String copyright;         /** A copyright statement for this work. */	
	protected String scope;             /** The scope of this work (e.g. Gen-Rev, New Testament) */
	protected String refSystem;         /** The reference system used to identify and resolve passages. */
	protected String sourceUrl;         /** URL where this resource was originally obtained. */
	
	protected String publicationDate;
	protected Date importDate;
	
	// create as embeddable class? This is currently very simplified. 
	protected Set<Contributor> contributors = new HashSet<Contributor>();
	
	// TODO need to implement support for providing license definitions. 
	protected License license;	
	
	// TOKENS AND CACHING
	//===============================
	
	private TokenRepository tokens = null;
	
	private int tokenBufferStart = 0;
	private List<Token> tokenBuffer = new ArrayList<Token>();
	
    boolean lastTokenWasWhitespace = true;         // used when importing tokens

    // Of dubious value - cache at the repo level
	private Cache<Integer, Token> tokenCache = new Cache<Integer, Token>("tokens", 1000);

//============================================================================================
// CONSTRUCTORS
//============================================================================================	
    	
	public Work() {
		
	}
	
	public Work(long id) {
	    this.id = id;
	}
	
	public Work(UUID id) {
	    this.uuid = id;
	}
	
	public Work(WorkId id) {
	    this.uuid = UUID.randomUUID();
	    
	    this.abbreviation = id.getName();
	    this.type = id.getType().value;
	    this.language = id.getLgCode();
	    this.publisher = id.getPublisher();
	    this.publicationDate = id.getPublicationDate();
	}
	
	public Work(String title, String abbr, String desc) {
		this.uuid = UUID.randomUUID();
		
		this.title = title;
		this.abbreviation = abbr;
		this.description = desc;
	}
	
	public TokenRepository getTokenRepository() {
	    if (this.tokens == null) {
            try {
                this.tokens = TextModuleInstance.get().getTokenRepository();
            } catch (RepositoryAccessException e) {
                this.tokens = null;
            } 
	    }
	    
	    assert this.tokens != null : "Could not load token repository.";
	    if (this.tokens == null) {
            throw new RepositoryNotInitializedException("Could not load token repository.");
        }
	    
	    return this.tokens;
	}

	//===================================================================================
	// METHODS TO CREATE AND QUERY THE CONTENTS OF THIS WORK
	//===================================================================================
	
	private boolean useTokenBuffer = true;
	private int maxTokenBuffer = 100;
	
	public void flushTokens() {
	    this.getTokenRepository().create(tokenBuffer);
        tokenBuffer.clear();
        this.tokenBufferStart = this.getTokenRepository().getNumberOfTokens(this);
	}
	
	/**
     * 
     * @param value
     * @return
     */
    public Token append(String value) {
        // FIXME  this may have synchronization issues, but this will be used only in
        //        pretty rare instances, so it likely doesn't pose a problem in the 
        //        near term (2011-2012)
        
        Token t = new Token(this, this.size(), value);
        if (useTokenBuffer) {
            // TODO synchronize on tokenBuffer
            tokenBuffer.add(t);
            if (tokenBuffer.size() > maxTokenBuffer) {
                this.flushTokens();
            }
        } else {
            t = this.getTokenRepository().create(t);
        }
        
        synchronized (tokenCache) {
            tokenCache.cache(t.getPosition(), t);
        }
        
        return t; 
    }
    
 
    
    /**
     * 
     * @param w
     * @param text
     * @return
     */
    public List<Token> appendAll(String text) {
        List<Token> tokens = new ArrayList<Token>();
        if (text == null)
            return tokens;
        
        Matcher mat = Pattern.compile(Token.TOKENIZATION_PATTERN).matcher(text);
        while (mat.find()) {
            String token = mat.group();

            Token.Type type = Token.classify(token);
            if (type == null) {
                continue;       // TODO do something about this 

            } else if (type == Token.Type.WHITESPACE) {
                if (!lastTokenWasWhitespace) { // normalize whitespace.
                    tokens.add(append(WHITESPACE));
                }

                lastTokenWasWhitespace = true;
            } else {
                lastTokenWasWhitespace = false;
                tokens.add(append(token));
            }
        }
       
        return tokens;
    }
    
	/** Returns the token at the specified index. */
    public Token get(int index) {
        Token t = null;
        if ((this.useTokenBuffer) && (index > this.tokenBufferStart)) {
            int ix = index - this.tokenBufferStart;
            if (ix >= this.tokenBuffer.size()) {
                throw new IndexOutOfBoundsException("Index: " + ix + ", Size: " + tokenBuffer.size());
            }
            t = this.tokenBuffer.get(index - this.tokenBufferStart);
        } else {
            t = tokenCache.get(index);
            synchronized (tokenCache) {
                if (t == null) {
                    TokenRepository tokens = this.getTokenRepository();
                    t = tokens.find(this, index);
                    if (t != null)
                        tokenCache.cache(t.getPosition(), t);
                }
            }
        }
        
        return t;
    }

    public int size() {
        int sz = 0;
        if (this.useTokenBuffer) {
            sz = this.tokenBufferStart + this.tokenBuffer.size();
        } else {
            sz = this.getTokenRepository().getNumberOfTokens(this);
        }
        
        return sz;
    }
	
	
//============================================================================================
// ACCESSORS AND MUTATORS
//============================================================================================
	
	/** Returns the unique persistent identifier for this work. */
	public Long getId() { return this.id; }
	/** Sets the unique persistent identifier for this work. */
	public void setId(Long id) { this.id = id; }
	
	/**
	 * Internal ID used to represent a specific local instance of a work. Note that a single 
	 * logical work as represented by a <tt>WorkId</tt> (e.g., the ESV 2001 edition) may 
	 * have multiple representations in the system if, for example, it was imported at 
	 * different times or with different import strategies.
	 */
	public UUID getUUID() { return this.uuid; }
	
	/** Returns the UUID as a string. Intended to be used by the persistence framework. */
	String getUUIDString() { return this.uuid.toString(); }
	/** Sets the UUID as a string. Intended to be used by the persistence framework. */
	public void setUUIDString(String id) { this.uuid = UUID.fromString(id); }
		
	/** The <tt>WorkId</tt> of this work. */
	public WorkId getWorkId() { 
	    WorkId workId = new WorkId();
	    workId.setName(this.getAbbreviation());
	    workId.setType(Type.find(this.getType()));
	    workId.setPublisher(this.getPublisher());
	    workId.setPublicationDate(this.getPublicationDate());
	    workId.setLgCode(this.getLgCode());
	    
	    return workId;
	}
	
	/** The title of this work. */
	public String getTitle() { return title; }
	 /** Sets the title. */
    public void setTitle(String title) { this.title = title; }

	/** An abbreviation of this work's title. */
	public String getAbbreviation() { return this.abbreviation; }
	/** Sets the abbreviated form of the title. */
    public void setAbbreviation(String abbr) { this.abbreviation = abbr; }
	
	/** A description of this work. */
	public String getDescription() { return this.description; }
	/** Sets the description of this work. */
    public void setDescription(String desc) {  this.description = desc; }

	/** The URL where this resource was originally obtained. */
	public String getSourceUrl() {  return this.sourceUrl; }
	 /** Sets the URL where this resource was originally obtained. */ 
    public void setSourceUrl(String url) {  this.sourceUrl = url; }
    
	/** The individual or organization responsible for creating this work. */
	public String getCreator() { return this.creator; }
	/** Sets the creator of this work. */
    public void setCreator(String creator) {  this.creator = creator; }
    
    /** Return the publisher of this work. */
    public String getPublisher() { return this.publisher; } 
    /** Sets the publisher of this work. */
    public void setPublisher(String value) { this.publisher = value; }
    
    /** Returns the date this work was published. */
    public String getPublicationDate() { return this.publicationDate; } 
    /** Sets the date this work was published. */
    public void setPublicationDate(String date) { this.publicationDate = date; }

	/** Returns a description of the copyright holder. */
	public String getCopyright() { return copyright; }
	 /** Sets the copyright holder of this work. */
    public void setCopyright(String copyright) {  this.copyright = copyright; }

	/** Returns a license under which this work is made available.  */
	public License getLicense() { return license; }
	/** Sets the license under which this work is made available. */
    public void setLicense(License license) { this.license = license; }
    
	/** Return the date that this version of the work was imported. */
	public Date getImportDate() { return importDate; }
	 /** Sets the date that this version of the work was imported. */
    public void setImportDate(Date importDate) { this.importDate = importDate; }
    
    /** Returns the type of this work (e.g., Bible, Quran, etc.) */
    public String getType() { return this.type; }
    /** Sets the type of work (e.g., Bible, Quran, Commentary) of this work. */
    public void setType(String value) { this.type = value; } 
    
    /** Returns the primary language used in this work. */
    public Language getLanguage() { 
        return Language.lookup(this.language);
    }
    /** Sets the primary language used in this work. */
    public void setLanguage(Language lg) { this.language = lg.getIsoIdentifier(); }
    
    /** Returns the language code. */
    public String getLgCode() { return this.language; }
    /** Sets the language code. Primarily intended for use by the persistence layer. */
    public void setLgCode(String lgCode) { this.language = lgCode; }

    /** Returns the scope (e.g., scripture range) of this work. */
    public String getScope() { return this.scope; }
    /** Sets the scope (e.g. range of scripture) covered by this work. */
    public void setScope(String value) { this.scope = value; }

    /** Returns the reference system used by this work. */
    public String getRefSystem() { return this.refSystem; }
    /** Sets the reference system used by this work. */
    public void setRefSystem(String value) { this.refSystem = value; }
    
//============================================================================================
// CONTRIBUTOR METHODS
//============================================================================================
    
    /** Returns an unmodifiable <tt>Set</tt> of the contributors to this work. */
    public Set<Contributor> getContributors() { 
        return Collections.unmodifiableSet(this.contributors); 
    }
    
    /**
     * Adds the indicated contributor. 
     * 
     * @param contributor The contributor to add.
     * @return <tt>true</tt> if the set of contributors was modified (that is, if the 
     *      contributor was not already in the set of contributors).
     */
    public boolean addContribotr(Contributor contributor) {
        return this.contributors.add(contributor);
    }
    
    /** 
     * Removes the indicated contributor. 
     * 
     * @param contributor The contributor to remove.
     * @return <tt>true</tt> if the set of contributors was modified (that is, if the 
     *      contributor was in the set of contributors).
     */
    public boolean removeContributor(Contributor contributor) {
        return this.contributors.remove(contributor);
    }
    
    /** Sets the contributors to this work. */
    protected void setContributors(Set<Contributor> contributors) {
        this.contributors = contributors;
    }
    
//============================================================================================
// TOKEN SEQUENCE METHODS
//============================================================================================


    /* (non-Javadoc)
     * @see openscriptures.text.TokenSequence#getWork()
     */
    public UUID getWorkUUID() {
        return this.uuid;
    }

    
    /* (non-Javadoc)
     * @see openscriptures.text.TokenSequence#getText()
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        
        Iterator<Token> i = this.iterator(); 
        while (i.hasNext()) {
            sb.append(i.next().getText());
        }
        
        return sb.toString();
    }

    /** 
     * Return the start position (inclusive) in the underlying <tt>Work</tt>'s token 
     * stream. This will always be <tt>0</tt>. 
     */
    public int getStart() {
        return 0;
    }
     
    /** 
     * Return the end position (exclusive) in the underlying <tt>Work</tt>'s token stream.  
     */
    public int getEnd() {
        return this.size();
        
    }
    
    private static class RepositoryNotInitializedException extends RuntimeException {
        private static final long serialVersionUID = -5178662703322206219L;

        private RepositoryNotInitializedException(String msg) {
            super(msg);
        }
    }
}
