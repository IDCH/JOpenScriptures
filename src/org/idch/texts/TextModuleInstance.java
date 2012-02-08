/**
 * 
 */
package org.idch.texts;

import org.idch.persist.DBBackedRepository;
import org.idch.persist.RepositoryAccessException;

/**
 * @author Neal Audenaert
 */
public class TextModuleInstance {
    public final static String MODULE_NAME = "texts";
    
    public static final TextModule get()
            throws RepositoryAccessException {
        
        return (TextModule)DBBackedRepository.get(MODULE_NAME);
    }
}
