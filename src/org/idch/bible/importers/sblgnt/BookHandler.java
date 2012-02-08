/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.structures.Book;


public class BookHandler extends StructureHandler {
    private static final Logger LOGGER = Logger.getLogger(BookHandler.class);
    
    public static final String NAME     = "BookOfTheBible";
    public static final String ATTR_BOOK     = "book";
    
    /** The book currently being processed. */
    private Book book = null;
    
    /** Default Constructor */
    public BookHandler() {
        super(NAME);
    }
    
    /** Matches div elements with a <tt>type="book"</tt>. */
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("div") && p.hasAttribute("type", ATTR_BOOK);
    }
    
    /** Create a new book structure, marks the importer as being in a text segment. */
    public void start(PathElement p) {
        ctx.inText();
        String osisId = p.getAttribute("osisID");
        
        Structure struct = createStructure(Book.STRUCTURE_NAME);
        book = Book.init(ctx.getTextRepo(), struct, osisId);       // initializes the 
        
        LOGGER.info("Creating book: " + book.getOsisId());
    }
    
    /** Close the book structure on exit. */
    public void end(PathElement p) {
        this.closeActiveStructure();
    }
    
    /** 
     * Called by the main handler class to perform final clean up. Marks the importer as 
     * no longer in the text of the document and closes any open chapters or verses.
     */
    public void close(Structure s) {
        if (!ensureMatchingStructure(book, s)) return;
        
        LOGGER.info("Finished creating book: " + book.getOsisId());
        
        // TODO clean up any open chapters and verses.
        
        ctx.notInText();
        this.book = null;
    }
    
   
    
    
}