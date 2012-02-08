/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class FrontMatterHandler extends StructureHandler {

    public static final String NAME = "FrontMatter";
    
    public FrontMatterHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals("div") && p.hasAttribute("type", "front");
    }
    
    public void start(PathElement p) {
        ctx.inFront();
        ctx.notInText();
    }
    
    public void end(PathElement p) {
        ctx.notInFront();
    }
}