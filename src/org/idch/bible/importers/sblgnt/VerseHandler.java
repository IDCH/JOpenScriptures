/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.structures.Verse;


public class VerseHandler extends StructureHandler {
    private static final Logger LOGGER = Logger.getLogger(VerseHandler.class);
    public static final String NAME = "BibleVerse";
    public static final String VERSE = "verse";
    
    private Verse verse = null;
    
    public VerseHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("verse");
    }
    
    public void start(PathElement p) {
        if (p.hasAttribute("osisID")) {         // starting verse milestone marker
            String osisId = p.getAttribute("osisID");
            
            Structure struct = createStructure(Verse.STRUCTURE_NAME);
            verse = Verse.init(ctx.getTextRepo(), struct, osisId);       // initializes the new chapter
            
            LOGGER.info("Creating verse: " + verse.getOsisId());
        } else if (p.hasAttribute("eID")) {     // ending verse milestone marker
            // FIXME This is the code block that is causing the delay.
            String eId = p.getAttribute("eID");
            assert eId.equals(verse.getOsisId()) :
                "Verse Mismatch. Expected " + verse.getOsisId() + " but found " + eId;
            
            this.closeActiveStructure();
        }

        
    }
    
    public void end(PathElement p) {  }
    

    /** 
     * Called by the main handler class to perform final clean up. Marks the importer as 
     * no longer in the text of the document and closes any open chapters or verses.
     */
    public void close(Structure s) {
        if (!ensureMatchingStructure(verse, s)) return;
        
        LOGGER.info("Finished creating verse: " + verse.getOsisId());
        
        this.verse = null;
    }
    
}