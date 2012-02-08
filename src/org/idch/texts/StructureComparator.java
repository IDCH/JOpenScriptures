/**
 * 
 * 
 */
package org.idch.texts;

import java.util.Comparator;

/**
 * Tests to determine the sequential relationship between two <tt>Structures</tt>. 
 * 
 * TODO Document me
 * TODO test me
 * TODO validate against LMNL and Core Range Algebra
 * 
 * @author Neal Audenaert
 * @see Structure
 */
public class StructureComparator implements Comparator<Structure> {

    // TODO use this to test for various types of nesting and overlapping relationships as 
    //      well: SEPARATE, NESTED, OVERLAPPING
    
    /**
     * 
     * <p>
     * Note: this comparator may impose orderings that are inconsistent with equals.
     * Specifically, two different structures are equal, strictly speaking, if their 
     * UUIDs are equal. For practical purposes, however, this treats structures as
     * equivalent if they have the same name and mark the same sequence of tokens
     * in the same work. In general, these refer to the "same" structure although they may 
     * come from different structure repositories and/or have different attributes. 
     * 
     * @param a The first structure to compare.
     * @param b The second structure to compare.
     * @return -1, 0, 1 if <tt>a</tt> is less than, equal to, or greater than <tt>b</tt>.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Structure a, Structure b) {

        // NESTED STRUCTURES - Enclosing structure is before enclosed struture
        //                AAAAAAAAAAAA    AAAAAAAAAAA            [1,4), [5, 8)
        //                    BBBB        BBBBBBBB               [2,3), [5, 7)
        // SEQUENCE   0   1   2   3   4   5   6   7   8   9  10  11  12  13  14
        
        // OVERLAPPING OR SEPARATE STRUCTURES - first structure comes first
        //                AAAAAAAAAAAA        
        //                        BBBBBBBB    
        // SEQUENCE   0   1   2   3   4   5   6   7   8   9  10  11  12  13  14
        
        
        // Make sure these structures are from the same text. If not, we can't 
        // compare them.
        if (!a.getWorkUUID().equals(b.getWorkUUID())) {
            // this is all we can do as per the API.
            throw new ClassCastException("Incomparable structures. " +
            		"These structures are defined for different texts.");  
        }
        
        // If one structure starts before the other, it comes first. This holds for both
        // nested and overlapping structures.
        int startDiff = a.getStart() - b.getStart();
        if (startDiff != 0)
            return startDiff;
        
        // These structures are co-extensive. We'll order them (semi-arbitrarily) 
        // by their names or UUIDs.
        if (b.getEnd() == a.getEnd()) {
            int order = a.getName().compareTo(b.getName());
            if (order == 0) {
                // This seems like a fringe case, but it is required to be consistent 
                // with 'equals'.
                String aUUID = a.getUUID().toString();                
                String bUUID = b.getUUID().toString();
                
                order = aUUID.compareTo(bUUID);
            }
            
            return order;
        }

        // If they start at the same place, the one that ends last is the "earlier" 
        // structure because because it is nested inside the surrounding structure.
        return b.getEnd() - a.getEnd();
    }
}