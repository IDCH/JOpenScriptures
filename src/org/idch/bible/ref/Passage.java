/**
 * 
 */
package org.idch.bible.ref;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.idch.texts.Structure;
import org.idch.texts.TextModule;
import org.idch.texts.Work;
import org.idch.texts.WorkRepository;
import org.idch.texts.structures.Verse;

/**
 * Represents a selection of Bible verses. This may be a single verse, a . 
 * @author Neal Audenaert
 */
public abstract class Passage implements Comparable<Passage> {
    private static final Logger LOGGER = Logger.getLogger(Passage.class);
	
    //=======================================================================================
    // STATIC MEMBERS
    //=======================================================================================
    
	private static boolean recheckMissingWorks = false;
	   
    private static Set<String> missingWorks = new HashSet<String>();
    private static Set<String> multipleWorks = new HashSet<String>();
    
    //=======================================================================================
    // STATIC METHODS
    //=======================================================================================
    
    /**
     * 
     * @param module
     * @param passage
     * @param workAbbr
     * 
     * @return
     */
    public static Structure resolve(TextModule module, Passage passage, String workAbbr) {
        // TODO need a more generic form of this so that it can work with non-tokenized texts
        // TODO Currently this only works (really) for verse ranges. May need to support 
        //      non-contiguous passages.
        if (!recheckMissingWorks && missingWorks.contains(workAbbr))
            return null;
        
        WorkRepository workRepo = module.getWorkRepository();
        
        List<Work> works = workRepo.findByAbbr(workAbbr);
        if (works.size() == 0) {
            LOGGER.warn("Cannot find references for work (" + workAbbr + "): No such work.");
            missingWorks.add(workAbbr);
            return null;
        } else if (works.size() > 1) {
            if (multipleWorks.add(workAbbr)) {
                LOGGER.warn("Found multiple works found (" + workAbbr + "). Using first.");
            }
        }
        
        Work work = workRepo.findByAbbr(workAbbr).get(0);
        VerseRef firstRef = passage.getFirst();
        VerseRef lastRef = passage.getLast();
        
        Structure structure = null;
        if (!firstRef.equals(lastRef)) {
            Verse startVs = Verse.getVerse(module, work, firstRef.toOsisId());
            Verse endVs = Verse.getVerse(module, work, lastRef.toOsisId());
            structure = new Structure(work.getUUID(), "passage", 
                    startVs.getStartToken(), endVs.getEndToken());
        } else {
            structure = Verse.getVerse(module, work, firstRef.toOsisId());
        }
        
        return structure;
    }
    
    /** 
     * Resets the set of missing works. After a call to this method, works for which a 
     * previous lookup has failed will be retried.
     */
    public static void resetMissingWorks() {
        missingWorks.clear();
        multipleWorks.clear();
    }
    
    //=======================================================================================
    // MEMBER VARIABLES
    //=======================================================================================
    
	protected BookOrder order = BookOrder.KJV;
	
	//=======================================================================================
    // CONSTRUCTOR
    //=======================================================================================
    
	protected Passage(BookOrder order) {
		this.order = order;
	}
	
	//=======================================================================================
    // METHODS
    //=======================================================================================
    
	public abstract VerseRef getFirst();
	
	public abstract VerseRef getLast();
	
	public abstract String format();
	
	public abstract String toOsisId();
	
	/**
	 * Indicates whether this passage is contained within the supplied passage.
	 * 
	 * @param ref The candidate super-passage to text.
	 * @return <tt>true</tt> if this passage starts after or at the same verse as the 
	 *         candidate parent passage and ends before at at the same ending verse as
	 *         the candidate.
	 */
	public boolean isSubPassageOf(Passage ref) {
	    VerseRef firstSuper = ref.getFirst();
	    VerseRef firstSub = this.getFirst();
	    
	    VerseRef lastSuper = ref.getLast();
	    VerseRef lastSub = this.getLast();

	    // TODO TEST ME
	    return (firstSub.compareTo(firstSuper) >= 0) && (lastSub.compareTo(lastSuper) <= 0);
	}
	

	public BookOrder getBookOrder() {
		return this.order;
	}
	
	private int nullSafeCompareTo(VerseRef a, VerseRef b) {
	    if ((a != null) && (b != null)) {
            return a.compareTo(b);
        } else {
            return (a == b) ? 0 
                    : (a == null) ? -1 : 1;
        }
	}
	
	public int compareTo(Passage passage) {
		int result = nullSafeCompareTo(this.getFirst(), passage.getFirst());
		return (result != 0) ? result 
                : nullSafeCompareTo(this.getLast(), passage.getLast());
	}
	
	public boolean equals(Object o) {
	    return this.compareTo((Passage)o) == 0;
	}
	
	public abstract String toString();
	
}
