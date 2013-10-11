package org.ecn.edtemps.exceptions;

/**
 * Codes de retour des requêtes 
 * @author Remi
 *
 */
public enum ResultCode {

	SUCCESS(0),
	IDENTIFICATION_ERROR(1),
	IDENTIFICATION_EXPIRED(2),
	LDAP_CONNECTION_ERROR(3),
	DATABASE_ERROR(4),
	CRYPTOGRAPHIC_ERROR(5),
	WRONG_ARGUMENTS_FOR_REQUEST(6);
	
	private int code;
	
	ResultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
