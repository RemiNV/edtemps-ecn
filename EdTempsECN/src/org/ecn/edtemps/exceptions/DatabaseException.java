package org.ecn.edtemps.exceptions;

/**
 * Nom d'exception choisi au pif, à changer éventuellement
 * 
 * @author Remi
 * 
 */
public class DatabaseException extends Exception {

	public DatabaseException() {
		super();
		System.out
				.println("Une erreur est survenue lors de l'accès à la base de données.");
	}

	public DatabaseException(Exception e) {
		super(e);
		System.out
				.println("Une erreur est survenue lors de l'accès à la base de données ("
						+ e.getMessage() + ")");
	}
}
