package org.ecn.edtemps.exceptions;

/**
 * Exception levée lorsqu'une requête retourne trop de lignes.
 * Définition d'une classe à part nécessaire pour une gestion à part dans les try/catch
 * @author Remi
 *
 */
public class MaxRowCountExceededException extends EdtempsException {
	public MaxRowCountExceededException(String message) {
		super(ResultCode.MAX_ROW_COUNT_EXCEEDED, message);
	}
	
	/**
	 * @param max Nombre maximum de lignes autorisé
	 * @param actual Nombre de lignes retournées
	 */
	public MaxRowCountExceededException(long max, long actual) {
		super(ResultCode.MAX_ROW_COUNT_EXCEEDED, "Nombre de résultats trop élevé : " + actual + " ; maximum " + max);
	}
}
