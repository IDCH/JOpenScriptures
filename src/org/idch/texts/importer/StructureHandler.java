/**
 * 
 */
package org.idch.texts.importer;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.Token;


/**
 * Helper class to facilitate structure creation during SAX based importing of XML documents. 
 * This class abstracts the process of creating individual structures in response to the 
 * start and end tags encountered while parsing an XML file. When the <tt>Importer</tt> 
 * encounters an XML start tag, it invokes the <tt>{@link #matchesStart(PathElement)}</tt> 
 * method to check if the start handler should be called. If this method returns <tt>true<tt>,
 * it invokes the <tt>@{@link #start(PathElement)}</tt> method and returns control to the 
 * main SAX processor without invoking any handlers later on in the handler chain (although, 
 * see the note below for possible changes). This process is repeated for the XML end tags
 * invoking the <tt>{@link #matchesEnd(PathElement)}</tt> and 
 * <tt>@{@link #end(PathElement)}</tt> methods respectively.
 * 
 * <p>A <tt>StructureHandler</tt> may register itself with the import <tt>Context</tt> so that
 * other handlers have access to its state. For example, a <tt>BookHandler</tt> might register
 * itself between its <tt>{@link #start(PathElement)}</tt> and 
 * <tt>{@link #end(PathElement)}</tt> methods so that any chapter handlers encountered can 
 * retrieve add the current book structure as a parent.</tt></p>
 * 
 * <p>
 * TODO Add some good example/tutorial documentation on a Wiki. Probably use book/chapter/verse
 *      hierarchy along with notes and book titles to demonstrate setting the <tt>inText</tt> 
 *      flag.
 * <p> 
 * NOTE The <tt>Importer</tt> is still in alpha stage and the API and overall workflow are 
 *      subject to change. Notably, this may be updated so that all structure handlers are 
 *      tested for all start/end elements. This will allow multiple handlers to service a
 *      single tag. If this option is implemented, we may also implement as 'short circut' 
 *      mechanism that will allow a handler to prevent subsequent handlers from being invoked.
 *      For example, the handler for the header tag might indicate that subsequent handlers 
 *      should not be invoked. This would lead to performance improvements and simplify 
 *      handler implementation since, for example, the handler for body-level paragraphs 
 *      wouldn't need to check if the current path is in the header. 
 * 
 * @author Neal Audenaert
 * @see {@link Context}
 */
public abstract class StructureHandler {
    private final static Logger LOGGER = Logger.getLogger(StructureHandler.class);
    
    protected Context ctx;
    protected String name;
    protected int count = 0;
    
    /**
     * Instantiates a new <tt>StructureHandler</tt> with the specified name.
     * @param name The name of this handler.
     */
    public StructureHandler(String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of this handler. This name should be unique for all structure 
     * handlers loaded into an importer.
     *  
     * @return The name of this handler
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the context object to be used by this <tt>StructureHandler</tt>. The context 
     * object is used to manage contextual information that needs to be shared across
     * multiple <tt>StructureHandler</tt> implementations. 
     *  
     * @param context The import context.
     */
    void setContext(Context context) {
        this.ctx = context;
    }
    
    //========================================================================================
    // PARSING METHODS
    //========================================================================================    
    
    /** 
     * Indicates whether this <tt>StructureHandler</tt> should be invoked for the start tag
     * tag associated with the supplied <tt>PathElement</tt>. 
     *  
     * @param p The <tt>PathElement</tt> to test.
     * @return <tt>true</tt> if this <tt>StructureHandler</tt> should be applied, 
     *      <tt>false</tt> otherwise.
     * @see #start(PathElement)
     * @see #matchesEnd(PathElement) 
     */
    public abstract boolean matchesStart(PathElement p);
    
    /** 
     * Indicates whether this <tt>StructureHandler</tt> should be invoked for the end tag
     * tag associated with the supplied <tt>PathElement</tt>. By default, this returns 
     * the same value as <tt>matchesStart</tt>.
     *  
     * @param p The <tt>PathElement</tt> to test.
     * @return <tt>true</tt> if this <tt>StructureHandler</tt> should be applied, 
     *      <tt>false</tt> otherwise.
     * @see #end(PathElement)
     * @see #matchesStart(PathElement) 
     */
    public boolean matchesEnd(PathElement p) {
        return this.matchesStart(p);
    }

    /**
     * Called to initiate in response to the start tag of a matching path element. This will 
     * typically perform some combination of the following steps.
     * 
     * <ul>
     *   <li>Start the process of creating a structure that will be closed during the 
     *       {@link #end(PathElement)} method. For example, in response to a 
     *       <tt>&lt;div type="book"&gt;</tt> tag, the start method might set-up a new
     *       book structure that will be closed when the corresponding <tt>&lt;/div&gt;</tt>
     *       is encountered. </li>
     *   <li>Fully create a new structure. For structures that mark a location but do not 
     *       span multiple tokens, it may be possible to handle the full process of creating
     *       the structure in the start method.</li>
     *   <li>Handle milestone elements by starting or completing the process of creating a 
     *       structure as appropriate. For example, on encountering a <tt>&lt;verse&gt;</tt>
     *       milestone element, this method might check for the presence of either the 
     *       <tt>sId</tt> or <tt>eId</tt> marker in order to start or complete the creation
     *       of a <tt>verse</tt> structure as appropriate.</li>
     *   <li>Do nothing. Some structures can be handled entirely at their end tag.</li>
     * </ul> 
     *       
     * @param p The path element whose start tag is being processed. 
     * @see #matchesStart(PathElement) 
     * @see #end(PathElement)
     */
    public abstract void start(PathElement p);
    
    /**
     * Called to initiate in response to the end tag of a matching path element. This will 
     * typically perform some combination of the following steps.
     * 
     * TODO update these
     * <ul>
     *   <li>Complete the process of creating a structure that was started by the 
     *       {@link #start(PathElement)} method. For example, in response to a the end tag
     *       corresponding to a book, the end method might be used to update the start and end
     *       tokens of the structure (since these were not known when the start tag was 
     *       encountered) and close any open sub-structures like chapters and verses.</li>
     *   <li>Fully create a new structure. For some structures, there may be no need to take 
     *       action based on the start tag and it may be possible to handle the full process 
     *       of creating the structure in the end method. This scenario is expected to be 
     *       rare.</li>
     *   <li>Do nothing. Some structures (especially milestones) can be handled entirely at 
     *       their start tag.</li>
     * </ul> 
     * 
     * @param p The path element whose end tag is being processed. 
     * @see #matchesEnd(PathElement)
     * @see #start(PathElement)
     */
    public abstract void end(PathElement p);

    //========================================================================================
    // STRUCTURE CREATION METHODS
    //========================================================================================
    
    protected Structure activeStructure = null;
    protected int startAfterIndex = 0;
    
    /**
     *  
     * @param p
     * @return
     */
    public Structure createEmptyStructure(String name) {
        LOGGER.info("creating empty structure: " + name);
        int end = ctx.work.getEnd() - 1;
        if (end < 0) 
            return null;
        
        Token start = ctx.work.get(end);
        return (start != null)
                ? ctx.getStructureRepo().create(ctx.work, name, start, null)
                : null;
    }
    
    /**
     * The typical structure creation work flow involves creating a new structure when a 
     * particular marker (start or end tag) is encountered and closing it when another 
     * marker is encountered. In this work flow, when the structure is first created, the
     * start token for the new structure should be set to be the next non-whitespace token. 
     * The end token for the structure should be set to the last non-whitespace token 
     * encountered before the end marker. 
     *  
     * @param p
     * @return
     */
    public Structure createStructure(String name) {
        LOGGER.info("creating structure: " + name);
        
        if (activeStructure != null) {
            LOGGER.info("autoclosing open structure: " + activeStructure.getName());
            this.closeActiveStructure();
        }
        
        startAfterIndex = ctx.work.getEnd();
        this.activeStructure = ctx.getStructureRepo().create(ctx.work, name);

        ctx.setHandler(this);

        return activeStructure;
    }
    
    /**
     * TODO add comment
     * @return
     */
    public Structure closeActiveStructure() {
        String warning = this.getName() + " - There was a problem closing a structure ";
        
        Structure structure = this.activeStructure;
        if (structure != null) {
            
            // get the start token
            Token start = ctx.work.get(startAfterIndex);        // TODO possibly (although bad) null pointer exception
            if (start == null) {
                System.out.println("Null Start Token: " + startAfterIndex);
            }
            if (start.getType() == Token.Type.WHITESPACE)
                start = start.next(true);
            
            // get the end token
            int lastPos = ctx.work.getEnd() - 1;
            Token end = ctx.work.get(lastPos);
            if (end.getType() == Token.Type.WHITESPACE)
                end = end.prev(true);
            
            // handle errors
            warning += "(" + structure.getName() + "): ";
            if (start == null) {
                warning += "Could not locate start token from  position " + startAfterIndex;
                LOGGER.warn(warning);
                
                // TODO this is bad. We can't recover from this.
            } else if (end == null) {
                warning += "Could not locate end token from  position " + lastPos;
                LOGGER.warn(warning);
                
                // TODO this is bad. I don't think we can recover from this.
            } else if (end.getPosition() < start.getPosition()) {
                end = start;
                warning += "End token came before start token. Setting end = start.";
                LOGGER.warn(warning);
            }
           
            structure.setStartToken(start);
            structure.setEndToken(end);

            this.close(structure);
            ctx.getStructureRepo().save(structure);
            ctx.clearHandler(this.getName());
            LOGGER.info("closed structure: " + structure.getName());
        } else {
            warning += "No active structure.";
            LOGGER.info(warning);
        }
        
        
        this.activeStructure = null;
        return structure;
    }
    
    /** 
     * Utility error handling function checks to ensure that a structure being closed
     * matches the book structure that the handler thinks it is processing. 
     * 
     * TODO needs to throw an exception if a mismatch occurs.
     */
    protected boolean ensureMatchingStructure(Structure local, Structure closing) {
        assert local.getUUID().equals(closing.getUUID());
        if (!local.getUUID().equals(closing.getUUID())) {
            // TODO throw exception.
            LOGGER.error("Mismatched structures. Attempted to close " + closing.getUUID() + 
                    " but subclass had reference to " + local.getUUID());
            return false;
        }
        
        return true;
    }
    
    /** Returns the number of times the start method was invoked (if implemented by subclass). */
    public int getCount() {
        return count;
    }
    
    /**
     * Allows subclasses to perform any additional steps required to close an active
     * structure prior to unregistring this handler. The start and end tokens for 
     * the active structure will have been updated by the time this method is invoked.
     * 
     * @param s
     */
    protected void close(Structure s) {
        // by default, do nothing
    }
    
}