/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.bible.importers.hcsb.BCV.BCVHandler;
import org.idch.bible.importers.hcsb.BCV.OsisId;
import org.idch.bible.ref.BookOrder;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.structures.Chapter;

public class ChapterHandler extends BCVHandler {
    public static final String NAME = "Chapter";
    public static final String BOOK = "chap";
    
    Chapter chapter = null;
    
    public ChapterHandler(BCV bcv) {
        super(NAME, bcv);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("chap") ||
               p.getName().equals("hiddenverse");       // marks a book with only one chapter
    }
    
    void openChapter(String osisId, String type) {
        Structure s = this.createStructure(Chapter.STRUCTURE_NAME);
        chapter = Chapter.init(ctx.getTextRepo(), s, osisId);
        chapter.setAttribute("hcsbRenderedType", type);
        
        if (printProgress) 
            System.out.println("Chapter: " + chapter.getOsisId());
        
        // open verse - there usually is not a marker for the first verse in a chapter
        osisId += "." + 1;
        bcv.verseHandler.openVerse(osisId);
    }
    
    @Override
    public void start(PathElement p) {
        // <chap type="noind" n="bible.50.1.0">1</chap>
        count++;
       
        this.ctx.notInText();           // this marks a chapter number (not text)
        this.closeActiveStructure();    // close the previously opened chapter (if present).

        // open the chapter
        OsisId osisId = getOsisId(BookOrder.KJV, p.getAttribute("n"));        // bible.50.1.0
        String indentType = (p.getName().equals("chap")) ? p.getAttribute("type") : ""; 
    
        openChapter(osisId.toString(), indentType);
    }
    
    @Override
    public void end(PathElement p) {
        this.ctx.inText();       // this marks a chapter number
    }
    
    protected void close(Structure s) {
        closeDependentHandler(VerseHandler.NAME);
        this.chapter = null;
//        System.out.println("    Closing " + chapter.getOsisId());
    }
    
    
   
}