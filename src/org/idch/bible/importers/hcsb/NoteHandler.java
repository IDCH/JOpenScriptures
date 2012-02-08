/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class NoteHandler extends StructureHandler {

    public static final String NAME = "Note";
    
    public static final String NOTE = "note";
    
    protected boolean inText = false;
    public NoteHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(NOTE) || ctx.check(NAME);
    }
    
    @Override
    public void start(PathElement p) {
        count++;
        if (p.getName().equals(NOTE)) {
            ctx.flag(NAME, true);
            inText = ctx.isInText();            // TODO need to test to see if notes ever appear outside of the text
            ctx.notInText();
            
//            String n = p.getAttribute("n");       
//            String type = p.getAttribute("type");
            
            
        } else {
            // internal not fields
        }
    }
    
    @Override
    public void end(PathElement p) {
        if (p.getName().equals(NOTE)) {
            ctx.flag(NAME, false);
            if (inText) ctx.inText();
        }
    }
    
    
}