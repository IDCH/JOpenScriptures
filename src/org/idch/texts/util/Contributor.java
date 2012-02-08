/**
 * 
 */
package org.idch.texts.util;

public class Contributor {
	private String contributor;
	private String role;
	
	public Contributor(String contributor, String role) {
		this.contributor = contributor;
		this.role = role;
	}
	
	public String getContributor() {
		return this.contributor;
	}
	
	public String getRole() {
		return this.role;
	}
}