package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;

/**
 * Classe de gestion de jours fériés
 * 
 * @author Joffrey
 */
public class JourFerieGestion {

	protected BddGestion bdd;
	private static Logger logger = LogManager.getLogger(JourFerieGestion.class.getName());

	
	/**
	 * Initialise un gestionnaire de jours fériés
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public JourFerieGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Création d'un jour férié à partir d'une ligne de base de données.
	 * Colonnes nécessaires dans le ResultSet : jourferie_id, jourferie_libelle, jourferie_date
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return JourFerie créé
	 * @throws SQLException
	 */
	private JourFerieIdentifie inflateJourFerieFromRow(ResultSet row) throws SQLException {
		int id = row.getInt("jourferie_id");
		String libelle = row.getString("jourferie_libelle");
		Date date = row.getDate("jourferie_date");
		
		return new JourFerieIdentifie(id, libelle, date);
	}
	
	
	/**
	 * Récupérer la liste des jours fériés dans la base de données pour une période donnée
	 * 
	 * @param debut Début de la période de recherche
	 * @param fin Fin de la période de recherche
	 * @return la liste des jours fériés
	 * @throws EdtempsException 
	 */
	public List<JourFerieIdentifie> getJoursFeries(Date debut, Date fin) throws EdtempsException {

		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
				
		try {

			String requeteString = "SELECT jourferie_id, jourferie_libelle, jourferie_date" +
					" FROM edt.joursferies" +
					" WHERE jourferie_date >= ? AND jourferie_date <= ?" +
					" ORDER BY jourferie_date";
			
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(requeteString);
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(debut.getTime()));
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(fin.getTime()));
			
			
			// Récupère les jours en base
			ResultSet requete = requetePreparee.executeQuery();
			logger.info("Récupération de la liste des jours fériés dans la base de données, pour la période du "+debut+" au "+fin);

			// Parcours le résultat de la requête
			List<JourFerieIdentifie> listeJours = new ArrayList<JourFerieIdentifie>();
			while (requete.next()) {
				listeJours.add(this.inflateJourFerieFromRow(requete));
			}

			// Ferme la requete
			requete.close();

			return listeJours;

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
}
