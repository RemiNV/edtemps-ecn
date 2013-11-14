package org.ecn.edtemps.exceptions;

/**
 * Exception des méthodes accédant à la base de données
 * 
 * @author Remi
 */
public class DatabaseException extends EdtempsException {

	private static final long serialVersionUID = 3116816893227762498L;

	public DatabaseException() {
		super(ResultCode.DATABASE_ERROR);
	}

	public DatabaseException(Throwable e) {
		super(ResultCode.DATABASE_ERROR, e);
	}
}
