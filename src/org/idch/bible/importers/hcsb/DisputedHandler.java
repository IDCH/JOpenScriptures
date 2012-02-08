/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class DisputedHandler extends StructureHandler {
    public static final String NAME = "DisputedText";
    public static final String TAGNAME = "disputed";
    
    public DisputedHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(TAGNAME);
    }
    
    @Override
    public void start(PathElement p) {
        count++;
        // TODO this needs to be migrated to a better, probably.
        this.createStructure("disputed");
    }
    
    @Override
    public void end(PathElement p) { this.closeActiveStructure(); } 
}