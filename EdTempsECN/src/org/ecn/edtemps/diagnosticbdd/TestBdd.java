package org.ecn.edtemps.diagnosticbdd;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

public abstract class TestBdd {

	protected String nom;
	protected int id;
	
	/**
	 * Code de retour d'un test de base de données
	 * @author Remi
	 *
	 */
	public static enum TestBddResultCode {
		OK,
		WARNING,
		ERROR,
		TEST_FAILED
	}
	
	/**
	 * Résultat d'un test de base de données
	 * @author Remi
	 *
	 */
	public static class TestBddResult {
		protected TestBddResultCode resultCode;
		protected String message;
		
		public TestBddResult(TestBddResultCode resultCode, String message) {
			this.resultCode = resultCode;
			this.message = message;
		}
		
		public final TestBddResultCode getResultCode() {
			return resultCode;
		}
		
		public final String getMessage() {
			return message;
		}
	}
	
	public TestBdd(String nom, int id) {
		this.nom = nom;
		this.id = id;
	}
	
	/**
	 * Lancement d'un test de base de données
	 * @param bdd Gestionnaire de base de données <b>pour lequel une transaction est en cours</b>
	 * @return Résultat du test
	 * @throws DatabaseException Erreur de communication avec la base
	 */
	public abstract TestBddResult test(BddGestion bdd) throws DatabaseException;
	
	/**
	 * Effectue la réparation de la base correspondant au test
	 * @param bdd Gestionnaire de base de données <b>pour lequel une transaction est en cours</b>
	 * @return Message de réparation
	 * @throws DatabaseException Erreur de communication avec la base
	 */
	public abstract String repair(BddGestion bdd) throws DatabaseException;
	
	public String getNom() {
		return this.nom;
	}
	
	public int getId() {
		return this.id;
	}
}
