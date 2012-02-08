/**
 * 
 */
package org.idch.bible.importers.hcsb;

import org.idch.texts.Structure;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.structures.Speaker;

public class FormatHandlers {
    
    /**
     * 
     * @author Neal Audenaert
     */
    public static class RedTag extends StructureHandler {
        public static final String NAME = "RedLetters";
        public static final String RED = "red";

        public RedTag() { super(NAME); }

        public boolean matchesStart(PathElement p) {
            return p.getName().equals(RED);
        }

        @Override
        public void start(PathElement p) {
            count++;
            Structure s = this.createStructure(Speaker.STRUCTURE_NAME);
            Speaker.init(ctx.getTextRepo(), s, "Jesus");
        }

        @Override
        public void end(PathElement p) { this.closeActiveStructure(); }
    }
    
    public static class Italics extends StructureHandler {
        public static final String NAME = "Italics";
        public static final String TAGNAME = "i";

        public Italics() { super(NAME); }

        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }

        @Override
        public void start(PathElement p) {
            count++;
            Structure s = this.createStructure("rend");
            s.setAttribute("type", "italics");
        }

        @Override
        public void end(PathElement p) { this.closeActiveStructure(); }
    }
    
    public static class Box extends StructureHandler {
        public static final String NAME = "Box";
        public static final String TAGNAME = "box";

        public Box() { super(NAME); }

        public boolean matchesStart(PathElement p) {
            return p.getName().equals(TAGNAME);
        }

        @Override
        public void start(PathElement p) {
            count++;
            // I don't really understand what these are, but they seem to be use to 
            // record reported inscriptions
        }

        @Override
        public void end(PathElement p) {  }
    }
    
    public static class LineBreaks extends StructureHandler {
        public static final String NAME = "LineBreak";

        public LineBreaks() { super(NAME); }

        public boolean matchesStart(PathElement p) {
            return p.toPath().endsWith("box/br");
        }

        @Override
        public void start(PathElement p) {
            count++;
            // These are line breaks in boxes, we should probably mark these off as lines 
        }

        @Override
        public void end(PathElement p) {  }
    }
    
    
}