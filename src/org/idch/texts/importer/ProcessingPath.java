/**
 * 
 */
package org.idch.texts.importer;

import java.util.Stack;

import org.xml.sax.Attributes;

/**
 * A stack-like structure to keep track of the XML hierarchy for while parsing a text. This 
 * also provides utility methods to keep track of the character content that has been parsed 
 * for this level in the processing path and a method for matching simple, XPath-like 
 * identifiers. 
 *  
 * @author Neal Audenaert
 */
public class ProcessingPath {
    private Stack<PathElement> path = new Stack<PathElement>();
    private final StringBuilder sb = new StringBuilder();
    
    /**
     * Checks to see if the current path matches the xpath like identifier. This uses 
     * an extremely simplified syntax to match against the current path. Specificially, 
     * it matches element names only, separated by the <tt>/</tt> character. If a single name 
     * is provided (e.g., <tt>"p"</tt>), this checks to see if topmost element on the stack
     * has the indicated name. If multiple element names are provided (e.g., 
     * <tt>"/osis/osisWork/header"</tt> or <tt>"header/location/p"</tt> this check to see
     * if the current path starts with (if the supplied path begins with a <tt>/</tt>) or 
     * ends with (if the supplied path does not begin with a <tt>/</tt> character.
     * 
     * @param test The path to test for a match.
     * @return <tt>true</tt> if the current path matches this test query, 
     *      <tt>false</tt> if it does not.
     */
    public boolean matches(String test) {
        if (path.size() == 0)
            return false;
        
        int ix = test.indexOf('/');
        if (ix < 0) {
            return this.path.peek().name.equals(test);
        } else if (ix == 0) {
            return this.toString().startsWith(test);
        } else if (ix > 0) {
            return this.toString().endsWith(test);
        } else {
            assert false : "unreachable code";
            return false;
        }
    }
    
    /**
     * Creates a new PathElement and adds it to the stack. This is called whenever the 
     * processor encounters a start element tag. 
     * 
     * @param nsURI The URI of the new element
     * @param name The name of the new element
     * @param qName The qualified name of the new element. 
     * @param attrs The attributes associated with the new element.
     * 
     * @return The created PathElement.
     */
    public PathElement push(
            String nsURI, String name, String qName, Attributes attrs) {
        PathElement parent = (this.path.size() > 0) ? this.path.peek() : null;
        PathElement e = new PathElement(parent, sb, 
                nsURI, name, qName, attrs);
        
        this.path.push(e);
        
        return e;
    }
    
    /**
     * Pops the topmost <tt>PathElement</tt> from the stack. This is called whenever the  
     * processor encounters an end element tag.
     *  
     * @return The popped <tt>PathElement</tt>
     */
    public PathElement pop() {
        PathElement e = this.path.pop();
        e.end = sb.length();
        
        return e;
    }
    
    /**
     * Accumulator to track the characters as they are read from the source document. The 
     * <tt>Importer.characters</tt> method forwards all invocations to this method prior
     * to taking action on the characters.  
     * 
     * <p><tt>PathElement</tt> objects will have a reference into this list of characters so 
     * that they can retrieve their textual content as distinct from the <tt>Token</tt>
     * stream created by the main importer. 
     *  
     * @param ch The character buffer
     * @param start The start index for the characters to be added.
     * @param length The number of characters to be added.
     */
    public void characters(char[] ch, int start, int length) {
        sb.append(ch, start, length);
    }
    
    /**
     * Peeks at the topmost <tt>PathElement</tt> in the stack without removing it. 
     *  
     * @return The topmost <tt>PathElement</tt> on the stack
     */
    public PathElement peek() {
        return this.path.peek();
    }

    /** 
     * Returns the number of elements in the current path.
     * @return the number of elements in the current path.
     */
    public int size() {
        return this.path.size();
    }
    
    /**
     * Returns a string representation of this path (for example 
     * <tt>"/osis/osisWork/header"</tt>).
     */
    public String toString() {
        if (this.path.size() > 0)
            return this.path.peek().toPath();
        else 
            return "";
    }
}