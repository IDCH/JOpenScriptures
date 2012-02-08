/**
 * 
 */
package org.idch.texts.structures;

import org.idch.texts.Structure;
import org.idch.texts.StructureWrapper;
import org.idch.texts.TextModule;
import org.idch.texts.Work;
import org.idch.texts.WorkRepository;

/**
 * @author Neal Audenaert
 */
public abstract class WorkStructureWrapper extends StructureWrapper {
    public static final String ATTR_OSIS_ID = "osisId";
    
    protected final TextModule repo;
    
    public WorkStructureWrapper(TextModule repo, Structure s) {
        super(s);
        
        this.repo = repo;
    }

    protected Work getWork() {
        WorkRepository worksRepo = repo.getWorkRepository();
        return worksRepo.find(this.getWorkUUID());
    }
    
    /**
     * 
     * @return
     */
    public String getOsisId() {
        return this.getAttribute(ATTR_OSIS_ID);
    }
    
    public void setOsisId(String id) {
        // TODO check pattern?
        this.setAttribute(ATTR_OSIS_ID, id);
    }
}
