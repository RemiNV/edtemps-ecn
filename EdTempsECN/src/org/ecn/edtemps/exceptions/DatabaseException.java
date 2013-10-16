package org.ecn.edtemps.exceptions;

/**
 * Exception des méthodes accédant à la base de données
 */
public class DatabaseException extends EdtempsException {

	public DatabaseException() {
		super(ResultCode.DATABASE_ERROR);
		System.out
				.println("Une erreur est survenue lors de l'accès à la base de données.");
	}

	public DatabaseException(Throwable e) {
		super(ResultCode.DATABASE_ERROR, e);
		System.out
				.println("Une erreur est survenue lors de l'accès à la base de données ("
						+ e.getMessage() + ")");
	}
}
