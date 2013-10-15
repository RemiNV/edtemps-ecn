package org.ecn.edtemps.exceptions;

/**
 * Classe à compléter plus tard en ayant défini un format pour les exceptions
 * @author Remi
 *
 */
public class EdtempsException extends Exception {

	private ResultCode resultCode;
	
	public EdtempsException(ResultCode resultCode) {
		super();
		this.resultCode = resultCode;
	}
	
	public EdtempsException(ResultCode resultCode, String message) {
		super(message);
		this.resultCode = resultCode;
	}
	
	public EdtempsException(ResultCode resultCode, Throwable e) {
		super(e);
		this.resultCode = resultCode;
	}
	
	public ResultCode getResultCode() {
		return resultCode;
	}
}
