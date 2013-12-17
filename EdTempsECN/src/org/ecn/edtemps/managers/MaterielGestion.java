package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Materiel;

/**
 * Classe de gestion du matériel
 * 
 * @author Joffrey
 */
public class MaterielGestion {

	/** Gestionnaire de base de données */
	protected BddGestion _bdd;
	
	private static Logger logger = LogManager.getLogger(MaterielGestion.class.getName());

	/**
	 * Initialise un gestionnaire de matériel
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public MaterielGestion(BddGestion bdd) {
		_bdd = bdd;
	}

	/**
	 * Récupérer la liste des matériels dans la base de données
	 * @return la liste de matériel
	 * @throws EdtempsException 
	 */
	public List<Materiel> getListeMateriel() throws EdtempsException {

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère les matériels en base
			ResultSet requeteMateriel= _bdd.executeRequest("SELECT * FROM edt.materiel ORDER BY materiel_nom");
			logger.debug("Récupération de la liste des matériels dans la base de données");

			// Parcours le résultat de la requête
			List<Materiel> listeMateriel = new ArrayList<Materiel>();
			while (requeteMateriel.next()) {
				listeMateriel.add(new Materiel(requeteMateriel.getInt("materiel_id"), requeteMateriel.getString("materiel_nom")));
			}

			// Ferme la requete
			requeteMateriel.close();

			// Termine la transaction
			_bdd.commit();

			return listeMateriel;

		} catch (DatabaseException | SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}


	/**
	 * Enregistrer un matériel en base de données
	 * @param nom Nom du matériel
	 * @return l'identifiant de la ligne insérée
	 * @throws EdtempsException
	 */
	public int sauverMateriel(String nom) throws EdtempsException {

		int idInsertion = -1;
		
		if(StringUtils.isBlank(nom)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un matériel doit avoir un nom");
		}

		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un matériel doit être alphanumérique");
		}
		
		try {
			// Démarre une transaction
			_bdd.startTransaction();

			// Vérifie que le nom n'est pas déjà en base de données
			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT * FROM edt.materiel WHERE materiel_nom=?");
			nomDejaPris.setString(1, nom);
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			if (nomDejaPrisResult.next()) {
				throw new EdtempsException(ResultCode.NAME_TAKEN,
						"Tentative d'enregistrer un matériel en base de données avec un nom déjà utilisé");
			}
			nomDejaPrisResult.close();
			
			// Préparation de la requête
			PreparedStatement requete = _bdd.getConnection().prepareStatement("INSERT INTO edt.materiel (materiel_nom) VALUES (?) RETURNING materiel_id");
			requete.setString(1, nom);

			// Exécute la requête
			ResultSet resultat = requete.executeQuery(); 

			// Récupère l'identifiant de la ligne ajoutée
			resultat.next();
			idInsertion = resultat.getInt(1);
			resultat.close();

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException | SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return idInsertion;
	}


	/**
	 * Supprime un type de matériel en base de données
	 * @param id Identifiant du type de matériel à supprimer
	 * @throws EdtempsException
	 */
	public void supprimerMateriel(int id) throws EdtempsException {

		// Démarre une transaction
		_bdd.startTransaction();

		// Supprime les liens avec les salles
		_bdd.executeUpdate("DELETE FROM edt.contientmateriel WHERE materiel_id=" + id);

		// Supprime le type de matériel
		_bdd.executeUpdate("DELETE FROM edt.materiel WHERE materiel_id=" + id);

		// Termine la transaction
		_bdd.commit();
		
	}

	
}
