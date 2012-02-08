package org.idch.texts.importer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * 
 * @param localName
 * @param value
 */


//========================================================================================
// XML PATH MANAGEMENT
//========================================================================================

public class PathElement {
    private final String nsURI;
    final String name;
    private final String qName;
    private final Map<String, String> attribs;
    
    private final PathElement parent;
    private final StringBuilder sb;
    
    private int start;
    int end = -1;
    
    PathElement(PathElement parent, StringBuilder sb,
            String nsURI, String name, String qName, Attributes attrs) {
        this.parent = parent;
        
        this.nsURI = nsURI;
        this.name = name;
        this.qName = qName;
        
        // initialize string builder
        this.sb = sb;
        this.start = sb.length();

        // load attributes
        attribs = new HashMap<String, String>();
        for (int ix = 0; ix < attrs.getLength(); ix++) {
            attribs.put(attrs.getQName(ix), attrs.getValue(ix));
        }
    }
    
    public PathElement getParent() {
        return this.parent;
    }
    
    public String getText() {
        return (end < 0)
            ? sb.substring(start)
            : sb.substring(start, end);
    }
    
    public String getNamespaceURI() {
        return nsURI;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQName() {
        return qName;
    }
    
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.attribs);
    }
    
    public String getAttribute(String name) {
        return this.attribs.get(name);
    }
    
    public boolean hasAttribute(String name) {
        return this.attribs.containsKey(name);
    }
    
    public boolean hasAttribute(String name, String regex) {
        String v = this.attribs.get(name);
        return (v == null) ? false : v.matches(regex);
    }
            
    public String toPath() {
        StringBuilder sb = new StringBuilder();
        return (parent == null) 
            ? sb.append("/").append(name) .toString()
            : sb.append(parent.toPath()).append("/").append(name).toString(); 
    }
    
    public String toString() {
        return this.name;
    }
}