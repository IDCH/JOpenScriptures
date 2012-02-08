/**
 * 
 */
package org.idch.texts;

import java.util.ArrayList;
import java.util.List;

import org.idch.texts.util.Language;


/**
 * @author Neal Audenaert
 */
public class WorkId {
	// Most of these TODO's are not properly a part of the OSIS framework.
	
	public static enum Type {
		// XXX This seems like the wrong approach. If we need multiple types, we'll
		//     need some serious refactoring of the methods for representing books, 
		//	   book orders etc. 
		
		// TODO Should think about implementing something more akin to FRBR. 
		//      Work corresponds to Bible , 
		//		Expression is the translation/edition 
		//		Manifestation is a particular edition/printing
		//		Item matters for MSS and/or specific imports
		
		BIBLE ("Bible"),
	    QURAN ("Quran"),
	    MISHNAH ("Mishnah"),
	    TALMUD ("Talmud"),
	    BOOK_OF_MORMON ("BookOfMormon"),
	    UNKNOWN ("unknown")
	    ;
		
		public final String value;
		
		Type(String v) {
			this.value = v;
		}
		
		/**
		 * Returns the appropriate enum instance for a string-valued type. Currently, this 
		 * matches the lower-case value of the name.
		 * 
		 * @param value
		 * @return
		 */
		public static Type find(String value) {
			value = value.toLowerCase();
			
			for (Type t : Type.values()) {
				if (t.value.toLowerCase().equals(value))
					return t;
			}
			
			return UNKNOWN;
		}
	}
	
//============================================================================================
// MEMBER VARIABLES
//============================================================================================	

	private Type type = null;
	private Language language;
	private String publisher;
	private String name;
	private String publicationDate;

//============================================================================================
// CONSTRUCTORS
//============================================================================================  

	/** Default constructor required for persistence layer. */
	WorkId() {
	    
	}
	
	/**
	 * 
	 * @param name
	 * @param date
	 */
	public WorkId(String name, String date) {
		this.name = name;
		this.publicationDate = date;
	}
	
	/**
	 * 
	 * @param type
	 * @param lg
	 * @param publisher
	 * @param name
	 * @param pubDate
	 */
	public WorkId(Type type, Language lg, String publisher, String name, String pubDate) {
		this.type = type;
		this.language = lg;
		this.publisher = publisher;
		this.name = name;
		this.publicationDate = pubDate;
	}
	
	/**
	 * 
	 * @param work
	 */
	public WorkId(String work) {
		// parse
		String[] segments = work.split("\\.");
		int ix = 0,			            // the index for the current segment 
		    len = segments.length;		// the number of segments
		
		if (ix >= len) {
			// BAD THINGS - this is probably an error. Should throw something.
			return;
		}
		
		// check to see if the current element is a work type
		// XXX This requires named types but this limits the different types of works 
		//     that we can represent. Need to split works off into their own class.
		Type t = Type.find(segments[ix]); 
		if (t != null) {
			this.type = t;
			if (t != Type.UNKNOWN) {
			    ix++;
			    if (ix >= len) return;	// just a type identifier 		
			}
		}
		
		// check to see if the current element is a language type
		if (segments[ix].matches("^[a-z]{2,3}$")) {
			this.language = Language.lookup(segments[ix++]);
			
			if (ix >= len) return;
		}
		
		List<String> slugs = new ArrayList<String>();
		for (; ix < len; ix++) {
			String segment = segments[ix];
			
			if (segment.matches("\\d{4}")) {
				// TODO For now, we'll just use YYYY style dates. Need to extend this to
				//		make it more robust.
				this.publicationDate = segment;
			} else {
				slugs.add(segment);
			}
		}
		
		if (slugs.size() == 1) {
			this.name = slugs.get(0);
		} else if (slugs.size() == 2) {
			this.publisher = slugs.get(0);
			this.name = slugs.get(1);
		} else if (slugs.size() != 0) {
			// XXX bad news. Invalid OSIS Work ID. Unexpected fields.
		}
		
		// TODO handle revision number, version number, and edition number?
	}
	
    //===================================================================================
    // ACCESSORS
    //===================================================================================

	/** Returns the type of this work. */
    public Type getType() { return this.type; }
    void setType(Type t) { this.type = t; }
	
	/** Returns the language of this work */
	public Language getLanguage() { return this.language; }
	String getLgCode() { return (language != null) ? this.language.getIsoIdentifier() : null; }
	void setLgCode(String lgCode) {
	    this.language = Language.lookup(lgCode); 
	}
	
	/** Returns the publisher of this work. */
	public String getPublisher() { return this.publisher; }
	void setPublisher(String pub) { this.publisher = pub; }
	
	/** Returns the name of this work. */
	public String getName() { return this.name; }
	void setName(String name) { this.name = name; }

	/** Returns the date this work was published. */
	public String getPublicationDate() { return this.publicationDate; }
	void setPublicationDate(String date) { this.publicationDate = date; }
	
	//===================================================================================
	// UTILITY METHODS
	//===================================================================================

	/** Returns the properly formatted OSIS identifier for this work. */
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (this.type != null) builder.append(type.value);
		if (this.language != null) {
			if (builder.length() > 0) builder.append(".");
			builder.append(this.language.getIsoIdentifier());
		}
		
		if (this.publisher != null) {
			if (builder.length() > 0) builder.append(".");
			builder.append(this.publisher);
		}
		
		if (this.name != null) {
			if (builder.length() > 0) builder.append(".");
			builder.append(this.name);
		}
		
		if (this.publicationDate != null) {
			if (builder.length() > 0) builder.append(".");
			builder.append(this.publicationDate);
		}
		
		return builder.toString();
	}
	
	public boolean equals(Object o) {
	    String target = null;
	    if (o instanceof WorkId) {
	        target = ((WorkId)o).toString();
	    } else if (o instanceof String) {
	        target = (String)o;
	    } else {
	        @SuppressWarnings("unused")
            WorkId wid = (WorkId)o;    // force class cast exception
	    }
	    
	    return this.toString().equals(target);
	}
	
	public int hashCode() {
	    return this.toString().hashCode();
	}
}
