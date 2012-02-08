/**
 * 
 */
package org.idch.texts.persist.mem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.idch.texts.Structure;
import org.idch.texts.StructureComparator;
import org.idch.texts.StructureRepository;
import org.idch.texts.Token;
import org.idch.texts.Work;


/**
 * @author Neal Audenaert
 */
public class MemStructureRepository implements StructureRepository {
    
    //===================================================================================
    // SYMBOLIC CONSTANTS
    //===================================================================================
    private final static Logger LOGGER = Logger.getLogger(MemStructureRepository.class);
    
    
    //===================================================================================
    // STATIC METHODS
    //===================================================================================
    
    
    //===================================================================================
    // MEMBER VARIABLES
    //===================================================================================
    private static long nextId = 1;
    
    @SuppressWarnings("unused")
    private MemTextModule repo = null;
    @SuppressWarnings("unused")
    private AttrIndex attrs = null;
    
    private Map<String, Structure> structuresByUUID = new HashMap<String, Structure>();
    private Map<Long, Structure> structuresById = new HashMap<Long, Structure>();
    private StructureIndex index = new StructureIndex();
    //===================================================================================
    // CONSTRUCTORS
    //===================================================================================
    
    /**
     * 
     * @param repo
     */
    MemStructureRepository(MemTextModule repo) {
        this.repo = repo;
        this.attrs = new AttrIndex();
    }

    //===================================================================================
    // HELPER METHODS
    //===================================================================================

    
    
    
    //===================================================================================
    // CREATION AND UPDATE METHOS
    //===================================================================================

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#create(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public Structure create(Work work, String name) {
        return create(new Structure(work.getUUID(), name));
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#create(openscriptures.text.Work, java.lang.String, openscriptures.text.Token, openscriptures.text.Token)
     */
    @Override
    public Structure create(Work work, String name, Token start, Token end) {
        return create(new Structure(work.getUUID(), name, start, end));
    }

        
    public Structure create(Structure s) {
        assert (s.getId() == null) : "This structure has already been created.";
        if (s.getId() != null)
            return null;
        
        s.setId(nextId++);
        
        index.add(s);
        this.structuresById.put(s.getId(), s);
        this.structuresByUUID.put(s.getUUID().toString(), s);
        
        return s;
    }
    
    
    
    /**
     * 
     * @see org.idch.texts.StructureRepository#save(org.idch.texts.Structure)
     */
    @Override
    public boolean save(Structure s) {
        // NOTE this should be the default method for saving/creating structures.
        assert s != null : "Cannot save a null structure.";
        if (s == null) {
            throw new NullPointerException("Cannot save a null structure");
        }
        
        // This is a new structure. We should create it instead.
        if (s.getId() == null) {
            return create(s) != null;
        } else {
            index.add(s);
            this.structuresById.put(s.getId(), s);
            this.structuresByUUID.put(s.getUUID().toString(), s);
            return true;
        }
        
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#hasStructuresFor(openscriptures.text.Work)
     */
    @Override
    public boolean hasStructuresFor(UUID workId) {
        StructureIndexByWork structures = index.getByWork(workId.toString());
        return structures.size() != 0;
    }
    

    //===================================================================================
    // RETRIEVAL METHOS
    //===================================================================================

   
    
    
    /**
     * 
     * @param s
     * @return
     */
    public Structure synchronize(Structure s) {
        if (s.getId() != null) {
            return this.structuresById.get(s.getId());
        } else if (s.getUUID() != null) {
            return this.structuresByUUID.get(s.getUUID().toString());
        } else {
            String msg = "Could not retrieve work. No identifier supplied.";
            LOGGER.warn(msg);
            return null;
        }
    }
    
    /**
     * 
     * @param id
     * @return
     */
    public Structure find(long id) {
        return this.index.get(id);
    }
    
    public Structure find(UUID id) {
        return this.index.get(id.toString());
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name) {
        StructureIndexByWork structures = index.getByWork(w.getUUID().toString());
        return structures.get(name);
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, int position) {
        return new TreeSet<Structure>(new StructureComparator());
    }

    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String, int, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, int start, int end) {
        StructureIndexByWork.filter(find(w, name), start, end);
        return new TreeSet<Structure>(new StructureComparator());
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String, int, int)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, int start, int end, boolean strict) {
        return new TreeSet<Structure>(new StructureComparator());
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public Map<UUID, SortedSet<Structure>> find(String name, String attribute, String value) {
        return new HashMap<UUID, SortedSet<Structure>>();
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.StructureRepository#find(openscriptures.text.Work, java.lang.String)
     */
    @Override
    public SortedSet<Structure> find(Work w, String name, String attribute, String value) {
        return new TreeSet<Structure>(new StructureComparator());
    }
    

    
    
    //===================================================================================
    // INNER CLASS FOR PERSISTING ATTRIBUTES
    //===================================================================================
    
    private static class StructureIndex {
        Map<String, StructureIndexByWork> indexByWork = new HashMap<String, StructureIndexByWork>();
        Map<String, Structure> indexByUUID = new HashMap<String, Structure>();
        Map<Long, Structure> indexById = new HashMap<Long, Structure>();
        
        void add(Structure s) {
            String workUuid = s.getWorkUUID().toString();
            
            getByWork(workUuid).add(s);
            indexByUUID.put(s.getUUID().toString(), s);
            indexById.put(s.getId(), s);
        }
        
        public StructureIndexByWork getByWork(String workId) {
            StructureIndexByWork workIndex = indexByWork.get(workId);
            if (workIndex == null) {
                workIndex = new StructureIndexByWork(workId);
                indexByWork.put(workId, workIndex);
            }
            
            return workIndex;
        }
        
        public Structure get(String sUUID) {
            return indexByUUID.get(sUUID);
        }
        
        public Structure get(long id) {
            return indexById.get(id);
        }
    }
    
    @SuppressWarnings("unused")
    private static class StructureIndexByWork {
        private static SortedSet<Structure> filter(SortedSet<Structure> structures, int pos) {
            SortedSet<Structure> results = new TreeSet<Structure>(new StructureComparator());
            
            for (Structure s : structures) {
                if (s.getEnd() <= pos) 
                    continue;
                
                if (s.getStart() > pos)
                    break;
                
                if (s.getStart() <= pos && s.getEnd() > pos) {
                    results.add(s);
                }
            }
            
            return results;
        }
        
        private static SortedSet<Structure> filter(SortedSet<Structure> structures, int start, int end) {
            SortedSet<Structure> results = new TreeSet<Structure>(new StructureComparator());
            
            for (Structure s : structures) {
                if (start <= s.getStart() && end > s.getEnd())
                    results.add(s);
            }
            
            return results;
        }
        
        private final String workUUID;
        
        // by name
        Map<String, SortedSet<Structure>> index = new HashMap<String, SortedSet<Structure>>();
        
        StructureIndexByWork(String id) {
            workUUID = id;
        }
        
        public int size() {
            int sz = 0;
            for (SortedSet<Structure> s : index.values())
                sz += s.size();
            
            return sz;
        }
        
        public SortedSet<Structure> get(String name) {
            SortedSet<Structure> structures = index.get(name);
            if (structures == null) {
                structures = new TreeSet<Structure>(new StructureComparator());
                index.put(name, structures);
            }
            
            return structures;
        }
        
        public boolean add(Structure s) {
            SortedSet<Structure> structures = this.get(s.getName());
            return structures.add(s);
        }
        
    }
    
    
    @SuppressWarnings("unused")
    private static class AttrIndex {
        Map<KeyValuePair, SortedSet<Structure>> structures = new HashMap<KeyValuePair, SortedSet<Structure>>();
        
        public AttrIndex() {  }
        
        public void add(Structure s) {
            Map<String, String> attrs = s.getAttributes();
            for (String key : attrs.keySet()) {
                put(key, attrs.get(key), s);
            }
        }
        
        public void put(String key, String value, Structure s) {
            // add this to the main structures table
            KeyValuePair kvp = KeyValuePair.get(key, value);
            SortedSet<Structure> structures = this.structures.get(kvp);
            if (structures == null) {
                structures = new TreeSet<Structure>(new StructureComparator());
                this.structures.put(kvp, structures);
            }
            
            if (!structures.contains(s))
                structures.add(s);
        }
        
        
        public SortedSet<Structure> get(String key, String value) {
            KeyValuePair kvp = KeyValuePair.get(key, value);
            SortedSet<Structure> structures = this.structures.get(kvp);
            if (structures == null) {
                structures = new TreeSet<Structure>(new StructureComparator());
            }
            
            return Collections.unmodifiableSortedSet(structures);
        }
        
        public SortedSet<Structure> get(String key) {
            Map<String, KeyValuePair> kvps = KeyValuePair.get(key);
            SortedSet<Structure> structures = 
                    new TreeSet<Structure>(new StructureComparator());
            for (KeyValuePair kvp : kvps.values()) {
                SortedSet<Structure> structs = this.structures.get(kvp);
                if (structs != null) {
                    structures.addAll(structs);
                }
            }
            
            return structures;
        }
        
    }   // END AttrRepo class
    
    
    private static class KeyValuePair implements Comparable<KeyValuePair> {
        private static Map<String, Map<String, KeyValuePair>> pairs =
                new HashMap<String, Map<String, KeyValuePair>>();
        
        public static KeyValuePair get(String key, String value) {
            Map<String, KeyValuePair> kvps = get(key);
            KeyValuePair kvp = kvps.get(value);
            if (kvp == null) {
                kvp = new KeyValuePair(key, value);
                kvps.put(value, kvp);
            }
            
            return kvp;
        }
        
        public static Map<String, KeyValuePair> get(String key) {
            Map<String, KeyValuePair> kvps = pairs.get(key);
            if (kvps == null) {
                kvps = new HashMap<String, KeyValuePair>();
                pairs.put(key, kvps);
            }
            
            return kvps;
        }
        
        private final String key;
        private final String value;
        
        KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String toString() {
            return this.key + "::" + this.value;
        }
        
        public int compareTo(KeyValuePair kvp) {
            return this.toString().compareTo(kvp.toString());
        }
        
        public boolean equals(Object o) {
            return this.toString().equals(((KeyValuePair)o).toString());
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
    }
}
