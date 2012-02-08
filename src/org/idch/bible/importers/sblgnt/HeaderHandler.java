/**
 * 
 */
package org.idch.bible.importers.sblgnt;

import org.idch.texts.Work;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.util.Language;


public class HeaderHandler extends StructureHandler {

    public static final String NAME = "BookTitle";
    
    public HeaderHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.toPath().matches("/osis/osisText/header");
    }
    
    public boolean matchesEnd(PathElement p) {
        return ctx.isInHeader();
    }
    
    public void start(PathElement p) {
        ctx.inHeader();
    }
    
    public void end(PathElement p) {
        if (p.getName().equals("header")) {                // exit the header section
            ctx.notInHeader();
            ctx.getWorksRepo().save(ctx.work);
        } else {
            processHeaderField(p);
        }
    }
    
    protected void processHeaderField(PathElement pElement) {
        String localName = pElement.getName();
        String value = pElement.getText();
        Work work = ctx.work;
        
        if (localName.equals("title")) {
            work.setTitle(value);
            
        } else if (localName.equals("creator")) {
            work.setCreator(value);
            
        } else if (localName.equals("date")) {
            work.setPublicationDate(value);
            
        } else if (localName.equals("publisher")) {
            work.setPublisher(value);
            
        } else if (localName.equals("type")) {
            work.setType(value);
            
        } else if (localName.equals("format")) {
            // not used.
            
        } else if (localName.equals("source")) {
            // not used.
            
        } else if (localName.equals("language")) {
            work.setLanguage(Language.lookup(value));
            
        } else if (localName.equals("rights")) {
            work.setCopyright(value);
            
        } else if (localName.equals("scope")) {
            work.setScope(value);
            
        } else if (localName.equals("refSystem")) {
            work.setRefSystem(value);
        }
    }
}