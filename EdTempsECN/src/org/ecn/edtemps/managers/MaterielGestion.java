package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	protected BddGestion _bdd;

	private static Logger logger = LogManager.getLogger(MaterielGestion.class.getName());

	/**
	 * Initialise un gestionnaire de matériel
	 * 
	 * @param bdd
	 *            Gestionnaire de base de données à utiliser
	 */
	public MaterielGestion(BddGestion bdd) {
		_bdd = bdd;
	}

	/**
	 * Récupérer la liste des matériels dans la base de données
	 * 
	 * @return la liste de matériel
	 * 
	 * @throws EdtempsException 
	 */
	public List<Materiel> getListeMateriel() throws EdtempsException {

		List<Materiel> listeMateriel = new ArrayList<Materiel>();

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère les matériels en base
			ResultSet requeteMateriel= _bdd.executeRequest("SELECT * FROM edt.materiel");
			logger.debug("Récupération de la liste des matériels dans la base de données");

			// Parcours le résultat de la requête
			while (requeteMateriel.next()) {
				listeMateriel.add(new Materiel(requeteMateriel.getInt("materiel_id"), requeteMateriel.getString("materiel_nom")));
			}

			// Ferme la requete
			requeteMateriel.close();

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return listeMateriel;

	}
	
}
