/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class BookTitleHandler extends StructureHandler {
        public static final String NAME = "BookTitle";
        
        public BookTitleHandler() {
            super(NAME);
        }
        
        public boolean matchesStart(PathElement p) {
            StructureHandler bookHandler = ctx.getHandler(BookHandler.NAME);
            
            // This needs to be a bit smarter about what title it is encountering - might 
            //      be a book but it might also be a chapter or section title.
            return (bookHandler != null) && p.getName().equals("title");
        }
        
        public void start(PathElement p) {
            ctx.notInText();
        }
        
        public void end(PathElement p) {
            ctx.inText();
            
            // TODO need to set the title.
            // TODO need to handle Mark weirdness
//            Other Endings of Mark
//            Intermediate Ending
//            Long Ending
        }
    }