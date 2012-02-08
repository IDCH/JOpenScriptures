/**
 * 
 */
package org.idch.texts.structures;

import org.idch.texts.Structure;
import org.idch.texts.TextModule;

/**
 * @author Neal Audenaert
 */
public class Speaker extends WorkStructureWrapper {
    public static final String STRUCTURE_NAME = "speaker"; 
    public static final String ATTR_SPKR = "who";
    
    /**
     * Initializes a newly created structure with the parameters for a book. Note that 
     * this should be called only once to initialize a new book structure, not as a means 
     * of creating wrapped version of a book. 
     * 
     * @param structure
     * @param osisId
     * @return
     */
    public static Speaker init(TextModule repo, Structure structure, String spkr) {
        Speaker speakder = new Speaker(repo, structure);
        speakder.setSpeaker(spkr);
        
        return speakder;
    }
    
    public static boolean isRedLetters(Structure s) {
        return s.getName().equals(STRUCTURE_NAME);
    }
    
    /**
     * @param repo
     * @param s
     */
    public Speaker(TextModule repo, Structure s) {
        super(repo, s);
    }

    /* (non-Javadoc)
     * @see org.idch.texts.StructureWrapper#accepts(org.idch.texts.Structure)
     */
    @Override
    public boolean accepts(Structure structure) {
        return isRedLetters(structure);
    }
    
    public String getSpeaker() {
        return this.getAttribute(ATTR_SPKR);
    }
    
    public void setSpeaker(String speaker) {
        this.setAttribute(ATTR_SPKR, speaker);
    }

}
