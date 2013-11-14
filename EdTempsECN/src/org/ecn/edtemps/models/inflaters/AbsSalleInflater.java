package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Génération d'une salle à partir d'une ligne de base de données
 * 
 * Colonnes obligatoires pour tous les types de salle : <br>
 * - salle_id<br>
 * - salle_batiment<br>
 * - salle_nom<br>
 * - salle_niveau<br>
 * - salle_numero<br>
 * - salle_capacite 
 * 
 * @author Remi
 *
 * @param <T> Type de salle à générer
 */
public abstract class AbsSalleInflater<T extends SalleIdentifie> {

	/**
	 * Méthode de génération de la salle à partir d'une ligne de la base de données
	 * 
	 * @param reponse Ligne de la base de données
	 * @param bdd Gestionnaire de la base de données
	 * @return l'objet salle
	 * @throws DatabaseException
	 */
	public T inflateSalle(ResultSet reponse, BddGestion bdd) throws DatabaseException {
		
		try {
			// Informations générales
			int id = reponse.getInt("salle_id");
			String batiment = reponse.getString("salle_batiment");
			String nom = reponse.getString("salle_nom");
			int niveau = reponse.getInt("salle_niveau");
			int numero = reponse.getInt("salle_numero");
			int capacite = reponse.getInt("salle_capacite");
	
			// Récupérer la liste des matériels de la salle avec la quantité
			ResultSet requeteMateriel = bdd.executeRequest(
					"SELECT * "
					+ "FROM edt.contientmateriel "
					+ "INNER JOIN edt.materiel ON materiel.materiel_id = contientmateriel.materiel_id "
					+ "WHERE salle_id =" + id);
			
			ArrayList<Materiel> materiels = new ArrayList<Materiel>();
			while (requeteMateriel.next()) {
				materiels.add(new Materiel(requeteMateriel.getInt("materiel_id"), requeteMateriel.getString("materiel_nom"), requeteMateriel.getInt("contientmateriel_quantite")));
			}
			
			requeteMateriel.close();
			
			T res = inflate(id, batiment, nom, capacite, niveau, numero, materiels, reponse, bdd);
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	protected abstract T inflate(int id, String batiment, String nom, int capacite, int niveau, 
			int numero, ArrayList<Materiel> materiels, ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException;
}
