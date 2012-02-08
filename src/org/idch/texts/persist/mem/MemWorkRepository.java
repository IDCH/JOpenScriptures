/**
 * 
 */
package org.idch.texts.persist.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.idch.texts.TokenRepository;
import org.idch.texts.Work;
import org.idch.texts.WorkId;
import org.idch.texts.WorkRepository;


/**
 * @author Neal Audenaert
 */
class MemWorkRepository implements WorkRepository {
    private final static Logger LOGGER = Logger.getLogger(MemWorkRepository.class);
    
    private static long nextId = 1;
    MemTextModule repo = null;
    
    // Note, access to the cache is unsynchronized. This should not be used in a 
    // multi-threaded environment 
    private Map<String, Work> worksByUUID = new HashMap<String, Work>();
    private Map<Long, Work> worksById = new HashMap<Long, Work>();
    
    MemWorkRepository(MemTextModule repo) {
        this.repo = repo;
    }

    private void cache(Work w) {
        if (w.getUUID() == null || w.getId() == null) {
            throw new RuntimeException("Invalid work: identifiers not specified");
        }
        
        if (w != null) {
            worksByUUID.put(w.getUUID().toString(), w);
            worksById.put(w.getId(), w);
        }
    }
    
    private void release(Work w) {
        if (w.getUUID() == null || w.getId() == null) {
            throw new RuntimeException("Invalid work: identifiers not specified");
        }
        
        if (w != null) {
            worksByUUID.remove(w.getUUID().toString());
            worksById.remove(w.getId());
        }
    }
    
    /**
     * 
     */
    @Override
    public TokenRepository getTokenRepository() {
        return repo.getTokenRepository();
    }

    /**
     * 
     */
    @Override
    public Work create(String workId) {
        return create(new Work(new WorkId(workId)));
    }
    

    
    public synchronized Work create(Work w) {
        assert (w.getId() == null) : "This work has already been created.";
        if (w.getId() != null)
            return null;
        
        w.setId(nextId++);
        cache(w);
        return w;
    }

    public Work find(long id) {
        return worksById.get(id);
    }
    
    public Work find(UUID id) {
        return worksByUUID.get(id.toString());
    }
    
    /**
     * Synchronizes the state of the supplied work with the database. Any changes to the 
     * work will be overridden with the latest values from the database. 
     * 
     * @param w The work to be synchronized.
     * @return The synchronized work instance.
     */
    public Work synchronize(Work w) {
        if (w.getId() != null) {
            return worksById.get(w.getId());
        } else if (w.getUUID() != null) {
            return worksByUUID.get(w.getUUID().toString());
        } else {
            String msg = "Could not retrieve work. No identifier supplied.";
            LOGGER.warn(msg);
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.WorkRepository#getByWorkId(openscriptures.text.WorkId.Type)
     */
    @Override
    public List<Work> findByType(String type) {
        Work w = null;
        List<Work> works = new ArrayList<Work>();
        for (long id : worksById.keySet()) {
            w = worksById.get(id);
            if (w.getType().equals(type))
                works.add(w);
        }
        
        return works;
    }
    
    /* (non-Javadoc)
     * @see openscriptures.text.WorkRepository#getByWorkId(openscriptures.text.WorkId.Type, java.lang.String)
     */
    @Override
    public List<Work> findByType(String type, String lgCode) {
        Work w = null;
        List<Work> works = new ArrayList<Work>();
        for (long id : worksById.keySet()) {
            w = worksById.get(id);
            if (w.getType().equals(type) && w.getLgCode().equalsIgnoreCase(lgCode))
                works.add(w);
        }
        
        return works;
    }
    


    /* (non-Javadoc)
     * @see openscriptures.text.WorkRepository#getByWorkId(java.lang.String)
     */
    @Override
    public List<Work> findByAbbr(String abbreviation) {
        Work w = null;
        List<Work> works = new ArrayList<Work>();
        for (long id : worksById.keySet()) {
            w = worksById.get(id);
            if (w.getAbbreviation().equals(abbreviation))
                works.add(w);
        }
        
        return works;
    }

    public boolean save(Work w) {
        this.cache(w);
        return true;
    }
    
    /**
     * Removes the indicated work and all associated tokens. Note that this does not remove 
     * any structures associated with this work. 
     */
    public boolean remove(Work w) {
        if (worksById.containsKey(w.getId())) {
            this.release(w);
            
            // probably ought to clear out the tokens as well.
            return true;
        } else return false;
    }


    
}
