/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

/**
 * The phrasespace tag is used to introduce non-breaking spaces in lines of poetry or other 
 * dynamic text. This tag can be safely ignored as the inserted space should be correctly 
 * interpreted as a whitespace token. Specific display formatting (non-breaking lines) should 
 * be applied at the higher level of abstraction (the line of the dynamic text block). 
 *  
 * @author Neal Audenaert
 */
public class PhraseSpaceHandler extends StructureHandler {

    public static final String NAME = "PhraseSpace";
    
    public static final String TAGNAME = "phrasespace";
    
    
    public PhraseSpaceHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(TAGNAME);
    }
    
    @Override
    public void start(PathElement p) {  count++; /* just ignore these. */  }
    
    @Override
    public void end(PathElement p) { }
}