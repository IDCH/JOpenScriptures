/**
 * 
 */
package org.idch.bible.ref;

/**
 * A fully general implementation of an OSIS ID.
 * @author Neal Audenaert
 */
public class OsisRef {
	/* FROM THE OpenScriptures Python module. 
	 * 
	 * OSIS passage such as Exodus.3.8: an osisID without the the work prefix.
	 * 
	 * Organized into period-delimited segments increasingly narrowing in scope,
	 * followed by an optional sub-identifier which is work-specific. Segments
	 * usually consist of an alphanumeric book code followed by a chapter number
	 * and a verse number. While there are conventions for book names, they are not
	 * standardized and one system is not inherently preferable over another, as
	 * the goal is to affirm canon-neutrality. Likewise, there is nothing
	 * restricting the segments to a single book, chapter, and verse identifier.
	 * There may be a book volumn identifier, or alphabetical chapters, or even
	 * numbered paragraph segments.
	 * 
	 * Read what Chris Little of Crosswire says:
	 * http://groups.google.com/group/open-scriptures/msg/4fb744efb27c1a41?pli=1
	 * 
	 * OSIS Manual: "Translations also often split verses into parts,
	 * provided labels such as 'a' and 'b' for the separate parts. Encoders
	 * may freely add sub-identifiers below the lowest standardized level.
	 * They are set off from the standardized portion by the character '!'"
	 * 
	 * Schema Regexp:
	 * identifier: ((\p{L}|\p{N}|_|(\\[^\s]))+)((\.(\p{L}|\p{N}|_|(\\[^\s]))+)*)?
	 * subidentifier: (!((\p{L}|\p{N}|_|(\\[^\s]))+)((\.(\p{L}|\p{N}|_|(\\[^\s]))+)*)?)?
	 * 
	 */
}
