/**
 * 
 */
package org.idch.bible.ref;

import java.util.List;
import java.util.Map;


/**
 * Represents an organizational system for the Bible. The specific form that 
 * the Bible takes, from the ordering of individual books, the names given 
 * for those books, and even which books are included varies between different 
 * communities. This class provides a general-purpose API for representing 
 * many of those different practices.  
 * 
 * @author Neal Audenaert
 */
@SuppressWarnings("unused")
public class BibleBookNames {
	// TODO generate METS record from this info.
	
	// NOTE Reference Parser implementation here
	//	    http://code.google.com/p/open-scriptures/source/browse/branches/php-prototypes/reference-parser.lib.php
	
	public static class BookNameLeader {
        private String canonicalForm;
		private String[] alternateForms;
	}
	
	// TODO this seems like it is different? Maybe should be in a separate class.
	public class BibleDivision {
		private String stdAbbr;
		private String name;
		private List<String> abbreviations;
		private List<BookName> books;
	}
	
	private SystemDescription information;
	
	private Map<String, BibleDivision> divisions;
	private Map<String, BookName> books;
	
	public BookName findBook(String query) {
		
		// TODO IMPLEMENT
		return null;
	}

}
