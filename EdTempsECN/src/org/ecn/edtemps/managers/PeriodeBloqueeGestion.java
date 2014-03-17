package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
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

	/** Nombre de caractères maximum pour le libellé */
	public static final int TAILLE_MAX_LIBELLE = 50;
	
	/**
	 * Initialise un gestionnaire d'une période bloquée
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public PeriodeBloqueeGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Création d'une période bloquée à partir d'une ligne de base de données
	 * Colonnes nécessaires dans le ResultSet : periodebloquee_id, periodebloquee_libelle, periodebloquee_date_debut,
	 * periodebloquee_date_fin, periodebloquee_vacances, periodebloquee_fermeture
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return PeriodeBloquee créée
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	private PeriodeBloqueeIdentifie inflatePeriodeBloqueeFromRow(ResultSet row) throws SQLException, DatabaseException {
		int id = row.getInt("periodebloquee_id");
		String libelle = row.getString("periodebloquee_libelle");
		Date dateDebut = row.getTimestamp("periodebloquee_date_debut");
		Date dateFin = row.getTimestamp("periodebloquee_date_fin");
		boolean vacances = row.getBoolean("periodebloquee_vacances");
		boolean fermeture = row.getBoolean("periodebloquee_fermeture");
		
		// Récupération des groupes associés
		ResultSet requete = bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant.groupeparticipant_createur" +
					" FROM edt.groupeparticipant" +
					" INNER JOIN edt.periodesbloqueesappartientgroupe ON periodesbloqueesappartientgroupe.groupeparticipant_id = groupeparticipant.groupeparticipant_id" +
					" WHERE periodesbloqueesappartientgroupe.periodebloquee_id=" + id +
					" ORDER BY groupeparticipant.groupeparticipant_nom");
		
		List<GroupeIdentifie> listeGroupes = new ArrayList<GroupeIdentifie>();
		while (requete.next()) {
			listeGroupes.add(new GroupeIdentifieInflater().inflateGroupe(requete, bdd));
		}
		
		requete.close();
		
		return new PeriodeBloqueeIdentifie(id, libelle, dateDebut, dateFin, listeGroupes, vacances, fermeture);
	}
	
	
	/**
	 * Récupérer la liste des périodes bloquées dans la base de données pour une période donnée
	 * 
	 * @param debut Début de la période de recherche
	 * @param fin Fin de la période de recherche
	 * @return la liste des jours bloqués
	 * @throws EdtempsException 
	 */
	public List<PeriodeBloqueeIdentifie> getPeriodesBloquees(Date debut, Date fin) throws DatabaseException {

		try {

			String requeteString = "SELECT periodebloquee_id, periodebloquee_libelle, periodebloquee_date_debut, periodebloquee_date_fin, periodebloquee_vacances, periodebloquee_fermeture" +
					" FROM edt.periodesbloquees" +
					" WHERE periodebloquee_date_debut <= ? AND periodebloquee_date_fin >= ?" +
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
	
	
	/**
	 * Ajouter une période bloquée
	 * 
	 * @param libelle Libellé de la période
	 * @param dateDebut Date de début de la période
	 * @param dateFin Date de fin de la période
	 * @param vacances VRAI si c'est une période de vacances
	 * @param fermeture VRAI si c'est une période de fermeture
	 * @param listeIdGroupe Liste des identifiants des groupes liés à cette période
	 * @throws EdtempsException
	 */
	public void sauverPeriodeBloquee(String libelle, Date dateDebut, Date dateFin, boolean vacances, boolean fermeture, ArrayList<Integer> listeIdGroupe) throws EdtempsException {

		// Quelques vérifications sur l'objet à enregistrer
		if (StringUtils.isEmpty(libelle) || dateDebut==null || dateDebut==null || (vacances && listeIdGroupe.isEmpty()) || (fermeture && !listeIdGroupe.isEmpty()) || (!fermeture && !vacances && listeIdGroupe.isEmpty())) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Les informations sur la période bloquée ne sont pas complètes");
		}
		
		// Vérifie que le libellé est bien alphanumérique
		if(libelle.length() > TAILLE_MAX_LIBELLE || !libelle.matches("^['a-zA-Z \u00C0-\u00FF0-9]+$")) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le libellé d'une période bloquée doit être alphanumérique et de moins de " + TAILLE_MAX_LIBELLE + " caractères");
		}
		
		try {
			
			// Démarre une transaction
			bdd.startTransaction();
			
			// Prépare la requête d'insertion
			PreparedStatement requete = bdd.getConnection().prepareStatement("INSERT INTO edt.periodesbloquees" +
					" (periodebloquee_libelle, periodebloquee_date_debut, periodebloquee_date_fin, periodebloquee_vacances, periodebloquee_fermeture)" +
					" VALUES (?, ?, ?, ?, ?)" +
				    " RETURNING periodebloquee_id");
			requete.setString(1, libelle);
			requete.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
			requete.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
			requete.setBoolean(4, vacances);
			requete.setBoolean(5, fermeture);
			
			// Exécute la requête
			ResultSet ligneCreee = requete.executeQuery();
			logger.info("Sauvegarde d'une période bloquée");
			 
			if (!fermeture) {
				// On récupère l'id de la ligne créée
				ligneCreee.next();
				int idLigneCree = ligneCreee.getInt("periodebloquee_id");
				ligneCreee.close();
				requete.close();
				
				// Requête d'ajout des liens avec les groupes de participants liés
				String strReq = "INSERT INTO edt.periodesbloqueesappartientgroupe (groupeparticipant_id, periodebloquee_id) VALUES";
				for (int i=0; i<listeIdGroupe.size() ; i++) {
					if (i!=0) strReq += ", ";
					strReq += " (" + listeIdGroupe.get(i) + ", " + idLigneCree + ")";
				}
				bdd.executeRequest(strReq);
			}
			
			// Commit la transaction
			bdd.commit();

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
	}
	
	
	/**
	 * Modifier une période bloquée
	 * 
	 * @param id Identifiant de la période à modifier
	 * @param libelle Nouveau libellé
	 * @param dateDebut Nouvelle date de début
	 * @param dateFin Nouvelle date de fin
	 * @param vacances Nouvelle valeur pour vacances
	 * @param fermeture Nouvelle valeur pour fermeture
	 * @param listeIdGroupe Nouvelle liste des identifiants des groupes liés à cette période
	 * @throws EdtempsException
	 */
	public void modifierPeriodeBloquee(int id, String libelle, Date dateDebut, Date dateFin, boolean vacances, boolean fermeture, ArrayList<Integer> listeIdGroupe) throws EdtempsException {

		// Quelques vérifications sur l'objet à enregistrer
		if (StringUtils.isEmpty(libelle) || dateDebut==null || dateDebut==null || (vacances && listeIdGroupe.isEmpty()) || (fermeture && !listeIdGroupe.isEmpty()) || (!fermeture && !vacances && listeIdGroupe.isEmpty())) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Les informations sur la période bloquée ne sont pas complètes");
		}
		
		// Vérifie que le libellé est bien alphanumérique
		if(libelle.length() > TAILLE_MAX_LIBELLE || !libelle.matches("^['a-zA-Z \u00C0-\u00FF0-9]+$")) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le libellé d'une période bloquée doit être alphanumérique et de moins de " + TAILLE_MAX_LIBELLE + " caractères");
		}

		try {
			
			// Démarre une transaction
			bdd.startTransaction();
			
			// Modifie les valeurs de base de la période
			PreparedStatement requete = bdd.getConnection().prepareStatement("UPDATE edt.periodesbloquees" +
					" SET periodebloquee_libelle=?, periodebloquee_date_debut=?, periodebloquee_date_fin=?, periodebloquee_vacances=?, periodebloquee_fermeture=?" +
					" WHERE periodebloquee_id=?");
			requete.setString(1, libelle);
			requete.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
			requete.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
			requete.setBoolean(4, vacances);
			requete.setBoolean(5, fermeture);
			requete.setInt(6, id);
			requete.execute();
			requete.close();
			
			if (!fermeture) {
				// Supprime l'ancienne liste des groupes liés
				bdd.executeUpdate("DELETE FROM edt.periodesbloqueesappartientgroupe WHERE periodebloquee_id=" + id);
				
				// Recréer la liste des groupes liés
				String strReq = "INSERT INTO edt.periodesbloqueesappartientgroupe (groupeparticipant_id, periodebloquee_id) VALUES";
				for (int i=0; i<listeIdGroupe.size() ; i++) {
					if (i!=0) strReq += ", ";
					strReq += " (" + listeIdGroupe.get(i) + ", " + id + ")";
				}
				bdd.executeRequest(strReq);
			}

			// Commit la transaction
			bdd.commit();

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
	}
	

	/**
	 * Supprimer une période bloquée de la base de donnée
	 * 
	 * @param id Identifiant de la période
	 * @throws EdtempsException
	 */
	public void supprimerPeriodeBloquee(int id) throws DatabaseException {

		// Démarre une transaction
		bdd.startTransaction();
		
		bdd.executeUpdate("DELETE FROM edt.periodesbloqueesappartientgroupe WHERE periodebloquee_id=" + id);
		bdd.executeUpdate("DELETE FROM edt.periodesbloquees WHERE periodebloquee_id=" + id);
		
		// Commit la transaction
		bdd.commit();

	}
	
	
}
