/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.structures.Chapter;


public class ChapterHandler extends StructureHandler {
    private static final Logger LOGGER = Logger.getLogger(ChapterHandler.class);
    
    public static final String NAME = "BibleChapter";
    public static final String CHAPTER = "chapter";

    Chapter chapter = null;
    
    public ChapterHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(CHAPTER);
    }
    
    public void start(PathElement p) {
        if (p.hasAttribute("osisID")) {         // starting chapter milestone marker
            String osisId = p.getAttribute("osisID");
            
            Structure struct = createStructure(Chapter.STRUCTURE_NAME);
            chapter = Chapter.init(ctx.getTextRepo(), struct, osisId);       // initializes the new chapter
            
            LOGGER.info("Creating chapter: " + chapter.getOsisId());
            
        } else if (p.hasAttribute("eID")) {     // ending verse milestone marker
            String eId = p.getAttribute("eID");
            assert eId.equals(chapter.getOsisId()) :
                "Chapter Mismatch. Expected " + chapter.getOsisId() + " but found " + eId;
            
            this.closeActiveStructure();
        }
    }
    
    public void end(PathElement p) {
    }
    
    /** 
     * Called by the main handler class to perform final clean up. Marks the importer as 
     * no longer in the text of the document and closes any open chapters or verses.
     */
    public void close(Structure s) {
        if (!ensureMatchingStructure(chapter, s)) return;
        
        LOGGER.info("Finished creating chapter: " + chapter.getOsisId());
        // TODO clean up any open verses.
        
        this.chapter = null;
    }
}