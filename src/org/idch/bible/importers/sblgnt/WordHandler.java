/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class WordHandler extends StructureHandler {

    public static final String NAME = "BookTitle";
    
    public static final String WORD = "w";
    
    
    public WordHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("w");
    }
    
    @Override
    public void start(PathElement p) {
        ctx.flag(WORD, true);
    }
    
    @Override
    public void end(PathElement p) {
        ctx.flag(WORD, false);
    }
}