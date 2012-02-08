/**
 * 
 */
package org.idch.bible.ref;

import java.util.Date;
import java.util.List;

/**
 * Represents meta information that describes a particular definition of a 
 * Reference System component (e.g. book names list, book order, etc).
 *  
 * @author Neal Audenaert
 */
@SuppressWarnings("unused")
public class SystemDescription {
// TODO figure out the right name for this.
    /*
     * <work>
          <version>0.10</version>
          <date>2010-12-01</date>
          <title>Catholic Bible book order</title>
          <contributor role="com">Robert Hunt</contributor>
          <rights>Public Domain</rights>
        </work>
     */
    
    private String name; 
	private String version;
	private Date lastUpdated;
	private List<String> contributor;
	private String rights;
	private String language;
}
