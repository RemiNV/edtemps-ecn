package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
	

	/**
	 * Sauver un jour férié dans la base de données
	 * 
	 * @param jour Objet de type JourFerie à enregistrer en base de donnée
	 * @return l'identifiant de la ligne ajoutée
	 * @throws EdtempsException 
	 */
	public int sauverJourFerie(String libelle, Date date) throws EdtempsException {

		// Quelques vérifications sur l'objet à enregistrer
		if (date==null || StringUtils.isEmpty(libelle)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un jour férié doit avoir un libellé et une date");
		}
		
		// Vérfie si un jour identique est déjà présent en base
		if (verifierPresenceJourFerie(date, null)) {
			throw new EdtempsException(ResultCode.DAY_TAKEN, "Il y a déjà un jour férié à cette date dans la base de données");
		}		
		
		try {
						
			// Prépare la requête
			PreparedStatement requete = bdd.getConnection().prepareStatement("INSERT INTO edt.joursferies" +
					" (jourferie_libelle, jourferie_date) " +
					" VALUES (?, ?) " +
				    " RETURNING jourferie_id");
			requete.setString(1, libelle);
			requete.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
			
			// Exécute la requête
			ResultSet ligneCreee = requete.executeQuery();
			logger.info("Sauvegarde d'un jour férié");
			requete.close();
			 
			// On récupère l'id de l'événement créé
			ligneCreee.next();
			int idLigneCree = ligneCreee.getInt("jourferie_id");
			ligneCreee.close();
			
			return idLigneCree;

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
	
	/**
	 * Supprimer un jour férié dans la base de données
	 * 
	 * @param id Identifiant (bdd) du jour férié à supprimer
	 * @throws EdtempsException 
	 */
	public void supprimerJourFerie(int id) throws EdtempsException {

		// Supprime le jour férié de la base de données à partir de l'identifiant
		bdd.executeUpdate("DELETE FROM edt.joursferies WHERE jourferie_id = " + id);
		logger.info("Suppression d'un jour férié");

	}
	
	
	/**
	 * Modifier un jour férié dans la base de données
	 * 
	 * @param jour Objet de type JourFerieIdentifie à modifier en base de donnée
	 * @throws EdtempsException 
	 */
	public void modifierJourFerie(JourFerieIdentifie jour) throws EdtempsException {

		// Quelques vérifications sur l'objet à modifier
		if (jour==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "La méthode pour sauver un jour férié doit recevoir un jour non null en paramètre");
		} else if (jour.getDate()==null || StringUtils.isEmpty(jour.getLibelle())) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un jour férié doit avoir un nom et une date");
		}
		
		// Vérfie si un jour identique est déjà présent en base
		if (verifierPresenceJourFerie(jour.getDate(), jour.getId())) {
			throw new EdtempsException(ResultCode.DAY_TAKEN, "Il y a déjà un jour férié à cette date dans la base de données");
		}		
		
		try {
			
			// Prépare la requête
			PreparedStatement requete = bdd.getConnection().prepareStatement("UPDATE edt.joursferies" +
					" SET jourferie_libelle = ?, " +
					" jourferie_date = ?, " +
					" WHERE jourferie_id = " + jour.getId());
			requete.setString(1, jour.getLibelle());
			requete.setTimestamp(2, new java.sql.Timestamp(jour.getDate().getTime()));

			// Exécute la requête
			requete.execute();
			requete.close();
			logger.info("Modification d'un jour férié");
			
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
	
	/**
	 * Vérifier si un jour férié est présent à une date
	 * Possibilité d'ignorer un jour férié dans la recherche
	 * 
	 * @param date Date du jour à vérifier
	 * @param ignoreId Identifiant du jour à ignorer dans la recherche
	 * @result VRAI s'il existe déjà un jour férié à cette date en base de données
	 * @throws EdtempsException 
	 */
	public boolean verifierPresenceJourFerie(Date date, Integer ignoreId) throws EdtempsException {
		
		if (date==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "La date ne doit pas être nulle");
		}
		
		try {
			// On regarde s'il existe un jour férié dont l'heure est comprise entre 00:00:00 et 23:59:59
			// Pour le jour donné en paramètre
			
			// Prépare un objet date à 00:00:00 du jour
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			java.sql.Timestamp debut = new java.sql.Timestamp(cal.getTime().getTime());
			
			// Prépare un objet date à 23:59:59 du jour
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			java.sql.Timestamp fin = new java.sql.Timestamp(date.getTime());
			
			// Préparation de la requête
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement("SELECT jourferie_id" +
					" FROM edt.joursferies" +
					" WHERE jourferie_date >= ? AND jourferie_date <= ?" + 
					(ignoreId==null ? "" : " AND jourferie_id<>"+ignoreId) );
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(debut.getTime()));
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(fin.getTime()));
			
			// Tente de récupérer le jour en base
			ResultSet requete = requetePreparee.executeQuery();

			// Vérifie si il existe un résultat
			boolean resultat = requete.next();

			// Ferme la requete
			requete.close();

			return resultat;
			
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
}
