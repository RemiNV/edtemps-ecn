package org.ecn.edtemps.exceptions;

/**
 * Exception des méthodes accédant à la base de données
 */
public class DatabaseException extends EdtempsException {

	public DatabaseException() {
		super(ResultCode.DATABASE_ERROR);
	}

	public DatabaseException(Throwable e) {
		super(ResultCode.DATABASE_ERROR, e);
	}
}
