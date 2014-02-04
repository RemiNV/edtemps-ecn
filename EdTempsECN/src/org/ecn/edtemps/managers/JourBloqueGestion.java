package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.models.identifie.JourBloqueIdentifie;
import org.ecn.edtemps.models.inflaters.GroupeIdentifieInflater;

/**
 * Classe de gestion de jours bloqués
 * 
 * @author Joffrey
 */
public class JourBloqueGestion {

	protected BddGestion bdd;
	private static Logger logger = LogManager.getLogger(JourBloqueGestion.class.getName());

	
	/**
	 * Initialise un gestionnaire de jours bloqués
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public JourBloqueGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Création d'un jour bloqué à partir d'une ligne de base de données
	 * Colonnes nécessaires dans le ResultSet : jourbloque_id, jourbloque_libelle, jourbloque_date_debut, jourbloque_date_fin
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return JourBloque créé
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	private JourBloqueIdentifie inflateJourBloqueFromRow(ResultSet row) throws SQLException, DatabaseException {
		int id = row.getInt("jourbloque_id");
		String libelle = row.getString("jourbloque_libelle");
		Date dateDebut = row.getDate("jourbloque_date_debut");
		Date dateFin = row.getDate("jourbloque_date_fin");
		boolean vacances = row.getBoolean("jourbloque_vacances");
		
		// Récupération des groupes associés
		ResultSet requete = bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant.groupeparticipant_createur" +
					" FROM edt.groupeparticipant" +
					" INNER JOIN edt.joursbloquesappartientgroupe ON joursbloquesappartientgroupe.groupeparticipant_id = groupeparticipant.groupeparticipant_id" +
					" ORDER BY groupeparticipant.groupeparticipant_nom");
		
		List<GroupeIdentifie> listeGroupes = new ArrayList<GroupeIdentifie>();
		while (requete.next()) {
			listeGroupes.add(new GroupeIdentifieInflater().inflateGroupe(requete, bdd));
		}
		
		requete.close();
		
		return new JourBloqueIdentifie(id, libelle, dateDebut, dateFin, listeGroupes, vacances);
	}
	
	
	/**
	 * Récupérer la liste des jours bloqués dans la base de données pour une période donnée
	 * Trois possibilité :
	 * 		- récupération des vacances (vacances=true)
	 * 		- récupération des jours bloqués non vacances (vacances=false)
	 * 		- récupération de tous les jours bloqués (vacances=null)
	 * 
	 * @param debut Début de la période de recherche
	 * @param fin Fin de la période de recherche
	 * @param vacances
	 * @return la liste des jours bloqués
	 * @throws EdtempsException 
	 */
	public List<JourBloqueIdentifie> getJoursBloques(Date debut, Date fin, Boolean vacances) throws EdtempsException {

		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}

		try {

			String requeteString = "SELECT jourbloque_id, jourbloque_libelle, jourbloque_date_debut, jourbloque_date_fin, jourbloque_vacances" +
					" FROM edt.joursbloques" +
					" WHERE jourbloque_date_debut >= ? AND jourbloque_date_debut <= ?" +
					" OR jourbloque_date_fin >= ? AND jourbloque_date_fin <= ?" +
					(vacances==null ? "" : " AND jourbloque_vacances = "+vacances) + 
					" ORDER BY jourbloque_date_debut";
			
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(requeteString);
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(debut.getTime()));
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(fin.getTime()));
			requetePreparee.setTimestamp(3, new java.sql.Timestamp(debut.getTime()));
			requetePreparee.setTimestamp(4, new java.sql.Timestamp(fin.getTime()));
			
			// Récupère les jours en base
			ResultSet requete = requetePreparee.executeQuery();
			logger.info("Récupération de la liste des jours bloqués dans la base de données, pour la période du "+debut+" au "+fin);

			// Parcours le résultat de la requête
			List<JourBloqueIdentifie> listeJours = new ArrayList<JourBloqueIdentifie>();
			while (requete.next()) {
				listeJours.add(this.inflateJourBloqueFromRow(requete));
			}

			// Ferme la requete
			requete.close();

			return listeJours;

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
}
