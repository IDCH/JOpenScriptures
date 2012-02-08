/**
 * 
 */
package org.idch.texts;

import org.idch.texts.WorkId;
import org.idch.texts.WorkId.Type;
import org.idch.texts.util.Language;

import junit.framework.TestCase;

/**
 * @author Neal Audenaert
 */
public class WorkIdTests extends TestCase {
    
    public void testParsing() {
        WorkId id = new WorkId("Bible.grc.SBLGNT.2010");
        assertEquals(id.getType(), Type.BIBLE);
        assertEquals(id.getLanguage(), Language.lookup("grc"));
        assertNull(id.getPublisher());
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getName(), "SBLGNT");
        
        id = new WorkId("Bible.grc.Logos.SBLGNT.2010");
        assertEquals(id.getType(), Type.BIBLE);
        assertEquals(id.getLanguage(), Language.lookup("grc"));
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getPublisher(), "Logos");
        assertEquals(id.getName(), "SBLGNT");
        
        id = new WorkId("SBLGNT.2010");
        assertNull(id.getType());
        assertNull(id.getLanguage());
        assertNull(id.getPublisher());
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getName(), "SBLGNT");
        
        id = new WorkId("Bible.SBLGNT.2010");
        assertEquals(id.getType(), Type.BIBLE);
        assertNull(id.getLanguage());
        assertNull(id.getPublisher());
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getName(), "SBLGNT");
        
        id = new WorkId("grc.SBLGNT.2010");
        assertNull(id.getType());
        assertNull(id.getPublisher());
        assertEquals(id.getLanguage(), Language.lookup("grc"));
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getName(), "SBLGNT");
    }
    
    public void testExplictCreation() {
        WorkId id = new WorkId("SBLGNT", "2010");
        assertNull(id.getType());
        assertNull(id.getLanguage());
        assertNull(id.getPublisher());
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getName(), "SBLGNT");
        
        id = new WorkId(Type.BIBLE, Language.lookup("grc"), "Logos", "SBLGNT", "2010");
        assertEquals(id.getType(), Type.BIBLE);
        assertEquals(id.getLanguage(), Language.lookup("grc"));
        assertEquals(id.getPublicationDate(), "2010");
        assertEquals(id.getPublisher(), "Logos");
        assertEquals(id.getName(), "SBLGNT");
    }
    
    public void testStringification() {
        WorkId id = new WorkId(Type.BIBLE, Language.lookup("grc"), "Logos", "SBLGNT", "2010");
        assertEquals(id.toString(), "Bible.grc.Logos.SBLGNT.2010");
        
        id = new WorkId("SBLGNT", "2010");
        assertEquals(id.toString(), "SBLGNT.2010");
        
        id = new WorkId("Bible.grc.SBLGNT.2010");
        assertEquals(id.toString(), "Bible.grc.SBLGNT.2010");
        
        id = new WorkId("Bible.grc.Logos.SBLGNT.2010");
        assertEquals(id.toString(), "Bible.grc.Logos.SBLGNT.2010");
        
        id = new WorkId("SBLGNT.2010");
        assertEquals(id.toString(), "SBLGNT.2010");
        
        id = new WorkId("Bible.SBLGNT.2010");
        assertEquals(id.toString(), "Bible.SBLGNT.2010");
        
        id = new WorkId("grc.SBLGNT.2010");
        assertEquals(id.toString(), "grc.SBLGNT.2010");
        
    }
}
