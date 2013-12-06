package org.ecn.edtemps.diagnosticbdd;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

/**
 * Classe pour les tests de base de données
 * 
 * @author Remi
 */
public abstract class TestBdd {

	protected String nom;
	protected int id;
	protected String repairMessage;
	
	/**
	 * Code de retour d'un test de base de données
	 * @author Remi
	 *
	 */
	public static enum TestBddResultCode {
		OK("ok"),
		ERROR("error"),
		WARNING("warning"),
		TEST_FAILED("failed");
		
		protected String label;
		
		private TestBddResultCode(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
	}
	
	/**
	 * Résultat d'un test de base de données
	 * @author Remi
	 *
	 */
	public static class TestBddResult {
		protected TestBdd test;
		protected TestBddResultCode resultCode;
		protected String message;
		
		public TestBddResult(TestBddResultCode resultCode, String message, TestBdd test) {
			this.resultCode = resultCode;
			this.message = message;
			this.test = test;
		}
		
		public final TestBddResultCode getResultCode() {
			return resultCode;
		}
		
		public final String getMessage() {
			return message;
		}
		
		public final TestBdd getTest() {
			return test;
		}
	}
	
	public TestBdd(String nom, int id, String repairMessage) {
		this.nom = nom;
		this.id = id;
		this.repairMessage = repairMessage;
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
		return nom;
	}
	
	public String getRepairMessage() {
		return repairMessage;
	}
	
	public int getId() {
		return this.id;
	}
}
