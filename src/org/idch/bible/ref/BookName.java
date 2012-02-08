/**
 * 
 */
package org.idch.bible.ref;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a book of the Bible within a specific language and tradition. 
 * 
 * @author Neal Audenaert
 */
public class BookName {
	
	/** A three letter abbreviation that uniquely identifies this book within the context
	 *  of a particular naming system. */
	private String id;
	
	/**  A common or authoritative name for this particular book of the Bible. */
	private String name;
	
	/** A list of alternate forms of this book, including abbreviations, misspellings, 
	 *  and different naming practices.  */
	private Set<String> alternateForms;
	
	public BookName(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public BookName(String id, String name, List<String> altForms) {
		this.id = id;
		this.name = name;
		this.alternateForms = new HashSet<String>(altForms);
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getAlternateForms() {
		return Collections.unmodifiableSet(this.alternateForms);
	}
	
	public List<String> getExpandedFormsList() {
		// TODO implement this to generate a giant list of possible search terms for this 
		//		book. -- MAYBE --
		return null;
	}
}