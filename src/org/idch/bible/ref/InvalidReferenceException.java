/**
 * 
 */
package org.idch.bible.ref;

/**
 * @author Neal Audenaert
 */
public class InvalidReferenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String ref;
	
	InvalidReferenceException(String msg, String ref) {
		super("Invalid Verse Reference ('" + ref + "'): " + msg);
		
		this.ref = ref;
	}
	
	public String getReference() {
	    return ref;
	}
}
