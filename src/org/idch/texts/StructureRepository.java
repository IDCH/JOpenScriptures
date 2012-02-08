/**
 * 
 */
package org.idch.texts;

import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;


/**
 * @author Neal Audenaert
 */
public interface StructureRepository {

    public Structure create(Work work, String name);
    
    public Structure create(Work work, String name, Token start, Token end);
    
    public Structure create(Structure s);
    
    public boolean hasStructuresFor(UUID workId);
    
    public Structure synchronize(Structure s);
    
    /**
     * Looks up an existing structure by its globally unique identifier.
     * 
     * @param id The id of the structure to retrieve.
     * @return The identified structure or null if no such structure exists. 
     */
    public Structure find(UUID id);
    
    /** 
     * Looks up an existing structure by its internal database identifier.
     * 
     * @param id The id of the structure to retrieve.
     * @return The identified structure or null if no such structure exists. 
     */
    public Structure find(long id);

    // TODO need to find a better way to do this
    /**
     * Looks up all structures within a work having the specified name. For example, 
     * find all 'book' structures in the SBLGNT.
     *  
     * @param w The work for which to retrieve structures.
     * @param name The name of the structure to retrieve.
     * @return The specified structures or the empty set if no such structures exist.
     */
    public SortedSet<Structure> find(Work w, String name);
    
    /**
     * Looks up all structures within a work having that include the specified token. 
     *  
     * @param w The work for which to retrieve structures.
     * @param position The token position that should be spanned by the returned structures. 
     * @return The specified structures or the empty set if no such structures exist.
     */
    public SortedSet<Structure> find(Work w, int position);
    
    public SortedSet<Structure> find(Work w, String name, int start, int end);
    
    public SortedSet<Structure> find(Work w, String name, int start, int end, boolean strict);
    
    public SortedSet<Structure> find(Work w, String name, String attribute, String value);
    
    public Map<UUID, SortedSet<Structure>> find(String name, String attribute, String value);
    
    public boolean save(Structure s);
}
