/**
 * 
 */
package org.idch.texts.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.idch.texts.Structure;
import org.idch.texts.StructureRepository;
import org.idch.texts.TextModule;
import org.idch.texts.Work;


/**
 * @author Neal Audenaert
 */
public class Book extends WorkStructureWrapper {
    
    public static final String STRUCTURE_NAME = "book"; 
    public static final String STRUCTURE_PERSPECTIVE = "bcv";       // book, chapter, verse 
    
    public static final String ATTR_TITLE = "title";
    
    /**
     * Initializes a newly created structure with the parameters for a book. Note that 
     * this should be called only once to initialize a new book structure, not as a means 
     * of creating wrapped version of a book. 
     * 
     * @param structure
     * @param osisId
     * @return
     */
    public static Book init(TextModule repo, Structure structure, String osisId) {
        Book book = new Book(repo, structure);
        
        structure.setPerspective(STRUCTURE_PERSPECTIVE);
        structure.setAttribute(ATTR_OSIS_ID, osisId);
        
        return book;
    }
    
    public static boolean isBook(Structure s) {
        return s.getName().equals(STRUCTURE_NAME);
    }
    
    public static Book getBook(TextModule repo, Work w, String osisId) {
        Book book = null;
        SortedSet<Structure> structures = 
                repo.getStructureRepository().find(w, STRUCTURE_NAME, ATTR_OSIS_ID, osisId);
        if (structures.size() >= 1) {
            book = new Book(repo, structures.first());
        }
        
        return book;
    }
    
    public static List<Book> getBooks(TextModule repo, Work w) {
        List<Book> books = new ArrayList<Book>();
        SortedSet<Structure> structures = 
                repo.getStructureRepository().find(w, STRUCTURE_NAME);
        for (Structure s : structures) {
            books.add(new Book(repo, s));
        }
        
        return books;
    }
    
    //======================================================================================
    // MEMBER VARIABLES
    //======================================================================================

    private List<Chapter> chapters = null;
    //======================================================================================
    // CONSTRUCTORS
    //======================================================================================

    public Book(TextModule repo, Structure s) {
        super(repo, s);
    }

    public boolean accepts(Structure s) {
        return isBook(s);
    }
   
    //======================================================================================
    // CUSTOM ACCESSORS AND MUTATORS
    //======================================================================================

    
    public String getTitle() {
        return this.getAttribute(ATTR_TITLE);
    }
    
    public String setTitle(String title) {
        return this.setAttribute(ATTR_TITLE, title);
    }
    
    public List<Chapter> getChapters() {
        return getChapters(false);
    }
    
    public List<Chapter> getChapters(boolean forceUpdate) {
        if (chapters != null && !forceUpdate) 
            return chapters;
        
        Work w = getWork();
        StructureRepository structRepo = repo.getStructureRepository();
        
        chapters = new ArrayList<Chapter>();
        SortedSet<Structure> structures = 
                structRepo.find(w, Chapter.STRUCTURE_NAME, this.getStart(), this.getEnd());
        
        for (Structure s : structures) {
            chapters.add(new Chapter(repo, s));
        }
        
        return chapters;
    }
    
    public int getNumberOfChapters() {
        return getChapters(false).size();
    }
    
    public Chapter getChapter(int num) {
        StructureRepository structRepo = repo.getStructureRepository();
        Work w = getWork();
        
        Chapter chapter = null;
        String osisId = this.getOsisId() + "." + num;
        SortedSet<Structure> structures = 
                structRepo.find(w, Chapter.STRUCTURE_NAME, ATTR_OSIS_ID, osisId);
        if (structures.size() == 1) {
            chapter = new Chapter(repo, structures.first());
        } else {
            // TODO figure out what to do here.
        }
        
        return chapter;
    }
}
