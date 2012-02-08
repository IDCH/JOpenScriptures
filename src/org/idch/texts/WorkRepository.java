/**
 * 
 */
package org.idch.texts;

import java.util.List;
import java.util.UUID;

/**
 * @author Neal Audenaert
 */
public interface WorkRepository {
    
    public TokenRepository getTokenRepository();
    
    public Work create(String workId);
    
//    public Work create(WorkId id, String title, String abbr, String desc);
    
    public Work find(long id);
    
    public Work find(UUID id);
    
    public List<Work> findByType(String type);
    
    public List<Work> findByType(String type, String lgCode);
    
    public List<Work> findByAbbr(String abbreviation);
    
    public boolean save(Work w);
    
    public boolean remove(Work w);

}
