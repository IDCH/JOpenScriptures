/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;

public class ParagraphHandler extends StructureHandler {
    public static final String NAME = "Paragraph";
    public static final String TAGNAME = "p";
    
    public ParagraphHandler() {
        super(NAME);
    }
    
    public boolean matchesStart(PathElement p) {
        return p.getName().equals(TAGNAME);
    }
    
    @Override
    public void start(PathElement p) {
        count++;
//        Normal Paragraph        
//        /book/p
//        /book/blockindent/p
        
//        Line of dynamic text        
//        /book/otdynprose/p
//        /book/dynprose/p
        
    }
    
    @Override
    public void end(PathElement p) {
    }

    
    //===================================================================================
    // INNER CLASSES (GENERAL BLOCK ORIENTED DIVISIONS)
    //===================================================================================

    /**
     * 
     * @author Neal Audenaert
     */
    public static class BlockIndent extends StructureHandler {
        public static final String NAME = "BlockIndent";
        public static final String TAGNAME = "blockindent";
        
        public BlockIndent() {
            super(NAME);
        }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
        }
        
        @Override
        public void end(PathElement p) {
        }

    }
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class DynamicProse extends StructureHandler {
        public static final String NAME = "DynamicProse";
        public static final String TAGNAME = "dynprose";
        
        public DynamicProse() {
            super(NAME);
        }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
            // this should probably be a div element, with an appropriate type
        }
        
        @Override
        public void end(PathElement p) {
        }
    }
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class OTDynamicProse extends StructureHandler {
        public static final String NAME = "OTDynamicProse";
        public static final String TAGNAME = "otdynprose";
        
        public OTDynamicProse() {
            super(NAME);
        }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
            // this should probably be a div element, with an appropriate type
        }
        
        @Override
        public void end(PathElement p) {
        }
    }
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class OTQuote extends StructureHandler {
        public static final String NAME = "OTQuote";
        public static final String TAGNAME = "otquote";
        
        public OTQuote() {
            super(NAME);
        }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
            // this should probably be a div element, with an appropriate type
        }
        
        @Override
        public void end(PathElement p) {
        }
    }
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class List extends StructureHandler {
        public static final String NAME = "List";
        public static final String TAGNAME = "list";
        
        public List() { super(NAME); }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
            // create list structure
        }
        
        @Override
        public void end(PathElement p) {
            
        }
    }
        
    /**
     * 
     * @author Neal Audenaert
     */
    public static class Item extends StructureHandler {
        public static final String NAME = "Item";
        public static final String TAGNAME = "item";
        
        public Item() { super(NAME); }
        
        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }
        
        @Override
        public void start(PathElement p) {
            count++;
            // create item structure
        }
        
        @Override
        public void end(PathElement p) {
        }
        
    }
}