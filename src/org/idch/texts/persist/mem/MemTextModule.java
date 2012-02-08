/**
 * 
 */
package org.idch.texts.persist.mem;

import org.idch.texts.StructureRepository;
import org.idch.texts.TokenRepository;
import org.idch.texts.WorkRepository;
import org.idch.texts.persist.AbstractTextModule;

/**
 * @author Neal Audenaert
 */
public class MemTextModule extends AbstractTextModule {

    private WorkRepository m_worksRepo = new MemWorkRepository(this);
    private TokenRepository m_tokensRepo = new MemTokenRepository(this);
    private StructureRepository m_structuresRepo = new MemStructureRepository(this);
    
    //========================================================================
    // REPOSITORY GETTERS
    //========================================================================
    public WorkRepository getWorkRepository() {
        return this.m_worksRepo;
    }
    
    public TokenRepository getTokenRepository() {
        return this.m_tokensRepo;
    }
    
    public StructureRepository getStructureRepository() {
        return this.m_structuresRepo;
    }

    /* (non-Javadoc)
     * @see org.idch.persist.DBBackedRepository#probe()
     */
    @Override
    public boolean probe() {
        // TODO Auto-generated method stub
        return true;
    }
    
    //========================================================================
    // UTILITY METHODS FOR INTERACTING WITH STRUCTURES
    //========================================================================


    
}
