/**
 * 
 */
package org.idch.texts.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.apache.log4j.Logger;
import org.idch.texts.Work;
import org.idch.texts.importer.Context;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.ProcessingPath;
import org.idch.texts.importer.StructureHandler;
import org.idch.util.StopWatch;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Main driver class for importing XML encoded documents into a tokenized structure. This 
 * implements the SAX <tt>DefaultHandler</tt> API in turn invokes a chain of 
 * <tt>StructureHandler</tt> implementations that are used to in order to govern how the 
 * XML document text is tokenized and to create <tt>Structure</tt>s over the tokenized 
 * text.  
 * 
 * 
 * @author Neal Audenaert
 */
public class Importer extends DefaultHandler {
//=====================================================================================
// SYMBOLIC CONSTANTS
//=====================================================================================
    private static final Logger LOGGER = Logger.getLogger(Importer.class);

    public static final String ATTR_OSIS_ID_WORK = "osisIDWork";
    
//=====================================================================================
// STATIC METHODS
//=====================================================================================
    /**
     * TODO move to org.idch.utils.Filenames
     * @param filename
     * @return
     */
    public static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
//========================================================================================
// MEMBER VARIABLES
//========================================================================================

    /** The supplied name of the file to import. */
    protected String filename;
    
    /** Used to keeps track of the XML document hierarchy during processing. */
    protected ProcessingPath path = new ProcessingPath();

    /**
     * The import context. This is used to share state information between structure 
     * handlers and to control how tokens are generated (tokens are generated only if
     * the 'inText' flag is true.
     */
    protected Context context = null;
    
    /** 
     * The <tt>StructureHandler</tt>s to be used to control how the source document 
     * is tokenized and to create structures as needed over the imported token stream.
     */
    protected List<StructureHandler> handlerChain = new ArrayList<StructureHandler>();
    
//========================================================================================
// CONSTRUCTORS
//========================================================================================

    /**
     * Creates a new Importer for the specified file.
     */
    public Importer(String filename, Context context) {
        this.context = context;
        this.filename = filename;
    }
    
    /**
     * Starts the import process.
     *  
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        String url = convertToFileURL(filename);
        LOGGER.info("Importing XML file: " + url);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse(url);
        
        LOGGER.info("Finished importing: " + url);
    }

    /** 
     * Returns the work being constructed by this importer.
     * 
     * @return
     */
    public Work getWork() {
        return context.work;
    }

    /**
     * Adds a new <tt>StructureHandler</tt> to the end of the handler chain.
     * 
     * @param h The handler to add
     */
    public void addHandler(StructureHandler h) {
        // TODO need to validate that the handler's name is unique
        h.setContext(this.context);
        this.handlerChain.add(h);
    }
    
//========================================================================================
// SAX Handlers
//========================================================================================
    
    /**
     * Handle all initialization once the start of the document is found.
     */
    public void startDocument() throws SAXException {
    }
    
    /**
     * SAX handler for start element tags. This invokes the first handler from the 
     * handler chain whose <tt>{@link StructureHandler#matchesStart(PathElement)}</tt>
     * method returns true.
     */
    public void startElement(
            String nsURI, String name, String qName, Attributes attrs)
    throws SAXException {
        PathElement el = this.path.push(nsURI, name, qName, attrs);
        if (context.isInHeader() || context.isInFront()) {
            // FIXME this seems really ad hoc
            // do nothing (we'll process these at the end tag)
            return;
        }

        // invoke appropriate handler
        boolean handled = false;
        for (StructureHandler h : handlerChain) {
            if (h.matchesStart(el)) {
                h.start(el);
                handled = true;
                break;
            }
        }
        
        
        if (!handled) {
            // TODO this is very specific to OSIS texts. What about TEI, etc. We need to 
            //      delegate this to the normal handler mechanisms and create the new work
            //      when the document is created.
            if (name.equals("osisText") && context.work == null) {
                // create an instance of the work.
                String osisIDWork = attrs.getValue(ATTR_OSIS_ID_WORK);
                context.work = context.getWorksRepo().create(osisIDWork);

                LOGGER.info("Ingesting new work: " + context.work.getWorkId());

            } else {
                LOGGER.info("Unhandled start tag: " + this.path);
            }
        }
    }
    
    /**
     * SAX handler for end element tags. This invokes the first handler from the 
     * handler chain whose <tt>{@link StructureHandler#matchesEnd(PathElement)}</tt>
     * method returns true.
     */
    public void endElement(String namespaceURI, String name, String qName)
    throws SAXException {
        PathElement el = this.path.pop();
        if (!el.getName().equals(name)) {
            LOGGER.error("Badly nested elements: Found '" + name + "' Expected '" + el + "'");
        }
        
        // invoke the appropriate end handler
        boolean handled = false;
        for (StructureHandler h : handlerChain) {
            if (h.matchesEnd(el)) {
                h.end(el);
                handled = true;
                break;
            }
        }
        
        if (!handled) {
            LOGGER.info("Unhandled end tag: " + this.path);
        }
    }
    
    StopWatch timer = new StopWatch("Tokens", 100);

    /** 
     * Tokenizes the source text. This method is called by SAX processor when characters 
     * from the text (as opposed to mark up characters) are encountered. If the import 
     * context's <tt>inText</tt> flag is currently set to true, this creates tokens based on 
     * the encountered text. This also invokes the 
     * <tt>{@link ProcessingPath#characters(char[], int, int)}</tt> to record all characters
     * that are encountered. The <tt>ProcessingPath</tt> and <tt>{@link PathElement}</tt> 
     * objects can be used to access this text regardless of whether the text was tokenized. 
     * This is useful, for example, to handle notes that are included inline in the XML but
     * are not strictly speaking part of the main text being imported in order to create 
     * stand-off markup notes.   
     * 
     * <p>
     * TODO Under the current implementation, markup that splits a single word will result
     *      in multiple word tokens being created. We need to think about whether this is 
     *      the appropriate behavior.
     */
    public void characters(char[] ch, int start, int length) {
        // This allows us to retrieve the text of any arbitrary element or between two 
        // different elements. It collects all text in the source file, including both
        // textual and paratextual elements.
        path.characters(ch, start, length);
        
        if (context.isInText()) {
            String text = new String(ch, start, length);        // FIXME this is inefficient (object creation)
            if (context.work != null)
                context.work.appendAll(text);
        }
    }
    
    /**
     * Handle any final clean up once the end of the document is encountered.
     */
    public void endDocument() throws SAXException {
        context.work.flushTokens();
        
        // TODO notify all handlers of the end of document.
    }
}