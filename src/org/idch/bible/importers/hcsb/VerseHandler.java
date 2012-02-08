/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.bible.importers.hcsb.BCV.BCVHandler;
import org.idch.bible.importers.hcsb.BCV.OsisId;
import org.idch.bible.ref.BookOrder;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.structures.Verse;

public class VerseHandler extends BCVHandler {
    public static final String NAME = "Verse";
    public static final String BOOK = "verse";
    
    private Verse verse = null;
    
    public VerseHandler(BCV bcv) {
        super(NAME, bcv);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(BOOK);
    }
    
    /** 
     * Makes the task of opening a verse structure visible to the rest of the BCV hierarchy. 
     * Notably, since most chapters do not mark the initial verse, the chapter handler needs 
     * to be able to open a new verse structure.
     *  
     * @param osisId The id of the verse strcuture to open.
     */
    void openVerse(String osisId) {
        Structure s = this.createStructure(Verse.STRUCTURE_NAME);
        verse = Verse.init(ctx.getTextRepo(), s, osisId);
    }
    
    @Override
    public void start(PathElement p) {
        count++;
        this.ctx.notInText();           // this marks a verse number (not text)
        OsisId osisId = getOsisId(BookOrder.KJV, p.getAttribute("n"));        // bible.50.1.2
        if (verse != null && verse.getOsisId().equals(osisId.toString())) {
            // Most chapters don't supply an initial verse number, so we open verse 1 when we 
            // open the chapter. However, some chapters do supply an initial verse, so we 
            // need to check to see if the verse we've just encounted is the same as the 
            // verse that we are currently processing. If it is, we'll just ignore it.
            
            return;
        }
        		
        this.closeActiveStructure();    // close the previously opened verse (if present).
        openVerse(osisId.toString());   // open the new verse structure
    }
    
    @Override
    public void end(PathElement p) {
        this.ctx.inText();       // this marks a chapter number
    }
    
    protected void close(Structure s) { /* no structures to close */
        if (printProgress) 
            System.out.println("  " + s.getAttribute("osisId") + ":\t" + ctx.getTextRepo().toString(s));
    }
}