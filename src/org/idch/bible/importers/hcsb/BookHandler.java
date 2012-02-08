/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.bible.importers.hcsb.BCV.BCVHandler;
import org.idch.bible.ref.BookOrder;
import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.structures.Book;

public class BookHandler extends BCVHandler {
    public static final String NAME = "Book";
    public static final String BOOK = "book";
    
    private Book book;
    
    public BookHandler(BCV bcv) {
        super(NAME, bcv);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(BOOK);
    }
    
    @Override
    public void start(PathElement p) {
        count++;
        String id = p.getAttribute("id");
        id = id.replace("BK.", "");
        
        int ix = Integer.parseInt(id);
        String osisId = BookOrder.KJV.getId(ix - 1);        // book order indices are 0-based
        
        ctx.inText();
        Structure s = this.createStructure(Book.STRUCTURE_NAME);
        book = Book.init(ctx.getTextRepo(), s, osisId);
    }
    
    @Override
    public void end(PathElement p) {
        ctx.notInText();
        this.closeActiveStructure();
    }
    
    protected void close(Structure s) {
        this.book = null;
        
        closeDependentHandler(ChapterHandler.NAME);
        closeDependentHandler(SectionHandler.NAME);         // TODO implement this so that it will close all nested.
    }
    
    //===================================================================================
    // INNER CLASSES (SUB-UNIT HANDLERS)
    //===================================================================================
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class LastUpdate extends BCVHandler {
        public static final String NAME = "LastUpdate";
        public static final String TAGNAME = "lastupdate";
        
        public LastUpdate(BCV bcv) {
            super(NAME, bcv);
        }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {  count++; ctx.notInText(); }
        
        @Override
        public void end(PathElement p) {
            ctx.inText();
            
            // set the name of the open book structure
            BookHandler h = (BookHandler)ctx.getHandler(BookHandler.NAME);
            if (h != null) {
                h.book.setAttribute("lastUpdate", p.getText());
            }
        }
    }
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class BookName extends BCVHandler {
        public static final String NAME = "BookName";
        public static final String BOOK_NAME = "bookname";
        
        
        public BookName(BCV bcv) { super(NAME, bcv); }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(BOOK_NAME);
        }
        
        @Override
        public void start(PathElement p) { count++; ctx.notInText(); }
        
        @Override
        public void end(PathElement p) {
            ctx.inText();
            
            // set the name of the open book structure
            BookHandler h = (BookHandler)ctx.getHandler(BookHandler.NAME);
            if ((h != null) && (h.book != null)) {
                h.book.setTitle("Title: " + p.getText());
            }
        }
    }
}