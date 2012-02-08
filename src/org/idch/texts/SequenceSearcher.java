/**
 * 
 */
package org.idch.texts;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.text.StringSearch;

public class SequenceSearcher {
    // TODO Evaluate for performance (read documentation)
    //      Consider extending the base StringSearch interface
    //      Document & Test
    
    private final TextModule module;
    private final TokenSequence seq;                  /* The sequence to search */
    private final String seqText;                     /* A string representation of the sequence. */
    private final CharacterIterator seqIterator;      /* Character iterator over the sequence text */ 
    
    private int lastMatch = StringSearch.DONE;
    private String searchPattern = "";
    private RuleBasedCollator collator;
    private StringSearch searcher;
    
    public SequenceSearcher(TextModule module, TokenSequence seq) {
        this.module = module;
        this.seq = seq;
        this.seqText = module.toString(seq);
        this.seqIterator = new StringCharacterIterator(this.seqText);
        
        reset(Collator.PRIMARY, Collator.CANONICAL_DECOMPOSITION);
    }
    
    public void reset(int strength, int decomposition) {
        collator = (RuleBasedCollator)Collator.getInstance();
        collator.setStrength(strength);
        collator.setDecomposition(decomposition);
        
        lastMatch = StringSearch.DONE;
        searcher = null;
    }
    
    /**
     * Indicates whether the last call search call (<tt>find</tt>, <tt>next</tt>, or 
     * <tt>prev</tt>) matched.
     * 
     * @return
     */
    public boolean matched() {
        return lastMatch != StringSearch.DONE;
    }
    
    public int find(String pattern) {
        return find(pattern, 0);
    }
    
    public int find(String pattern, int startAt) {
        this.searchPattern = pattern;
        searcher = new StringSearch(searchPattern, seqIterator, collator);
        searcher.setIndex(startAt);

        lastMatch = searcher.first();
        return lastMatch;
    }
    
    public int next() {
        lastMatch = searcher.next();
        return lastMatch;
    }
    
    public int previous() {
        lastMatch = searcher.previous();
        return lastMatch;
    }
    
    public Structure getStructure(String name) {
        Structure s = null;
        if (lastMatch != StringSearch.DONE) {
            Token start = module.getTokenAt(seq, lastMatch);
            Token end = module.getTokenAt(seq, lastMatch + searchPattern.length() - 1);
            s = new Structure(seq.getWorkUUID(), name, start, end);
        }
        
        return s;
    }
}