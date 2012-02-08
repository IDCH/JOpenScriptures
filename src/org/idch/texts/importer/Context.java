/**
 * 
 */
package org.idch.texts.importer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.idch.texts.StructureRepository;
import org.idch.texts.TextModule;
import org.idch.texts.Work;
import org.idch.texts.WorkRepository;


/**
 * Provides a shared context for use by both the main <tt>Importer</tt> and individual 
 * <tt>StructureHandler</tt> implementations. This is designed as a lightweight class 
 * that provides direct access to its member variables. Use with care.
 * 
 * @author Neal Audenaert
 */
public class Context {
    private static final Logger LOGGER = Logger.getLogger(Context.class);
    
    /** The <tt>Work</tt> that is being imported. */
    public Work work = null;
    
    /** Indicates that the parser is in the header section of the document. */
    private boolean inHeader = false;
    
    /** Indicates that the parser is in the front matter section of the document. */
    private boolean inFront = false;
    
    /** Indicates that the parser is in the text of the document. While this is 
     *  set to true, the importer will add tokens from the character data that 
     *  it encounters. */
    private boolean inText = false;
    
    private TextModule repo = null;
    
    //=====================================================================================
    // PRIVATE STATE VARIABLES
    //=====================================================================================
    // TODO we can probably simplify a lot of this by just tracking active/closed handlers.
    //      for example, the BookHandler could maintain information about the current 
    //      chapter number, we are 'inHeader' if there is a header handler, etc.
    
    /** Boolean state flags. */
    private Map<String, Boolean> flags = new HashMap<String, Boolean>();
    
    /** State property values. */ 
    private Map<String, String> props = new HashMap<String, String>();
    
    /** Structures that are currently being processed. */
    private Map<String, StructureHandler> handler = new HashMap<String, StructureHandler>();
    
    //=====================================================================================
    // CONSTRUCTORS
    //=====================================================================================
    
    /**
     * Instantiates a new Context.  
     * @param repo The structure factory to use to create and retrieve structure 
     *      objects. 
     *      
     *      TODO we need to come up with a more robust set of tools to define the  
     *           how objects are created and retrieved.
     *      
     */
    public Context(TextModule repo) {
        this.repo = repo;
    }
    
    //=====================================================================================
    // ACCESSORS & MUTATORS
    //=====================================================================================
    
    /**
     * Updates the context to indicate that the parser is in a text segment (this 
     * will enable tokenization until <tt>{@link #notInText()}</tt> is called).
     */
    public void inText() { this.inText = true;  }
    
    /**
     * Updates the context to indicate that the parser is not in a text segment (this 
     * will prevent tokenization until <tt>{@link #inText()}</tt> is called).
     */
    public void notInText() { this.inText = false; }
    
    public boolean isInText() { return this.inText; }
    

    public void inHeader() { this.inHeader= true;  }
   
    public void notInHeader() { this.inHeader = false; }
   
    public boolean isInHeader() { return this.inHeader; }
    
    public void inFront() { this.inFront= true;  }
    
    public void notInFront() { this.inFront = false; }
   
    public boolean isInFront() { return this.inFront; }
           
    /**
     * Sets a numeric property of the context.
     *  
     * @param key The key for the property to set.
     * @param value The value to be set.
     * 
     * @return The previous value for this property.
     */
    public String set(String key, double value) {
        LOGGER.debug("Set numeric property: '" + key + "'=" + value + "");
        return this.props.put(key, Double.toString(value));
    }

    /**
     * Sets a string-valued property of this context.
     * 
     * @param key The key for the property to set.
     * @param value The value to be set
     * 
     * @return The previous value for this property.
     */
    public String set(String key, String value) {
        LOGGER.debug("Set property: '" + key + "'='" + value + "'");
        return this.props.put(key, value);
    }
    
    /**
     * Returns the value of a string-valued property.
     * @param key The property whose value should be returned.
     * 
     * @return 
     */
    public String get(String key) {
        String v = this.props.get(key);
        
        LOGGER.debug("Retreived property: '" + key + "'='" + v + "'");
        return v;
    }
    
    /**
     * Returns the value of a numeric property.  
     * 
     * @param key The property whose value should be returned
     * @return The set value or <tt>Double.NaN</tt> if the set value is null.
     */
    public double getn(String key) {
        LOGGER.debug("Retreiving property '" + key + "'");
        
        double d = Double.NaN;
        String value = this.props.get(key);
        if (value != null) {
            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Could not retrieve numeric property (" + key + "). The " +
                		"stored value '" + value + "' is not a number."); 
                d = Double.NaN;
            }
        }
        
        LOGGER.debug("Retreived numeric property: '" + key + "'=" + d);
        return d;
    }
    
    public TextModule getTextRepo() {
        return this.repo;
    }
    

    public WorkRepository getWorksRepo() {
        return this.repo.getWorkRepository();
    }
    
    public StructureRepository getStructureRepo() {
        return this.repo.getStructureRepository();
    }
    
    //=====================================================================================
    // HANDLER ACCESS METHOS
    //=====================================================================================
    
    /**
     * Returns the named structure currently being processed by an importer. Note that this 
     * does not currently allow for multiple structures with the same name to be processed 
     * concurrently, and expects that structure handlers are well behaved in cleaning up 
     * structures after they have been created. 
     *  
     * @param name
     * @return
     */
    public StructureHandler getHandler(String name) {
        StructureHandler h = this.handler.get(name);
        
        LOGGER.debug("Retreived handler (" + name + ")" + 
                ((h == null) ? ": handler not active." : ""));
        return h;
    }
    
    /**
     * Sets the current structure being processed by an importer. This is used by content
     * handlers to make the structures that they are responsible for creating accessible 
     * to other handlers. Note, this assumes that structures with the same name do not 
     * overlap.
     * 
     * @param struct The structure to set.
     * @return The previous structure with this name, or <tt>null</tt> if there was no
     *      previous structure.
     */
    public StructureHandler setHandler(StructureHandler struct) {
        LOGGER.debug("Setting handler (" + struct.getName() + ")"); 
        return this.handler.put(struct.getName(), struct);
    }
    
    /**
     * Clears a structure handler currently being processed by an importer. This typically 
     * occurs when a structure is closed by the importer (e.g. when a verse is closed at an 
     * verse end tag or else by closing a chapter. 
     * 
     * @param name The name of the structure to be cleared.
     * @return The cleared structure or <tt>null</tt> if there was no current structure for 
     *      this name.
     */
    public StructureHandler clearHandler(String name) {
        LOGGER.debug("Clearing handler (" + name + ")");
        return this.handler.remove(name);
    }
    
    /**
     * Sets a boolean state flag, for example, to indicate that the parser is in the 
     * document header.
     * 
     * @param state The flag to set.
     * @param value The value to set.
     */
    public void flag(String state, Boolean value) {
        if (value)
            LOGGER.debug("Setting flag (" + state + ")");
        else 
            LOGGER.debug("Clearing flag (" + state + ")");
        flags.put(state, value);
    }
    
    /** 
     * Checks the value of a state flag.
     * @param state The state flag to check.
     * @return the value of this flag.
     */
    public boolean check(String state) {
        boolean flag = flags.containsKey(state) 
               ? flags.get(state) 
               : false;
        
        LOGGER.debug("Checking flag: '" + state + "'=" + flag);   
        return flag;
    }
}