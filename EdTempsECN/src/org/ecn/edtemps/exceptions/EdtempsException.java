package org.ecn.edtemps.exceptions;

/**
 * Classe générale pour les exceptions de l'application
 * 
 * @author Remi
 */
public class EdtempsException extends Exception {

	private static final long serialVersionUID = -4822842236569206129L;

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
	
	public EdtempsException(ResultCode resultCode, String message, Throwable e) {
		super(message, e);
		this.resultCode = resultCode;
	}
	
	public ResultCode getResultCode() {
		return resultCode;
	}
}
