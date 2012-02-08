/**
 * 
 */
package org.idch.texts.util;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A human language, either ancient or modern.
 *  
 * @author Neal Audenaert
 */
public class Language implements Comparable<Language> {
    private static final Logger LOGGER = Logger.getLogger(Language.class);
    
	// ==================================================================================
	// SYMBOLIC CONSTANTS
    // ==================================================================================
	
	/**
	 * The ISO 639-3 identifier [mul] (multiple languages) should be applied when many 
	 * languages are used and it is not practical to specify all the appropriate 
	 * language codes.
	 */
	public static final String MULTIPLE = "mul";
	
	/**
	 * The ISO 639-3 identifier [und] (undetermined) is provided for those situations in which 
	 * a language or languages must be indicated but the language cannot be identified. 
	 */
	public static final String UNDETERMINED = "und";
	
	/** 
     * The ISO 639-3 identifier [mis] (Uncoded languages) is provided for those situations
     * in which there is not code point for a language.
     */
    public static final String UNCODED = "mis";
    
	/** 
	 * The ISO 639-3 identifier [zxx] (no linguistic content) may be applied in a situation
	 * in which a language identifier is required by system definition, but the item being 
	 * described does not actually contain linguistic content.
	 */
    public static final String NO_LG_CONTEXT = "zxx";
	
    /** Symbolic constant to look up languages by part 1 of the ISO 639 standard. */
    public final static int ISO_639_1 = 1;
    /** Symbolic constant to look up languages by part 2 of the ISO 639 standard. */
    public final static int ISO_639_2 = 2;
    /** Symbolic constant to look up languages by part 3 of the ISO 639 standard. */
    public final static int ISO_639_3 = 3;

    // ==================================================================================
    // ENUMERATIONS
    // ==================================================================================
    public enum Direction {
        // also needing vertical directions, see CSS writing-mode
        LTR, RTL;
    }

    /**
     * In the code table for ISO 639-3, the individual languages are identified as being 
     * of one of the following five types:
     * 
     * <ul>
     *   <li>Living languages</li>
     *   <li>Extinct languages</li>
     *   <li>Ancient languages</li>
     *   <li>Historic languages</li>
     *   <li>Constructed languages</li>
     * </ul>
     * 
     * @see {@link http://www.sil.org/iso639-3/types.asp}
     */
    public enum Type {
        LIVING("L", "Living languages", 
               "A language is listed as living when there are people still living who " +
               "learned it as a first language."),
        EXTINCT("E", "Extinct languages",
                "This part of ISO 639 also includes identifiers for languages that are no " +
                "longer living. A language is listed as extinct if it has gone extinct in " +
                "recent times. (e.g. in the last few centuries). The criteria for identifying " +
                "distinct languages in these case are based on intelligibility (as defined " +
                "for individual languages)."),
        ANCIENT("A", "Ancient languages", 
                "A language is listed as ancient if it went extinct in ancient times (e.g. " +
                "more than a millennium ago). In the case of ancient languages, a criterion " +
                "based on intelligibility would be ideal, but in the final analysis, " +
                "identifiers are assigned to ancient languages which have a distinct literature " +
                "and are treated distinctly by the scholarly community. In order to qualify " +
                "for inclusion in ISO 639-3, the language must have an attested literature or " +
                "be well-documented as a language known to have been spoken by some particular " +
                "community at some point in history; it may not be a reconstructed language " +
                "inferred from historical-comparative analysis."),
        HISTORIC("H", "Historic languages",
                 "A language is listed as historic when it is considered to be distinct " +
                 "from any modern languages that are descended from it; for instance, Old " +
                 "English and Middle English. Here, too, the criterion is that the language " +
                 "have a literature that is treated distinctly by the scholarly community."),
        CONSTRUCTED("C", "Constructed languages",
                    "This part of ISO 639 also includes identifiers that denote constructed " +
                    "(or artificial) languages. In order to qualify for inclusion the language " +
                    "must have a literature and it must be designed for the purpose of human " +
                    "communication. Specifically excluded are reconstructed languages and " +
                    "computer programming languages."),
        SPECIAL("S", "Special situations", "One of several codes defined for special " +
        		"situations.  These include multiple languages [mul], undetermined languages " +
        		"[und], uncoded languages [mis], and no linguistic content [zxx].");

        String code;
        String name;
        String desc;

        Type(String code, String name, String desc) {
            this.code = code;
            this.name = name;
            this.desc = desc;
        }

        static Type getType(char code) {
            Type t = null;

            switch(code) {
            case 'L' : t = LIVING; break;
            case 'E' : t = EXTINCT; break;
            case 'A' : t = ANCIENT; break;
            case 'H' : t = HISTORIC; break;
            case 'C' : t = CONSTRUCTED; break;
            case 'S' : t = SPECIAL; break;
            default: 
                throw new IllegalArgumentException("Illegal language type ('" + code + "'). " +
                		"Must be one of the following values: 'L', 'E', 'A', 'H', 'C', 'S'.");
            }

            return t;
        }
    }
	
    /**
     * A language identifier represents one or more language names, all of which 
     * designate the same specific language. The ultimate objects of identification 
     * are languages themselves; language names are the formal means by which the 
     * languages denoted by language identifiers are designated.
     * 
     * <p>Languages are not static objects; there is variation temporally, spatially, and 
     * socially; every language corresponds to some range of variation in linguistic
     * expression. In this part of ISO 639, then, a language identifier denotes some 
     * range of language varieties. The range of varieties that are denoted can have three 
     * different scopes: individual language, macrolanguage or collection.
     * 
     * <p>@see {@link http://www.sil.org/iso639-3/scope.asp}
     */
    public enum Scope {
        INDIVIDUAL, MACROLANGUAGE, SPECIAL;

        static Scope getScope(char code) {
            Scope s;

            switch(code) {
            case 'I' : s = INDIVIDUAL; break;
            case 'M' : s = MACROLANGUAGE; break;
            case 'S' : s = SPECIAL; break;
            default: 
                throw new IllegalArgumentException("Illegal language scope. Must be one of the " +
                        "following values: 'I', 'M', 'S'.");
            }
            return s;
        }
    }
    
    private static class XMLImporter  extends DefaultHandler {
        /**
         * 
         */
        public void startElement(String nsURI, String name, String qName, Attributes attrs)
                throws SAXException {
            if (!name.equals("iso_639_3_entry")) return; // root element, ignore

            String id = attrs.getValue("id");
            String lgName = attrs.getValue("name");  //TODO 'mis'
            String type = attrs.getValue("type");
            String scope = attrs.getValue("scope");
            String p1 = StringUtils.trimToNull(attrs.getValue("part1_code"));
            String p2 = StringUtils.trimToNull(attrs.getValue("part2_code"));

            Language.define(id, lgName, type, scope, p2, p1);
        }
    }
    
    public static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
	
    // ==================================================================================
    // STATIC MEMBERS
    // ==================================================================================
    
	private static Map<String, Language> iso_639_1 = new HashMap<String, Language>();
	private static Map<String, Language> iso_639_2 = new HashMap<String, Language>();
	private static Map<String, Language> iso_639_3 = new HashMap<String, Language>();
	
    // ==================================================================================
    // STATIC METHODS & INITIALIZATION
    // ==================================================================================
	
	static {
	    URL url = Language.class.getResource("iso_639_3.xml");
	    System.out.println(url.toExternalForm());
	    
	    SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        SAXParser saxParser;
        try {
            saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new XMLImporter());
            xmlReader.parse(url.toExternalForm());
        } catch (Exception e) {
            LOGGER.error("Could not load ISO 639 language definitions", e);
        } 
	}
	
	
	
	/**
     * @param id The ISO 639-3 identifier for this language.
     * @param lgName The English language name for this language.
     * @param type The type of language (living, extinct, ancient, historic, constructed).
     *      This must be one of the following values: <tt>"L", "E", "A", "H", "C"</tt>.
     * @param scope The scope of this language (individual, macrolanguage, or special 
     *      situations. This must be one of the following values:  <tt>"I", "M", "S"</tt>.
     * @param p2 The ISO 639-2 identifier for this language, if one exists.
     * @param p1 The ISO 639-1 identifier for this language, if one exists.
     */
    private static void define(String id, String lgName, String type, String scope, 
            String p2, String p1) {
        
        Type t = null;
        if (type == null || type.length() == 0) {
            throw new IllegalArgumentException("Language type must be specified.");
        }
        
        t = Type.getType(type.toUpperCase().charAt(0));
        
        Scope s = null;
        if (scope == null || scope.length() == 0) {
            throw new IllegalArgumentException("Language scope must be specified.");
        }
        
        s = Scope.getScope(scope.toUpperCase().charAt(0));
        
        Language lg = new Language(id, lgName, t, s, p2, p1);
        iso_639_3.put(id, lg);
        if (p2 != null) iso_639_2.put(p2, lg);
        if (p1 != null) iso_639_1.put(p1, lg);
    }
	
    /**
     * Looks up the language definition for the specified language code. If a two letter 
     * identifier, this will use the 639-1 specification, otherwise it will use 639-3.
     * 
     * @param code The two (ISO 639-1) or three (ISO 639-3) letter code for the language 
     *      to retrieve. 
     * @return The specified language or null if no such language is defined.
     */
	public static Language lookup(String code) {
	    int isoPart = (code.length() == 2) ? ISO_639_1 : ISO_639_3;
	    return lookup(code, isoPart);
	}
	
	/**
     * Looks up the language definition for the specified language code. If a two letter 
     * identifier, this will use the 639-1 specification, otherwise it will use 639-3.
     * 
     * @param code The two (ISO 639-1) or three (ISO 639-3) letter code for the language 
     *      to retrieve. 
     * @param isoPart The sub-part of the ISO 639 standard to use. This should be one of the 
     *      following symbolic constants: <tt>Language.ISO_639_1</tt>, 
     *      <tt>Language.ISO_639_2</tt>, <tt>Language.ISO_639_3</tt>,
     * @return The specified language or null if no such language is defined.
     */
	public static Language lookup(String code, int isoPart) {
        Language lg = null;
        switch(isoPart) {
        case ISO_639_1:
            lg = iso_639_1.get(code);
            break;
        case ISO_639_2:
            lg = iso_639_1.get(code);
            break;
        case ISO_639_3:
            lg = iso_639_1.get(code);
            break;
        default:
            throw new IllegalArgumentException();
        }
        
        return lg;
    }
	
	//==============================================================================
	// MEMBER VARIABLES
	//==============================================================================
	
	/** ISO 639-3 language code */
	private String code;
	/** ISO 639-1 two-letter language code (if defined) */
	private String p1 = null;
	/** ISO 639-2 three-letter language code (if defined) */
	private String p2 = null;
	
	private Type type;
	private Scope scope;
	
	/** Display name of the language. 
	 * 
	 * TODO For now, this is assumed to be in English, but we need to support 
	 *      internationalization.
	 */
	private String name;
	
	//==============================================================================
    // CONSTRUCTORS
    //==============================================================================
	
	/**
	 * Helper function to perform sanity checks on the supplied ISO codes.
	 * 
	 * @param id The ISO 639-3 identifier. Three-letter string, required.  
	 * @param p2 The ISO 639-2 identifier. Three-letter string, optional.
	 * @param p1 The ISO 639-1 identifier. Two-letter string, optional.
	 */
	private void checkIsoIdentifiers(String id, String p2, String p1) {
	    assert id != null && id.length() == 3 : 
	        "Must have a three letter language identifier. Found [" + id + "].";
        assert p2 == null || p2.length() == 3 : "If present, the ISO 639-2 identifier must " +
                "be a three-letter identifier. Found [\" + p2 + \"].\"";
        assert p1 == null || p1.length() == 2 : "If present, the ISO 639-1 identifier must " +
                "be a two-letter identifier. Found [\" + p1 + \"].\"";
        
        if (id == null || id.length() != 3) {
            throw new IllegalArgumentException(
                    "Must have a three letter language identifier. Found [" + id + "].");
        }
        
        if (p2 != null && p2.length() != 3) {
            throw new IllegalArgumentException("If present, the ISO 639-2 identifier must " +
                            "be a three-letter identifier. Found [" + p2 + "].\"");
        }
        
        if (p1 != null && p1.length() != 2) {
            throw new IllegalArgumentException("If present, the ISO 639-1 identifier must " +
                            "be a two-letter identifier. Found [" + p1 + "].\"");
        }
	}
    
	/** Constructor. */
    private Language(String id, String lgName, Type type, Scope scope, String p2, String p1) {
        checkIsoIdentifiers(id, p2, p1); 
        
        this.code = id;
        this.name = lgName;
        this.type = type; 
        this.scope = scope;
        
        this.p1 = p1;
        this.p2 = p2;
    }
    
	//==============================================================================
    // ACCESSORS
    //==============================================================================
  
	
	/** Returns the display name of this language (in English). */
	public String getName() {
		return this.name;
	}
	
	/** Returns the ISO 639-3 identifier of this language. */
	public String getIsoIdentifier() {
		return this.code;
	}
	
	/** Returns the ISO 639-2 identifier of this language. */
	public String getPart2Identifier() {
	    return this.p2;
	}
	
	/** Returns the ISO 639-1 identifier of this language. */
	public String getPart1Identifier() {
	    return this.p1;
	}
	
	/** 
	 * Returns the ISO 639-3 type of this language. ISO 639-3 classifies individual languages 
	 * according to one of five types:
	 * 
	 * <dl>
	 *   <dt>Living languages</dt>
	 *   <dd>A language is listed as living when there are people still living who learned 
	 *       it as a first language.</dd>
	 *       
	 *   <dt>Extinct languages</dt>
	 *   <dd>Identifiers for languages that are no longer living. A language is listed 
	 *       as extinct if it has gone extinct in recent times. (e.g. in the last 
	 *       few centuries). The criteria for identifying distinct languages in these case 
	 *       are based on intelligibility (as defined for individual languages).</dd>
	 *   
     *   <dt>Ancient languages</dt>
     *   <dd>A language is listed as ancient if it went extinct in ancient times (e.g. more 
     *       than a millennium ago). In the case of ancient languages, a criterion based 
     *       on intelligibility would be ideal, but in the final analysis, identifiers 
     *       are assigned to ancient languages which have a distinct literature and are 
     *       treated distinctly by the scholarly community. In order to qualify for 
     *       inclusion in ISO 639-3, the language must have an attested literature or be 
     *       well-documented as a language known to have been spoken by some particular 
     *       community at some point in history; it may not be a reconstructed language 
     *       inferred from historical-comparative analysis.</dd>
     *   
     *   <dt>Historic languages</dt>
     *   <dd>A language is listed as historic when it is considered to be distinct from 
     *       any modern languages that are descended from it; for instance, Old English 
     *       and Middle English. Here, too, the criterion is that the language have a 
     *       literature that is treated distinctly by the scholarly community.</dd>
     *   
     *   <dt>Constructed languages</dt>
     *   <dd>Identifiers that denote constructed (or artificial) languages. In order to 
     *       qualify for inclusion the language must have a literature and it must be 
     *       designed for the purpose of human communication. Specifically excluded are 
     *       reconstructed languages and computer programming languages.</dd>
     * </dl>
     * 
     * <p>In addition, the <tt>Special situations</tt> type is used for language codes that 
     * are to be used in special situations (uncoded languages, multiple languages, unknown 
     * language and no linguistic content). 
     * 
     * <p>@see {@link http://www.sil.org/iso639-3/types.asp} 
	 * @return The type of this language
	 */
	public Type getType() {
	    return this.type;
	}
	
	/**
	 * Returns the scope of this language. ISO codes are provided for individual languages,
	 * language families (metalanguages), and special situations (e.g., uncoded languages, 
	 * multiple languages, unknown language and no linguistic content).
	 * 
	 * @return the scope of this language
	 */
	public Scope getScope() {
	    return this.scope;
	}
	
	
	   
    //==============================================================================
    // UTILITY METHODS
    //==============================================================================
    
	/** Compares this language to another language or language code for equality. */
	public boolean equals(Object o) {
	    if (o instanceof String) {
	        return this.code.equalsIgnoreCase((String)o);
	    } 

	    // forces ClassCastException if not String or Language 
	    Language lg = (Language)o;
	    return this.code.equalsIgnoreCase(lg.code);
	}
	
	/**
	 * 
	 */
	public int hashCode() {
	    return this.code.toLowerCase().hashCode();
	}
	
	public int compareTo(Language lg) {
	    if (this.equals(lg)) return 0;
	    
	    String thisName = this.getName();
	    String thatName = lg.getName();
	    if (thisName != null && thatName != null) {
	        // sort by readable name (if present)
	        return thisName.compareTo(thatName);
	    } else {
	        // otherwise sort by language code
	        return this.code.compareTo(lg.code);
	    }
	}
}
