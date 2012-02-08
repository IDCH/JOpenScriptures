/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class Transliteration extends StructureHandler {
    public static final String NAME = "Transliteration";
    
    public Transliteration() { super(NAME); }
    
    public boolean matchesStart(PathElement p) {
        String name = p.getName();
        return name.equals("tarc") || name.equals("theb");
    }
    
    @Override
    public void start(PathElement p) {
        count++;
        // Translated/Transliterated words
        // tarc Aramic, theb Hebrew
    }
    
    @Override
    public void end(PathElement p) { 
        
    }
    
    
}