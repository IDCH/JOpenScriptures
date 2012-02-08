/**
 * 
 */
package org.idch.texts.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * 
 * @author Neal Audenaert
 */
public class License {
	private static final Logger LOGGER = Logger.getLogger(License.class.getName());
	
	public static final License BY = new License("Attribution", "by",
			"This license lets others distribute, remix, tweak, and build upon your work, even commercially, as long as they credit you for the original creation. This is the most accommodating of licenses offered. Recommended for maximum dissemination and use of licensed materials.",
			"http://creativecommons.org/licenses/by/3.0/legalcode",
			true /* by */, false /* sa */, false /* nd */, false /* nc */);
	
	public static final License BY_SA = new License("Attribution-ShareAlike", "by-sa",
			"This license lets others remix, tweak, and build upon your work even for commercial purposes, as long as they credit you and license their new creations under the identical terms. This license is often compared to “copyleft” free and open source software licenses. All new works based on yours will carry the same license, so any derivatives will also allow commercial use. This is the license used by Wikipedia, and is recommended for materials that would benefit from incorporating content from Wikipedia and similarly licensed projects.",
			"http://creativecommons.org/licenses/by-sa/3.0/legalcode",
			true /* by */, true /* sa */, false /* nd */, false /* nc */);
	
	public static final License BY_ND = new License("Attribution-NoDerivs", "by-nd",
			"This license allows for redistribution, commercial and non-commercial, as long as it is passed along unchanged and in whole, with credit to you.",
			"http://creativecommons.org/licenses/by-nd/3.0/legalcode",
			true /* by */, false /* sa */, true /* nd */, false /* nc */);
	
	public static final License BY_NC = new License("Attribution-NonCommercial", "by-nc",
			"This license lets others remix, tweak, and build upon your work non-commercially, and although their new works must also acknowledge you and be non-commercial, they don’t have to license their derivative works on the same terms.",
			"http://creativecommons.org/licenses/by-nc/3.0/legalcode",
			true /* by */, false /* sa */, false /* nd */, true /* nc */);
	
	public static final License BY_NC_SA = new License("Attribution-NonCommercial-ShareAlike", "by-nc-sa",
			"This license lets others remix, tweak, and build upon your work non-commercially, as long as they credit you and license their new creations under the identical terms.",
			"http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode",
			true /* by */, true /* sa */, false /* nd */, true /* nc */); 
	
	public static final License BY_NC_ND = new License("Attribution-NonCommercial-NoDerivs", "by-nc-nd",
			"This license is the most restrictive of our six main licenses, only allowing others to download your works and share them with others as long as they credit you, but they can’t change them in any way or use them commercially.",
			"http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode",
			true /* by */, false /* sa */, true /* nd */, true /* nc */);
	
	public static final License CC0 = new License("No Rights Reserved", "CC0",
			"CC0 enables scientists, educators, artists and other creators and owners of copyright- or database-protected content to waive those interests in their works and thereby place them as completely as possible in the public domain, so that others may freely build upon, enhance and reuse the works for any purposes without restriction under copyright or database law.",
			"http://creativecommons.org/about/cc0",
			false /* by */, false /* sa */, false /* nd */, false /* nc */);
	
	public static final License PUBLIC_DOMAIN = new License("Public Domain", "",
			"When a work is in the public domain, it is free for use by anyone for any purpose without restriction under copyright law. Public domain is the purest form of open/free, since no one owns or controls the material in any way.",
			"http://wiki.creativecommons.org/Public_domain",
			false /* by */, false /* sa */, false /* nd */, false /* nc */);
	
	private String name;
	private String abbreviation;
	private String description;
	
	/**
	 * A URL for more information about this license.
	 * 
	 * NOTE This poses a security risk in the event that people may supply a URL 
	 * 	    that points to malicious content. It is important to ensure that whoever
	 * 		has access to specify the license is trusted.
	 */
	private URL url;
	
	private Boolean attribution;
	private Boolean nonCommercial;
	private Boolean shareAlike;
	private Boolean noDerivatives;
	
	/** If this is true, then this work can be displayed independently. Otherwise, it 
	 *  must only be displayed in conjunction with other works. Important condition 
	 *  for fair use license. */
	Boolean isolateable = null;
	
	/**
	 * 
	 * @param name
	 * @param abbreviation
	 * @param description
	 * @param url
	 */
	public License(String name, String abbreviation, String description, String url) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.description = description;
	}
	
	/**
	 * 
	 * @param name
	 * @param abbreviation
	 * @param description
	 * @param url
	 * @param by
	 * @param sa
	 * @param nd
	 * @param nc
	 */
	public License(String name, String abbreviation, String description, String url,
				boolean by, boolean sa, boolean nd, boolean nc) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.description = description;
		
		try {
			this.url = new URL(url);
		} catch (MalformedURLException ex) {
			this.url = null;
			LOGGER.warning("Bad URL (" + url +"): Fearlessly pressing on without one.");
			LOGGER.warning(ex.getLocalizedMessage());
		}
		
		assert !(sa && nd) : 
			"Incompatible Parameters: A licence cannot both prevent derivative " +
			"forms and require that derivative forms be shared.";
	}
	
	public String getName() {
	    return this.name;
	}
	
	public String getAbbreviation() {
	    return this.abbreviation;
	}
	
	public String getDescription() {
	    return this.description;
	}
	
	public URL getUrl() {
	    return this.url;
	}
	
	/** 
	 * Indicates whether or not this licenses requires that use of a work attribute 
	 * the rights holder. 
	 * 
	 * @return <tt>true</tt> if attribution is required, <tt>false</tt> if it is not or
	 *     <tt>null</tt> if it is not specified. Note that if this is not specified, it 
	 *     is not safe to make an assumption about whether or not attribution is required.
	 *     Instead, the person seeking to use this work should consult the license 
	 *     documentation or contact the rights holder.
	 */
	public Boolean requireAttribution() { 
		return this.attribution;
	}
	
	public Boolean requireSharing() {
		return this.shareAlike;
	}
	
	public Boolean allowCommercialUse() {
		return this.nonCommercial;
	}
	
	public Boolean allowDerivativeWorks() {
		return (this.noDerivatives != null) ? !this.noDerivatives : null;
	}
	
	
}