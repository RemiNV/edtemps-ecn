package org.ecn.edtemps.diagnosticbdd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

/**
 * Classe de détection des éléments incorrects dans la base
 * @author Remi
 */
public abstract class TestEntiteIncorrecte extends TestBdd {

	/**
	 * Constructeur
	 * @param nom Nom du test
	 * @param id Identifiant du test
	 * @param repairMessage Infobulle sur le bouton de réparation
	 */
	public TestEntiteIncorrecte(String nom, int id, String repairMessage) {
		super(nom, id, repairMessage);
	}

	/**
	 * Récupérer la liste des identifiants de lignes incorrectes
	 * @param bdd Gestionnaire de base de données
	 * @return liste des identifiants des entrées incorrectes
	 * @throws DatabaseException
	 */
	protected ArrayList<Integer> getLstIncorrects(BddGestion bdd) throws DatabaseException {
		try {
			PreparedStatement statementListing = getStatementListing(bdd);
			ArrayList<Integer> ids = bdd.recupererIds(statementListing, getColonneId());
			statementListing.close();
			return ids;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public final TestBddResult test(BddGestion bdd) throws DatabaseException {
		ArrayList<Integer> lstIncorrects = getLstIncorrects(bdd);
		
		if(lstIncorrects.isEmpty()) {
			return new TestBddResult(TestBddResultCode.OK, "Aucune entité incorrecte trouvée", this);
		}
		else {
			return new TestBddResult(TestBddResultCode.WARNING, "Des entités incorrectes ont été trouvées : ID " + DiagnosticsBdd.getStrPremiersIds(lstIncorrects), this);
		}
	}

	@Override
	public final String repair(BddGestion bdd) throws DatabaseException {
		
		ArrayList<Integer> lstIncorrects = getLstIncorrects(bdd);
		return reparerIncorrects(bdd, lstIncorrects);
	}
	
	/**
	 * Méthode de réparation des données incorrectes
	 * @param bdd Gestionnaire de la base de données
	 * @param ids Liste des identifiants des lignes à corriger
	 * @return message affiché suite à la correction
	 * @throws DatabaseException
	 */
	protected abstract String reparerIncorrects(BddGestion bdd, ArrayList<Integer> ids) throws DatabaseException;
	
	/**
	 * Retourne la requête qui va permettre de trouver les lignes incorrectes
	 * @param bdd
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getStatementListing(BddGestion bdd) throws SQLException;
	
	/**
	 * Définition du nom de la colonne comportant l'identifiant de la ligne à traiter
	 * @return nom de la colonne
	 */
	protected abstract String getColonneId();

}
