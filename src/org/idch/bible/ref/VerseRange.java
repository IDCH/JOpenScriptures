/**
 * 
 */
package org.idch.bible.ref;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author Neal Audenaert
 */
public class VerseRange extends Passage {
    private static final Logger LOGGER = Logger.getLogger(VerseRange.class);
    
    // TODO implements Comparable<VerseRange>
    //      add in overlap tests, add in merge operation
    
    //=======================================================================================
    // SYMBOLIC CONSTANTS
    //=======================================================================================
    
    private static String BAD_VERSE_ORDER = 
        "The supplied ending verse is before the starting verse.";
    
    //=======================================================================================
    // STATIC METHODS
    //=======================================================================================
    
    /** 
     * Checks to see if the supplied reference is a valid verse range.
     * 
     * @param ref The reference to check. 
     * @throws InvalidReferenceException If the supplied reference is not valid.
     */
    public static void checkReference(String ref) 
    throws InvalidReferenceException {
        // TODO should use a RegEx, migrate to Passage class
        String UNEXPECTED_NUMBER_OF_VERSES = 
            "Expected at most two verses separated by a '-', but found NUM verses";
        
        String[] references = ref.split("-");
        if (references.length > 2) {
            String msg = UNEXPECTED_NUMBER_OF_VERSES.replace("NUM", references.length + "");
            throw new InvalidReferenceException(msg, ref);
        }
    }
    
    //=======================================================================================
    // MEMBER VARIABLES
    //=======================================================================================
	
	private VerseRef start;
	private VerseRef end;

	//=======================================================================================
    // CONSTRUCTORS
    //=======================================================================================
    
	/**
	 * Creates a verse range from the specified start and end verses.
	 * 
	 * @param start The first verse in the range.
	 * @param end The last verse in the range.
	 * 
	 * @throws InvalidReferenceException If the start verse comes after the end verse.
	 */
	public VerseRange(VerseRef start, VerseRef end) {
		super(start.getBookOrder());
		this.start = start;
		this.end = end;
		
		if (start.compareTo(end) > 0)  
		    throw new InvalidReferenceException(BAD_VERSE_ORDER, this.toOsisId());
	}
	
	/**
     * Creates a new verse range with the specified string valued reference and a default 
     * book order.
     * 
     * @param ref The string valued reference to be parsed. This should contain at most two 
     *     verse references separated by a hyphen. If the second reference is underspecified 
     *     (e.g. <tt>1 Peter 2:20-22</tt>) it will be interpreted relative to the first verse.  
     *     
     * @throws InvalidReferenceException If the supplied reference is badly formatted or 
     *      if the start verse comes after the end verse.
     */
	public VerseRange(String ref) {
		this(BookOrder.KJV, ref);
	}
	
	/**
	 * Creates a new verse range with the specified book order and string valued reference.
	 * @param order The book order to be used.
	 * @param ref The string valued reference to be parsed. This should contain at most two 
     *     verse references separated by a hyphen. If the second reference is underspecified 
     *     (e.g. <tt>1 Peter 2:20-22</tt>) it will be interpreted relative to the first verse.  
     *     
     * @throws InvalidReferenceException If the supplied reference is badly formatted or 
     *      if the start verse comes after the end verse.   
	 */
	public VerseRange(BookOrder order, String ref) {
		super(order);
		LOGGER.debug("Parsing verse range: " + ref);
		checkReference(ref);
		
		String[] references = ref.split("-");
		String a = StringUtils.trimToEmpty(references[0]);
		String b = (references.length == 2) 
            		    ? StringUtils.trimToEmpty(references[1]) : a; 
		
		this.start = new VerseRef(order, a);
		this.end = new VerseRef(start, b);
		
        if (start.compareTo(end) > 0)  
            throw new InvalidReferenceException(BAD_VERSE_ORDER, ref);
        LOGGER.debug(" ... done parsing verse range: " + ref);
	}
	
	//=======================================================================================
    // ACCESSORS
    //=======================================================================================
    
	/** Returns the first verse in this range. */
	public VerseRef getFirst() {
		return this.start;
	}
	
	/** Returns the last verse in this range. */
	public VerseRef getLast() {
		return this.end;
	}
	
	   
    //=======================================================================================
    // OBJECT METHOD OVERRIDES
    //=======================================================================================
    
	private StringBuilder appendExt(VerseRef ref, String prefix, StringBuilder sb) {
	    if (ref.getExtension() != null)
	        sb.append(prefix).append(ref.getExtension());
	    
	    return sb;
	}
	
	private StringBuilder appendVerse(VerseRef ref, String prefix, StringBuilder sb) {
        if (ref.isVerseSpecified())
            sb.append(prefix).append(ref.getVerse());
        
        return sb;
    }
	
	/** 
	 * Returns a string representation of this verse range.
	 */
	public String format() {
		if (start.equals(end)) 
		    return start.format();
		
		StringBuilder sb = new StringBuilder();
		sb.append(start.format());
		
		if (end != null && !start.equals(end)) {
		    if (start.getBookIndex() != end.getBookIndex()) {
		        sb.append(" - ").append(end.format());
		    } else if (end.isChapterSpecified() && start.getChapter() != end.getChapter()) {
		        sb.append("-").append(end.getChapter());
		        appendVerse(end, ":", sb);
		        appendExt(end, "", sb);
		    } else if (end.isVerseSpecified() && (start.getVerse() != end.getVerse())) {
		        sb.append("-").append(end.getVerse());
		        appendExt(end, "", sb);
		    } else if ((end.getExtension() != null) && 
		                start.getExtension().equals(end.getExtension())) {
		        sb.append("-").append(end.getExtension());
		    }
		}
		
		return sb.toString();
	}
	
	public String toOsisId() {
	    StringBuilder sb = new StringBuilder();
        sb.append(start.toOsisId());
	    if (end != null && !start.equals(end)) {
	        sb.append("-").append(end.toOsisId());
	    } 
	    
	    return sb.toString();
	}
	
	public String toString() {
	    return this.format();
	}
}
