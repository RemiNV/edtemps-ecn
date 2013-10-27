package org.ecn.edtemps.exceptions;

/**
 * Erreur d'identification de l'utilisateur
 * @author Remi
 *
 */
public class IdentificationException extends EdtempsException {

	public IdentificationException(ResultCode resultCode, String message) {
		super(resultCode, message);
	}
	
	public IdentificationException(ResultCode resultCode, String message, Throwable e) {
		super(resultCode, message, e);
	}
}
