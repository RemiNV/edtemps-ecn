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
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.models.identifie.PeriodeBloqueeIdentifie;
import org.ecn.edtemps.models.inflaters.GroupeIdentifieInflater;

/**
 * Classe de gestion d'une période bloquée
 * 
 * @author Joffrey
 */
public class PeriodeBloqueeGestion {

	protected BddGestion bdd;
	private static Logger logger = LogManager.getLogger(PeriodeBloqueeGestion.class.getName());

	
	/**
	 * Initialise un gestionnaire d'une période bloquée
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public PeriodeBloqueeGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Création d'une période bloquée à partir d'une ligne de base de données
	 * Colonnes nécessaires dans le ResultSet : periodebloquee_id, periodebloquee_libelle, periodebloquee_date_debut, periodebloquee_date_fin
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return PeriodeBloquee créée
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	private PeriodeBloqueeIdentifie inflatePeriodeBloqueeFromRow(ResultSet row) throws SQLException, DatabaseException {
		int id = row.getInt("periodebloquee_id");
		String libelle = row.getString("periodebloquee_libelle");
		Date dateDebut = row.getDate("periodebloquee_date_debut");
		Date dateFin = row.getDate("periodebloquee_date_fin");
		boolean vacances = row.getBoolean("periodebloquee_vacances");
		
		// Récupération des groupes associés
		ResultSet requete = bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant.groupeparticipant_createur" +
					" FROM edt.groupeparticipant" +
					" INNER JOIN edt.periodesbloqueesappartientgroupe ON periodesbloqueesappartientgroupe.groupeparticipant_id = groupeparticipant.groupeparticipant_id" +
					" ORDER BY groupeparticipant.groupeparticipant_nom");
		
		List<GroupeIdentifie> listeGroupes = new ArrayList<GroupeIdentifie>();
		while (requete.next()) {
			listeGroupes.add(new GroupeIdentifieInflater().inflateGroupe(requete, bdd));
		}
		
		requete.close();
		
		return new PeriodeBloqueeIdentifie(id, libelle, dateDebut, dateFin, listeGroupes, vacances);
	}
	
	
	/**
	 * Récupérer la liste des périodes bloquées dans la base de données pour une période donnée
	 * Trois possibilité :
	 * 		- récupération des vacances (vacances=true)
	 * 		- récupération des jours bloqués non vacances (vacances=false)
	 * 		- récupération de tous les jours bloqués (vacances=null)
	 * 
	 * @param debut Début de la période de recherche
	 * @param fin Fin de la période de recherche
	 * @param vacances Si non null, inclus uniquement les vacances (true) ou les non-vacances (false)
	 * @return la liste des jours bloqués
	 * @throws EdtempsException 
	 */
	public List<PeriodeBloqueeIdentifie> getPeriodesBloquees(Date debut, Date fin, Boolean vacances) throws DatabaseException {

		try {

			String requeteString = "SELECT periodebloquee_id, periodebloquee_libelle, periodebloquee_date_debut, periodebloquee_date_fin, periodebloquee_vacances" +
					" FROM edt.periodesbloquees" +
					" WHERE periodebloquee_date_debut <= ? AND periodebloquee_date_fin >= ?" +
					(vacances==null ? "" : " AND periodebloquee_vacances = "+vacances) + 
					" ORDER BY periodebloquee_date_debut";
			
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(requeteString);
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(fin.getTime()));
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(debut.getTime()));
			
			// Récupère les périodes en base
			ResultSet requete = requetePreparee.executeQuery();
			logger.info("Récupération de la liste des périodes bloquées dans la base de données, pour la période du "+debut+" au "+fin);

			// Parcours le résultat de la requête
			List<PeriodeBloqueeIdentifie> listeJours = new ArrayList<PeriodeBloqueeIdentifie>();
			while (requete.next()) {
				listeJours.add(this.inflatePeriodeBloqueeFromRow(requete));
			}

			// Ferme la requete
			requete.close();

			return listeJours;

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

	}
	
}
