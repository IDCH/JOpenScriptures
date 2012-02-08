/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.bible.importers.hcsb.BCV.BCVHandler;
import org.idch.texts.importer.PathElement;

public class SectionHandler extends BCVHandler {
    public static final String NAME = "SectionHeader";
    
    public SectionHandler(BCV bcv) {
        super(NAME, bcv);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("head1") || p.getName().equals("supertitle"); 
        // TODO need to map super title as a different handler or else add pop/push 
        //      support to active handlers
    }
    
    @Override
    public void start(PathElement p) {
        // <chap type="noind" n="bible.50.1.0">1</chap>
        count++;
        
        
        this.ctx.notInText();       // this marks a chapter number
        // TODO close any open sections
        
        // TODO open section
    }
    
    @Override
    public void end(PathElement p) {
        this.ctx.inText();       // this marks a chapter number
    }
}