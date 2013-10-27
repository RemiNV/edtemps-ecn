package org.ecn.edtemps.exceptions;

/**
 * Exception signalant des erreurs anormales lors de l'identification
 * (telles qu'une erreur d'accès à LDAP, à l'opposé d'un login incorrect)
 * @author Remi
 *
 */
public class IdentificationErrorException extends IdentificationException {
	
	public IdentificationErrorException(ResultCode resultCode, String message, Throwable e) {
		super(resultCode, message, e);
	}
}
