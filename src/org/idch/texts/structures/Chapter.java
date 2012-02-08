/**
 * 
 */
package org.idch.texts.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.idch.texts.Structure;
import org.idch.texts.StructureRepository;
import org.idch.texts.TextModule;
import org.idch.texts.Work;


/**
 * @author Neal Audenaert
 */
public class Chapter extends WorkStructureWrapper {
    private static Pattern OSIS_ID = Pattern.compile("\\S+\\.(\\d+)");
    
    public static final String STRUCTURE_NAME = "chapter"; 
    public static final String STRUCTURE_PERSPECTIVE = "bcv";       // book, chapter, verse 
    
    public static final String ATTR_TITLE = "title";   // ?? is this relevant - probably
    public static final String ATTR_NUMBER = "n";
    
    /**
     * Initializes a newly created structure with the parameters for a chapter. Note that 
     * this should be called only once to initialize a new chapter structure, not as a means 
     * of creating wrapped version of a chapter. 
     * 
     * @param structure
     * @param osisId
     * @return
     */
    public static Chapter init(TextModule repo, Structure structure, String osisId) {
        Chapter chapter = new Chapter(repo, structure);
        
        structure.setPerspective(STRUCTURE_PERSPECTIVE);

        // set the osis id and chapter number
        Matcher m = OSIS_ID.matcher(osisId);
        if (m.matches()) {
            structure.setAttribute(ATTR_NUMBER, m.group(1));
            structure.setAttribute(ATTR_OSIS_ID, osisId);
        } else {
            // FIXME this should have matched
        }
        
        return chapter;
    }
    
    public static Chapter getChapter(TextModule repo, Work w, String osisId) {
        Chapter chapter = null;
        SortedSet<Structure> structures = 
                repo.getStructureRepository().find(w, STRUCTURE_NAME, ATTR_OSIS_ID, osisId);
        if (structures.size() >= 1) {
            chapter = new Chapter(repo, structures.first());
        }
        
        return chapter;
    }
    
    public static boolean isChapter(Structure s) {
        return s.getName().equals(STRUCTURE_NAME);
    }
    
    private List<Verse> verses = null;
    //======================================================================================
    // CONSTRUCTORS
    //======================================================================================

    public Chapter(TextModule repo, Structure s) {
        super(repo, s);
    }

    public boolean accepts(Structure s) {
        return s.getName().equals(STRUCTURE_NAME);
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
    
    public int getChapterNumber() {
        // NOTE we're assuming numeric chapter numbers. That might need to change to be more
        //      general, but this is intended to be sequential. 
        String n = this.getAttribute(ATTR_NUMBER);
        return Integer.parseInt(n);
    }
    
    public void setChapterNumber(int num) {
        this.setAttribute(ATTR_NUMBER, Integer.toString(num));
    }
    
    public List<Verse> getVerses() {
        return getVerses(false);
    }
    
    public List<Verse> getVerses(boolean forceUpdate) {
        if (verses != null && !forceUpdate) 
            return verses;
        
        Work w = getWork();
        StructureRepository structRepo = repo.getStructureRepository();
        
        verses = new ArrayList<Verse>();
        SortedSet<Structure> structures = 
                structRepo.find(w, Verse.STRUCTURE_NAME, this.getStart(), this.getEnd());
        
        for (Structure s : structures) {
            verses.add(new Verse(repo, s));
        }
        
        return verses;
    }
    
    public int getNumberOfVerses() {
        return getVerses(false).size();
    }
    
    public Verse getVerse(int num) {
        StructureRepository structRepo = repo.getStructureRepository();
        
        Verse verse = null;
        String osisId = this.getOsisId() + "." + num;
        SortedSet<Structure> structures = 
                structRepo.find(getWork(), Verse.STRUCTURE_NAME, ATTR_OSIS_ID, osisId);
        if (structures.size() == 1) {
            verse = new Verse(repo, structures.first());
        } else {
            // TODO figure out what to do here.
        }
        
        return verse;
    }
}
