/**
 * 
 */
package org.sagebionetworks.repo.model;

/**
 * @author deflaux
 * 
 */
public class AuthorizationConstants {
	
	public enum ACCESS_TYPE {
		READ,
		CHANGE,
		SHARE
	};
	
	/**
	 * The group name for a system defined group which allows access to its
	 * resources to all (including anonymous users)
	 */
	public static final String PUBLIC_GROUP_NAME = "Public";
	
	/**
	 * The group name for those users that have all kinds of access to all resources.
	 */
	public static final String ADMIN_GROUP_NAME = "Administrators";
	

}
