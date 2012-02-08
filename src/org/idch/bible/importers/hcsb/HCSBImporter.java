/**
 * 
 */
package org.idch.bible.importers.hcsb;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.idch.texts.TextModule;
import org.idch.texts.TextModuleInstance;
import org.idch.texts.Work;
import org.idch.texts.importer.Context;
import org.idch.texts.importer.Importer;
import org.idch.texts.importer.PathElement;
import org.idch.texts.importer.StructureHandler;
import org.idch.texts.util.Language;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Neal Audenaert
 */
public class HCSBImporter extends Importer {
    static final Logger LOGGER = Logger.getLogger(HCSBImporter.class);
    
    private long elapsedTime = 0;
    private Work work = null;
    
    private Map<String, Integer> tagCounts = new HashMap<String, Integer>();
    
    public HCSBImporter(String filename, TextModule repo) {
        super(filename, new Context(repo));
        
        this.initWork();
    }
    
    protected void initWork() {
        // TODO make as abstract super class method
        // initialize the work object associated with this importer
        if (work == null) {
            work = this.context.getWorksRepo().create("Bible.Holman.HCSB.2009");
            // TODO add initial metadata values

            work.setCopyright("Copyright Broadman & Holman. All Rights Reserved. Used by Permission.");
            work.setCreator("Edwin Blum, ed.");
            work.setDescription(
                    "100 scholars and English stylists from 17 denominations, prayerfully, " +
                    "translated what is one of the most significant Bible translations " +
                    "available, the Holman Christian Standard Bible (HCSB). Taking into " +
                    "account the significant advancements in scholarship, translation " +
                    "theory, and contemporary English usage, the HCSB will satisfy both " +
                    "those new to the faith and seasoned scholars.");
            work.setLanguage(Language.lookup("eng"));
            work.setPublisher("Broadman & Holman");
            work.setRefSystem("Bible.NRSVA");
            work.setScope("Matt-Rev");
            work.setTitle("Holman Christian Standard Bible");
            
            this.context.getWorksRepo().save(work);
            this.context.work = work;
        }
    }
    
//===================================================================================
// OVERWRITTEN SUPERCLASS METHODS
//===================================================================================

//========================================================================================
// MEMBER VARIABLES
//========================================================================================

    /** The supplied name of the file to import. */
    

    
//========================================================================================
// CONSTRUCTORS
//========================================================================================

        
    public void parseDirectory(File dir) throws SAXException, ParserConfigurationException, IOException {
        for (File file : dir.listFiles()) {
            String fname = file.getName();
            if (!fname.matches("^[456]\\d\\S+_HCSB\\.textonly\\.xml$"))
                continue;
            
            System.out.println("Processing: " + fname);
            parseFile(file);
        }
    }
    
    public void parseFile(File file) throws SAXException, ParserConfigurationException, IOException {
        String url = convertToFileURL(file.getAbsolutePath());
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
     * Starts the import process.
     *  
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        File dir = new File(this.filename);
        if (dir.isDirectory()) parseDirectory(dir);
        else parseFile(dir);
        
        SortedSet<String> keys = new TreeSet<String>();
        keys.addAll(tagCounts.keySet());
        System.out.println("Tags in use in this document: " + keys.size() + " diffent tags.");
        for (String name : keys) {
            System.out.printf("    %25s   %d\n", name, this.tagCounts.get(name));
        }
        
        System.out.println("Handled Tags: ");
        for (StructureHandler h : handlerChain) {
            System.out.printf("    %25s   %d\n", h.getName(), h.getCount());
        }
    }

    //========================================================================================
    // SAX Handlers
    //========================================================================================
        
        
    public void startDocument() throws SAXException {
    }
    
    
    public void startElement(
            String nsURI, String name, String qName, Attributes attrs)
    throws SAXException {
        PathElement el = this.path.push(nsURI, name, qName, attrs);
        
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
            int ct = 0;
            if (el.toPath().equals("/book/blockindent/p")) {
                System.out.println("p: " + el.toPath());
                
                PathElement p = el;
                while ((p = p.getParent()) != null) {
                    if (p.getName().equals("book")) {
                        System.out.println("Book: " + p.getAttribute("id"));
                        break;
                    }
                }
            }
            if (this.tagCounts.containsKey(name)) {
                ct = this.tagCounts.get(name);
            }
            
            this.tagCounts.put(name, ++ct);
        }
    }
    
    
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

    
    public void characters(char[] ch, int start, int length) {
        // There's a lot of noise associated with notes. We're ignoring that for now. . . 
        if (!this.context.check("note"))
            super.characters(ch, start, length);
            
    }
    
    /**
     * Handle any final clean up once the end of the document is encountered.
     */
    public void endDocument() throws SAXException {
        
    }
    
    //===================================================================================
    // MAIN METHOD
    //===================================================================================
    
    public void doImport() throws Exception {
        long start = System.currentTimeMillis();
        try {
            this.addHandler(new NoteHandler());
            this.addHandler(new ParagraphHandler());
            
            BCV bcv = new BCV();
            bcv.attach(this);
            
            this.addHandler(new ParagraphHandler.BlockIndent());
            this.addHandler(new ParagraphHandler.DynamicProse());
            this.addHandler(new ParagraphHandler.OTDynamicProse());
            this.addHandler(new ParagraphHandler.OTQuote());
            this.addHandler(new ParagraphHandler.List());
            this.addHandler(new ParagraphHandler.Item());
            
            this.addHandler(new DisputedHandler());
            this.addHandler(new FormatHandlers.RedTag());
            this.addHandler(new FormatHandlers.Italics());
            this.addHandler(new FormatHandlers.Box());
            this.addHandler(new FormatHandlers.LineBreaks());
            this.addHandler(new Transliteration());
            
            this.addHandler(new PhraseSpaceHandler());
            this.addHandler(new SupHandler());
            
            this.parse();
            
        } catch (Exception ex) {
            LOGGER.error("Failed to import HCSB: " + this.filename, ex);
            throw ex;
        } finally {
            this.elapsedTime = System.currentTimeMillis() - start;
            LOGGER.info("Elapsed Time: " + this.elapsedTime);
        }
        
    }
    
    public long getElapsedTime() {
        return this.elapsedTime;
    }
    
    public Work getWork() {
        return this.work;
    }

    //===================================================================================
    // MAIN METHOD
    //===================================================================================
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        //      String filename = "SBLGNT.osis.xml";
//        String filename = "H:\\IDCH\\Development\\Workspaces\\nttext\\data\\nt\\HCSB\\source\\50-Phil_HCSB.textonly.xml";
        String filename = "H:\\IDCH\\Development\\Workspaces\\nttext\\data\\nt\\HCSB\\source";
        
        HCSBImporter importer;
        try {
            importer = new HCSBImporter(filename, TextModuleInstance.get());
            importer.doImport();

            System.out.println();
            System.out.println();
            System.out.println("Elapsed Time: " + importer.getElapsedTime());
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
      
    }
}