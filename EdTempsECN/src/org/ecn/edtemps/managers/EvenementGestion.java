package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.MaxRowCountExceededException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.TestRepetitionEvenement;
import org.ecn.edtemps.models.TestRepetitionEvenement.Probleme;
import org.ecn.edtemps.models.TestRepetitionEvenement.ProblemeStatus;
import org.ecn.edtemps.models.identifie.EvenementComplet;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;
import org.ecn.edtemps.models.identifie.PeriodeBloqueeIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.inflaters.AbsEvenementInflater;
import org.ecn.edtemps.models.inflaters.EvenementCompletInflater;
import org.ecn.edtemps.models.inflaters.EvenementIdentifieInflater;

/** 
 * Classe de gestion des événements
 * 
 * @author Maxime TERRADE
 */
public class EvenementGestion {

	/** Gestionnaire de base de données */
	protected BddGestion _bdd;
	
	/** Nombre maximum d'événements récupérables en une requête */
	public static final int MAX_ROWS_QUERY_EVENEMENTS = 500;
	
	/** Nombre de caractères maximum pour le nom d'un événement */
	public static final int TAILLE_MAX_NOM_EVENEMENT = 50;
	
	/** Nombre de maximum de répétition d'un événement pour une demande */
	public static final int MAX_REPETITIONS = 50;
	
	/** Maximum de créneaux examinés pour mettre en place une répétition d'événement */
	public static final int MAX_RECHERCHE_REPETITIONS = 75;
	
	/** Maximum d'événements par utilisateur et par calendrier par semaine glissante */
	public static final int MAX_EVENEMENTS_UTILISATEUR_PAR_CALENDRIER_SEMAINE = 20; 
	
	/**
	 * Initialise un gestionnaire d'evenements
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public EvenementGestion(BddGestion bdd) {
		_bdd = bdd;
	}
		
	
	/**
	 * Vérifie la validité d'une date selon certains critères :
	 * 	- date < aujourd'hui + 2 ans
	 *  - date > aujourd'hui - 1 an
	 * @param date Date à tester
	 * @throws EdtempsException
	 */
	protected void verifierDateEvenement(Date date) throws EdtempsException {
		Date dansDeuxAns = new Date();
		long millisUnAn = 1000L * 3600L * 24L * 365L;
		dansDeuxAns.setTime(dansDeuxAns.getTime() + millisUnAn * 2L);
		if(date.after(dansDeuxAns)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Impossible de créer un événement dans plus de 2 ans");
		}
		
		Date ilYAUnAn = new Date();
		ilYAUnAn.setTime(ilYAUnAn.getTime() - millisUnAn);
		if(date.before(ilYAUnAn)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Impossible de créer un événement il y a plus d'un an");
		}
	}
	
	
	/**
	 * Méthode d'enregistrement d'un evenement dans la base de données
	 * @param nom Nom de l'événement
	 * @param dateDebut Date de début
	 * @param dateFin Date de fin
	 * @param idCalendriers Liste des identifiants des calendriers auxquels l'événement est rattaché
	 * @param idCreateur Identifiant du créateur
	 * @param idSalles Liste des identifiants des salles
	 * @param idIntervenants Liste des identifiants des intervenants
	 * @param idResponsables Liste des identifiants des responsables
	 * @param startTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @throws EdtempsException
	 */
	public void sauverEvenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, Integer idCreateur, List<Integer> idSalles, 
			List<Integer> idIntervenants, List<Integer> idResponsables, boolean startTransaction) throws EdtempsException {
		
		if(StringUtils.isBlank(nom) || idCalendriers.isEmpty() || idResponsables.isEmpty() || idCreateur==null) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un événement doit avoir un nom, un calendrier et un responsable et un créateur");
		}
		
		if(dateDebut.after(dateFin)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "La date de début doit être inférieure à celle de fin");
		}

		verifierDateEvenement(dateDebut);
		
		if(nom.length() > TAILLE_MAX_NOM_EVENEMENT || !StringUtils.isAlphanumericSpace(nom)) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un événement doit être alphanumérique et de moins de " + TAILLE_MAX_NOM_EVENEMENT + " caractères");
		}
		
		// Vérification des quotas de l'utilisateur
		Date dateDebutFenetreQuota = new Date(dateDebut.getTime() - (1000 * 3600 * 84)); // -3.5 jours (84h)
		Date dateFinFenetreQuota = new Date(dateDebut.getTime() + (1000 * 3600 * 84)); // +3.5 jours (84h)
		
		if(getMaxNbEvenementsParCalendriersUtilisateur(idCreateur, idCalendriers, dateDebutFenetreQuota, dateFinFenetreQuota) 
				>= MAX_EVENEMENTS_UTILISATEUR_PAR_CALENDRIER_SEMAINE) {
			throw new EdtempsException(ResultCode.QUOTA_EXCEEDED, "Vous ne pouvez pas créer plus de " + 
				MAX_EVENEMENTS_UTILISATEUR_PAR_CALENDRIER_SEMAINE + " événements par calendrier et par semaine glissante");
		}
		
		try {
			
			if(startTransaction) {
				_bdd.startTransaction();
			}
			
			// Vérification de la disponibilité de la salle
			if(!idSalles.isEmpty()) {
				SalleGestion salleGestion = new SalleGestion(_bdd);
				if(!salleGestion.sallesLibres(idSalles, dateDebut, dateFin)) {
					throw new EdtempsException(ResultCode.SALLE_OCCUPEE, "Une des salles demandées n'est pas/plus libre");
				}
			}
			
			// On crée l'événement dans la base de données
			PreparedStatement req = _bdd.getConnection().prepareStatement("INSERT INTO edt.evenement "
					+ "(eve_nom, eve_dateDebut, eve_dateFin, eve_createur) "
					+ "VALUES (?, ?, ?, ?) "
				    + "RETURNING eve_id");
			req.setString(1, nom);
			req.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
			req.setInt(4, idCreateur);
			
			ResultSet rsLigneCreee = req.executeQuery();
			 
			// On récupère l'id de l'evenement créé
			rsLigneCreee.next();
			int idEvenement = rsLigneCreee.getInt("eve_id");
			rsLigneCreee.close();
			
			// On rattache l'evenement aux calendriers
			for(int idCalendrier : idCalendriers) {
				_bdd.executeUpdate(
					"INSERT INTO edt.evenementappartient "
					+ "(eve_id, cal_id) "
					+ "VALUES (" + idEvenement + ", " + idCalendrier + ")"
					);
			}
			
			// On rattache l'evenement aux salles
			for(int idSalle : idSalles) {
				_bdd.executeUpdate(
					"INSERT INTO edt.alieuensalle "
					+ "(eve_id, salle_id) "
					+ "VALUES ("+ idEvenement + ", " + idSalle + ")");
			}
			
			// On indique le(s) responsable(s) dans la base
			for(int idResponsable : idResponsables) {
				_bdd.executeUpdate(
					"INSERT INTO edt.responsableevenement "
					+ "(eve_id, utilisateur_id) "
					+ "VALUES ("+ idEvenement + ", " + idResponsable + ")");
			}
			
			// On indique le(s) intervenant(s) dans la base
			for(int idIntervenant : idIntervenants) {
				_bdd.executeUpdate(
					"INSERT INTO edt.intervenantevenement "
					+ "(eve_id, utilisateur_id) "
					+ "VALUES ("+ idEvenement + ", " + idIntervenant + ")");
			}
			
			if(startTransaction) {
				_bdd.commit();
			}
		}
		catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
			
	}
	
	
	/**
	 * Modification d'un événement existant (en base de données)
	 * 
	 * <p>Permet d'actualiser dans la base de données les anciens attributs d'un événements (date, nom, intervenant, ...)
	 * avec de nouveaux ayant été modifiés par un utilisateur</p>
	 * 
	 * @param id Identifiant de l'événement à modifier
	 * @param nom Nom de l'événement
	 * @param dateDebut Date de début
	 * @param dateFin Date de fin
	 * @param idCalendriers Liste des identifiants des calendriers auxquels l'événement est rattaché
	 * @param idSalles Liste des identifiants des salles
	 * @param idIntervenants Liste des identifiants des intervenants
	 * @param idResponsables Liste des identifiants des responsables
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @throws EdtempsException
	 */
	public void modifierEvenement(int id, String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<Integer> idSalles, 
			List<Integer> idIntervenants, List<Integer> idResponsables, boolean createTransaction) throws EdtempsException{
		
		verifierDateEvenement(dateDebut);
		
		try {
			
			if(StringUtils.isBlank(nom) || idCalendriers.isEmpty() || idResponsables.isEmpty()) {
				throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un événement doit avoir un nom, un calendrier et un responsable");
			}
			
			if(nom.length() > TAILLE_MAX_NOM_EVENEMENT || !StringUtils.isAlphanumericSpace(nom)) {
				throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un événement doit être alphanumérique et de moins de " + TAILLE_MAX_NOM_EVENEMENT + " caractères");
			}
			
			//début d'une transaction si requis
			if (createTransaction){
				_bdd.startTransaction();
			}
			
			// Vérification de la disponibilité de la salle
			if(!idSalles.isEmpty()) {
				SalleGestion salleGestion = new SalleGestion(_bdd);
				if(!salleGestion.sallesLibres(idSalles, dateDebut, dateFin, id)) {
					throw new EdtempsException(ResultCode.SALLE_OCCUPEE, "Une des salles demandées n'est pas/plus libre");
				}
			}
			
			// Modifier l'évenement (nom, date début, date fin)
			PreparedStatement requetePreparee = _bdd.getConnection().prepareStatement(
					"UPDATE edt.evenement "
					+ "SET eve_nom = ?, "
					+ "eve_datedebut = ?, "
					+ "eve_datefin = ? "
					+ "WHERE eve_id = " + id);
			requetePreparee.setString(1, nom);
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
			requetePreparee.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
			requetePreparee.execute();
			
			// Modifier  les intervenants de l'évenement (supprimer les anciens puis ajouter les nouveaux)
			_bdd.executeUpdate(
					"DELETE FROM edt.intervenantevenement "
					 + "WHERE eve_id = " + id);
			
			PreparedStatement addIntervenantStatement = _bdd.getConnection().prepareStatement(
					"INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id) VALUES(?, " + id + ")");
			
			for(int idIntervenant : idIntervenants) {
				
				addIntervenantStatement.setInt(1, idIntervenant);
				addIntervenantStatement.execute();
			}
			
			// Modifier  les responsables de l'événement
			_bdd.executeUpdate(
					"DELETE FROM edt.responsableevenement "
					 + "WHERE eve_id = " + id);
			
			PreparedStatement addResponsableStatement = _bdd.getConnection().prepareStatement(
					"INSERT INTO edt.responsableevenement(utilisateur_id, eve_id) VALUES(?," + id + ")");
			
			for (int idResponsable : idResponsables){
				addResponsableStatement.setInt(1, idResponsable);
				addResponsableStatement.execute();
			}
			
			// Modifier les calendriers associés à l'événement
			_bdd.executeUpdate(
					"DELETE FROM edt.evenementappartient "
					+ "WHERE eve_id = " + id);
			
			PreparedStatement addCalendrierStatement = _bdd.getConnection().prepareStatement(
					"INSERT INTO edt.evenementappartient(cal_id, eve_id) VALUES(?," + id + ")");
			
			for (int idCalendrier : idCalendriers){
				addCalendrierStatement.setInt(1, idCalendrier);
				addCalendrierStatement.execute();
			}
			
			// Modifier les salles de l'évènement
			_bdd.executeUpdate(
					"DELETE FROM edt.alieuensalle "
					+ "WHERE eve_id = " + id);
			
			PreparedStatement addSalleStatement = _bdd.getConnection().prepareStatement(
					"INSERT INTO edt.alieuensalle(salle_id, eve_id) VALUES(?," + id + ")");
			
			for (int idSalle : idSalles){
				addSalleStatement.setInt(1, idSalle);
				addSalleStatement.execute();
			}
			
			// fin transaction si requis
			if (createTransaction){
				_bdd.commit();
			}
			
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	/**
	 * Suppression d'un évènement
	 * 
	 * <p>Permet de supprimer un évènement dans la base de données,
	 * l'évènement est identifié par son ID entier</p>
	 * 
	 * @param idEvenement identifiant de l'événement
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @throws EdtempsException
	 */
	public void supprimerEvenement(int idEvenement, boolean createTransaction) throws DatabaseException {
	
		// Début transaction si nécessaire
		if (createTransaction) {
			_bdd.startTransaction();
		}
		
		// Supprimer l'association aux intervenants de l'événement
		_bdd.executeUpdate(
				"DELETE FROM edt.intervenantevenement "
				 + "WHERE eve_id = " + idEvenement);
		
		// Supprimer l'association aux responsables de l'évenement
		_bdd.executeUpdate(
				"DELETE FROM edt.responsableevenement "
				 + "WHERE eve_id = " + idEvenement);
		
		// Supprimer l'asosciation aux salles de l'événement
		_bdd.executeUpdate(
				"DELETE FROM edt.alieuensalle "
				 + "WHERE eve_id = " + idEvenement);
		
		// Supprimer l'association aux calendriers
		_bdd.executeUpdate(
				"DELETE FROM edt.evenementappartient "
				 + "WHERE eve_id = " + idEvenement);
		
		// Supprimer l'évenement
		_bdd.executeUpdate(
				"DELETE FROM edt.evenement "
				 + "WHERE eve_id = " + idEvenement);

		
		// fin transaction si nécessaire
		if (createTransaction){
			_bdd.commit();
		}
	}
	
	
	/**
	 * Récupération d'un évènement en base
	 * @param idEvenement ID de l'évènement à récupérer
	 * @return Evènement récupéré
	 * @throws DatabaseException
	 */
	public EvenementIdentifie getEvenement(int idEvenement) throws DatabaseException {
		ResultSet reponse = _bdd.executeRequest(
				"SELECT eve_id, eve_nom, eve_datedebut, eve_datefin, evenement.eve_createur "
				+ "FROM edt.evenement "
				+ "WHERE eve_id=" + idEvenement);
		
		EvenementIdentifie res = null;
		try {
			if(reponse.next()) {
				res = new EvenementIdentifieInflater().inflateEvenement(reponse, _bdd);
			}
			reponse.close();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
			
		return res;
	}
	
	
	/**
	 * Liste les évènements auxquels un utilisateur est abonné par l'intermédiaire de ses abonnements aux groupes, et donc aux calendriers.
	 * Le nombre d'événements est limité à MAX_ROWS_QUERY_EVENEMENTS.
	 * @param idUtilisateur Utilisateur dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 * @throws MaxRowCountExceededException nombre de résultats trop important
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsUtilisateur(int idUtilisateur, Date dateDebut, Date dateFin, 
			boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException, MaxRowCountExceededException {
		return listerEvenementsUtilisateur(idUtilisateur, dateDebut, dateFin, createTransaction, reuseTempTableAbonnements, MAX_ROWS_QUERY_EVENEMENTS);
	}
	
	
	/**
	 * Liste les évènements auxquels un utilisateur est abonné par l'intermédiaire de ses abonnements aux groupes, et donc aux calendriers
	 * @param idUtilisateur Utilisateur dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * @param maxNbEvenements Nombre maximum d'événements à renvoyer
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 * @throws MaxRowCountExceededException nombre de résultats trop important
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsUtilisateur(int idUtilisateur, Date dateDebut, Date dateFin, 
			boolean createTransaction, boolean reuseTempTableAbonnements, int maxNbEvenements) throws DatabaseException, MaxRowCountExceededException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		if(createTransaction)
			_bdd.startTransaction();
		
		if(!reuseTempTableAbonnements) {
			GroupeGestion.makeTempTableListeGroupesAbonnement(_bdd, idUtilisateur);
		}
		
		String req = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
				"FROM edt.evenement " +
				"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
				"INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
				"INNER JOIN " + GroupeGestion.NOM_TEMPTABLE_ABONNEMENTS + " abonnements ON abonnements.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
				"WHERE evenement.eve_datefin >= ? AND evenement.eve_datedebut <= ?";
		
		res = listerEvenements(req, dateDebut, dateFin, new EvenementIdentifieInflater(), false, maxNbEvenements);
		
		if(createTransaction)
			_bdd.commit();
		
		return res;
	}
	
	
	/**
	 * Liste les évènements liés à un groupe d'utilisateurs. <b>Les événements des groupes parents et enfants sont aussi renvoyés.</b>
	 * Le nombre d'événements est limité à MAX_ROWS_QUERY_EVENEMENTS
	 * @param idGroupe groupe dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException 
	 * @throws MaxRowCountExceededException Nombre d'événements supérieur à MAX_ROWS_QUERY_EVENEMENTS
	 */
	public ArrayList<EvenementComplet> listerEvenementsGroupe(int idGroupe, Date dateDebut, Date dateFin, boolean createTransaction) throws DatabaseException, MaxRowCountExceededException {
		
		if(createTransaction) {
			_bdd.startTransaction();
		}
		
		GroupeGestion.makeTempTableListeParentsEnfants(_bdd, idGroupe);

		ArrayList<EvenementComplet> res = listerEvenementsGroupesTempTable(GroupeGestion.NOM_TEMPTABLE_PARENTSENFANTS, dateDebut, dateFin);
		
		GroupeGestion.dropTempTableListeParentsEnfants(_bdd);
		
		if(createTransaction) {
			_bdd.commit();
		}
		
		return res;
	}
	
	/**
	 * Liste les événements liés aux groupes présents dans une table temporaire.
	 * Cette méthode doit être appelée dans une transaction déjà ouverte, puisqu'une table temporaire doit être présente.
	 * @param nomTempTable Nom de la table temporaire contenant les groupes
	 * @param dateDebut Date de début de la fenêtre de recherche
	 * @param dateFin Date de fin de la fenêtre de recherche
	 * @return Liste d'événements récupérés
	 * @throws MaxRowCountExceededException Nombre d'événements supérieur à MAX_ROWS_QUERY_EVENEMENTS
	 * @throws DatabaseException 
	 */
	public ArrayList<EvenementComplet> listerEvenementsGroupesTempTable(String nomTempTable, Date dateDebut, Date dateFin) throws DatabaseException, MaxRowCountExceededException {
		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
			"FROM edt.evenement " +
			"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
			"INNER JOIN edt.calendrierappartientgroupe cap ON cap.cal_id = evenementappartient.cal_id " +
			"INNER JOIN " + nomTempTable + " tmpParentsEnfants ON tmpParentsEnfants.groupeparticipant_id = cap.groupeparticipant_id "
			+ "WHERE evenement.eve_datefin >= ? AND evenement.eve_datedebut <= ?";

		return listerEvenements(request, dateDebut, dateFin, new EvenementCompletInflater(), false, MAX_ROWS_QUERY_EVENEMENTS);
	}
	
	/**
	 * Méthode de listing des évènements de cours ou pas de cours (exclusivement l'un ou l'autre) d'une salle
	 * @param idSalle ID de la salle en question
	 * @param dateDebut Date de début de la fenêtre de recherche d'évènements
	 * @param dateFin Date de fin de la fenêtre de recherche d'évènements
	 * @param estCours Lister uniquement les évènements qui sont des cours (true) ou uniquement ceux qui n'en sont pas (false)
	 * @param createTransaction Indique si il faut créer une transaction dans cette méthode, ou si elle sera déjà appelée dans une transaction
	 * @return Liste des évènements trouvés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsSalleCoursOuPas(int idSalle, Date dateDebut, Date dateFin, boolean estCours, boolean createTransaction) throws EdtempsException {
		ArrayList<Integer> idsSalles = new ArrayList<Integer>(1);
		idsSalles.add(idSalle);
		return listerEvenementsSalleCoursOuPas(idsSalles, dateDebut, dateFin, estCours, createTransaction);
	}
	
	
	/**
	 * Méthode de listing des évènements de cours ou pas de cours (exclusivement l'un ou l'autre) d'un ensemble de salles
	 * @param idSalles ID des salles en question
	 * @param dateDebut Date de début de la fenêtre de recherche d'évènements
	 * @param dateFin Date de fin de la fenêtre de recherche d'évènements
	 * @param estCours Lister uniquement les évènements qui sont des cours (true) ou uniquement ceux qui n'en sont pas (false)
	 * @param createTransaction Indique si il faut créer une transaction dans cette méthode, ou si elle sera déjà appelée dans une transaction
	 * @return Liste des évènements trouvés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsSalleCoursOuPas(ArrayList<Integer> idSalles, Date dateDebut, Date dateFin, boolean estCours, boolean createTransaction) 
			throws DatabaseException {
		
		if(idSalles.size() == 0) { // Aucune salle, donc aucun événement
			return new ArrayList<EvenementIdentifie>(0);
		}
		
		String strIdsSalles = StringUtils.join(idSalles, ",");
		
		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
				"FROM edt.evenement " +
				"INNER JOIN edt.alieuensalle ON evenement.eve_id = alieuensalle.eve_id " +
				"LEFT JOIN edt.evenementappartient ON evenementappartient.eve_id = evenement.eve_id " +
				"LEFT JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) " +
				"WHERE evenement.eve_datefin >= ? "
				+ "AND evenement.eve_datedebut <= ? "
				+ "AND alieuensalle.salle_id IN (" + strIdsSalles + ") "
				+ "GROUP BY evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
				"HAVING COUNT(groupecours.groupeparticipant_id)" + (estCours ? " > 0" : " = 0");
		ArrayList<EvenementIdentifie> res = listerEvenements(request, dateDebut, dateFin, new EvenementIdentifieInflater(), createTransaction);
		return res;
	}
	
	
	/**
	 * Méthode générique de listing d'évènements complets ou incomplets d'une salle
	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @parm inflater Inflater permettant de créer l'objet voulu à partir des lignes de base de données
	 * @return Liste des évènements trouvés
	 * @throws EdtempsException 
	 */
	protected <T extends EvenementIdentifie> ArrayList<T> listerEvenementsSalle(int idSalle, Date dateDebut, Date dateFin, 
			boolean createTransaction, AbsEvenementInflater<T> inflater) throws DatabaseException {
		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
				"FROM edt.evenement " +
				"INNER JOIN edt.alieuensalle ON evenement.eve_id = alieuensalle.eve_id " +
				"WHERE alieuensalle.salle_id = " + idSalle +" "
				+ "AND evenement.eve_datefin >= ? "
				+ "AND evenement.eve_datedebut <= ?";
		ArrayList<T> res = listerEvenements(request, dateDebut, dateFin, inflater, createTransaction);
		return res;
	}
	
	
	/**
	 * Liste les évènements liés à une salle, sous forme d'évènement complet
	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementComplet> listerEvenementCompletsSalle(int idSalle, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		return listerEvenementsSalle(idSalle, dateDebut, dateFin, createTransaction, new EvenementCompletInflater());
	}
	
	
	/**
	 * Liste les évènements liés à une salle, sous forme d'évènement identifié
	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementIdentifie> listerEvenementIdentifiesSalle(int idSalle, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		return listerEvenementsSalle(idSalle, dateDebut, dateFin, createTransaction, new EvenementIdentifieInflater());
	}
	
	
	/**
	 * Liste les évènements liés à un intervenant
	 * @param idIntervenant identifiant de l'utilisateur intervenant dans les évènements à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementComplet> listerEvenementsIntervenant(int idIntervenant, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
				"FROM edt.evenement " +
				"INNER JOIN edt.intervenantevenement ON evenement.eve_id = intervenantevenement.eve_id " +
				"WHERE intervenantevenement.utilisateur_id = " + idIntervenant + " "
				+ "AND evenement.eve_datefin >= ? "
				+ "AND evenement.eve_datedebut <= ?";
		ArrayList<EvenementComplet> res = listerEvenements(request, dateDebut, dateFin, new EvenementCompletInflater(), createTransaction);
		return res;
	}
	
	
	/**
	 * Liste les évènements liés à un calendrier
	 * @param idCalendrier identifiant du calendrier dont les évènements sont à récupérer
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws EdtempsException 
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsCalendrier(int idCalendrier, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin, evenement.eve_createur " +
				"FROM edt.evenement " +
				"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
				"WHERE evenementappartient.cal_id = " + idCalendrier + " "
				+ "AND evenement.eve_datefin >= ? "
				+ "AND evenement.eve_datedebut <= ?";
		return listerEvenements(request, dateDebut, dateFin, new EvenementIdentifieInflater(), createTransaction);
	}
	
	/**
	 * Liste les événements de l'ensemble des groupes auxquels est rattaché le calendrier demandé
	 * @param idCalendrier ID du calendrier en question
 	 * @param dateDebut Date de début de la fenêtre de recherche
	 * @param dateFin Date de fin de la fenêtre de recherche
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode ; sinon la méthode <b>doit</b> être appelée dans une transaction
	 * @return Liste d'événements récupérés (valeurs) pour chaque groupe (ID du groupe en clé). Pas d'entrée si liste d'événements vide pour un groupe.
	 * @throws DatabaseException 
	 * @throws MaxRowCountExceededException 
	 */
	public Hashtable<Integer, ArrayList<EvenementComplet>> listerEvenementsGroupesCalendrier(int idCalendrier, Date dateDebut, Date dateFin, boolean createTransaction) 
			throws DatabaseException, MaxRowCountExceededException {
		
		ArrayList<Integer> ids = new ArrayList<Integer>(1);
		ids.add(idCalendrier);
		
		return listerEvenementsGroupesCalendrier(ids, dateDebut, dateFin, createTransaction);
	}
	
	/**
	 * Liste les événements de l'ensemble des groupes auxquels sont rattachés les calendriers demandés
	 * @param idCalendriers ID des calendriers en question
 	 * @param dateDebut Date de début de la fenêtre de recherche
	 * @param dateFin Date de fin de la fenêtre de recherche
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode ; sinon la méthode <b>doit</b> être appelée dans une transaction
	 * @return Liste d'événements récupérés (valeurs) pour chaque groupe (ID du groupe en clé). Pas d'entrée si liste d'événements vide pour un groupe.
	 * @throws DatabaseException 
	 * @throws MaxRowCountExceededException 
	 */
	public Hashtable<Integer, ArrayList<EvenementComplet>> listerEvenementsGroupesCalendrier(List<Integer> idCalendriers, Date dateDebut, Date dateFin, boolean createTransaction) 
			throws DatabaseException, MaxRowCountExceededException {
		
		Hashtable<Integer, ArrayList<EvenementComplet>> res = new Hashtable<Integer, ArrayList<EvenementComplet>>();
		
		if(idCalendriers.size() == 0) {
			return res; // Résultat vide
		}
		
		if(createTransaction) {
			_bdd.startTransaction();
		}
		
		// Récupération des groupes de ces calendriers
		ArrayList<Integer> idGroupes = new ArrayList<Integer>();
		String strIds = StringUtils.join(idCalendriers, ",");
		ResultSet reponse = _bdd.executeRequest("SELECT cap.groupeparticipant_id FROM edt.calendrierappartientgroupe cap WHERE cap.cal_id IN (" + strIds + ")");
		
		try {
			while(reponse.next()) {
				idGroupes.add(reponse.getInt(1));
			}
			reponse.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		// Récupération des événements pour chaque groupe (permet de repérer facilement quel événement récupéré appartient à quel groupe)
		for(int idGroupe : idGroupes) {
			GroupeGestion.makeTempTableListeParentsEnfants(_bdd, idGroupe);
			
			ArrayList<EvenementComplet> evens = listerEvenementsGroupesTempTable(GroupeGestion.NOM_TEMPTABLE_PARENTSENFANTS, dateDebut, dateFin);
			
			if(evens.size() > 0) {
				res.put(idGroupe, evens);
			}
			
			GroupeGestion.dropTempTableListeParentsEnfants(_bdd);
		}
		
		if(createTransaction) {
			_bdd.commit();
		}
		
		return res;
	}
	
	/**
	 * Suppression de l'association d'un évènement "non cours" à une salle.
	 * Utile pour ajouter des évènements de cours dans une salle déjà occupée par autre chose (les cours sont prioritaires)
	 * @param idSalles ID des salles à libérer (peut contenir plus de salles que celles occupées)
	 * @param idEvenements ID des évènements concernés
	 * @throws DatabaseException
	 */
	public void supprimerSallesEvenementsNonCours(List<Integer> idSalles, List<Integer> idEvenements) throws DatabaseException {
		
		if(idSalles.isEmpty() || idEvenements.isEmpty()) {
			return;
		}
		
		String strIdSalles = StringUtils.join(idSalles, ",");
		String strIdEvenements = StringUtils.join(idEvenements, ",");
		
		_bdd.executeUpdate("DELETE FROM edt.alieuensalle tablesuppr WHERE EXISTS (" +
				"SELECT 1 FROM edt.alieuensalle " +
				"LEFT JOIN edt.evenementappartient ON evenementappartient.eve_id=alieuensalle.eve_id " +
				"LEFT JOIN edt.calendrierappartientgroupe ON evenementappartient.cal_id=calendrierappartientgroupe.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id=calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) " +
				"WHERE alieuensalle.eve_id IN (" + strIdEvenements + ") AND alieuensalle.salle_id IN (" + strIdSalles + ") " +
					"AND alieuensalle.eve_id = tablesuppr.eve_id AND alieuensalle.salle_id = tablesuppr.salle_id " +
				"GROUP BY alieuensalle.eve_id, alieuensalle.salle_id " +
				"HAVING COUNT(groupecours.groupeparticipant_id) = 0)");
	}
	
	/**
	 * Liste les évènements correspondant à une requête préparée (pour obtenir les événements liés à un groupe, à une salle, à un calendrier, à un responsable).
	 * Le nombre d'événements renvoyé n'est pas vérifié (peut être très élevé), donc attention aux requêtes utilisées.
	 * @param request requêre SQL pour obtenir les événements souhaités
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @parm inflater Inflater permettant de créer l'objet voulu à partir des lignes de base de données
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	private <T  extends EvenementIdentifie> ArrayList<T> listerEvenements(String request, Date dateDebut, Date dateFin, 
			AbsEvenementInflater<T> inflater, boolean createTransaction) throws DatabaseException {
		try {
			return listerEvenements(request, dateDebut, dateFin, inflater, createTransaction, -1);
		}
		catch(MaxRowCountExceededException e) {
			throw new RuntimeException("La méthode listerEvenements n'est pas censée renvoyer une MaxRowCountExceededException avec le paramètre nbMaxEvenements à -1", e);
		}
	}
	
	/**
	 * Liste les évènements correspondant à une requête préparée (pour obtenir les événements liés à un groupe, à une salle, à un calendrier, à un responsable)
	 * @param request requêre SQL pour obtenir les événements souhaités
	 * @param dateDebut Date de début de la recherche
	 * @param dateFin Date de fin de la recherche
	 * @parm inflater Inflater permettant de créer l'objet voulu à partir des lignes de base de données
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @param nbMaxEvenements Indique le nombre maximum d'événements à renvoyer (comparé avec MAX_ROWS_QUERY_EVENEMENTS). -1 si la vérification ne doit pas être effectuée.
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 * @throws EdtempsException Uniquement si le paramètre checkCount est true, est levé si le nombre d'événements excède MAX_ROWS_QUERY_EVENEMENTS
	 */
	private <T  extends EvenementIdentifie> ArrayList<T> listerEvenements(String request, Date dateDebut, Date dateFin, 
			AbsEvenementInflater<T> inflater, boolean createTransaction, int nbMaxEvenements) throws DatabaseException, MaxRowCountExceededException {
		
		ArrayList<T> res = null;
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
			

			PreparedStatement req = _bdd.getConnection().prepareStatement(request, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			if(nbMaxEvenements != -1) {
				reponse.last();
				int nbLignes = reponse.getRow();
				reponse.beforeFirst();
				
				if(nbLignes > nbMaxEvenements) {
					reponse.close();
					throw new MaxRowCountExceededException(nbMaxEvenements, nbLignes);
				}
			}

			res = new ArrayList<T>();
			while(reponse.next()) {
				res.add(inflater.inflateEvenement(reponse, _bdd));
			}
			
			reponse.close();
			
			if(createTransaction){
				_bdd.commit();
			}	
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Récupère le nombre maximum d'événements créés par l'utilisateur par calendriers, pendant la période donnée
	 * @param userId ID de l'utilisateur créateur des événements
	 * @param idCalendriers IDs des calendriers à examiner
	 * @param dateDebut Date de début de la période de recherche
	 * @param dateFin Date de fin de la période de recherche
	 * @return Le nombre maximum d'événements créés par l'utilisateur par calendrier, pendant la fenêtre de recherche
	 * @throws DatabaseException
	 */
	public int getMaxNbEvenementsParCalendriersUtilisateur(int userId, List<Integer> idCalendriers, Date dateDebut, Date dateFin) throws DatabaseException {
		
		if(idCalendriers.isEmpty()) {
			return 0;
		}
		
		String strCalendriers = StringUtils.join(idCalendriers, ",");

		try {
			
			PreparedStatement statement = _bdd.getConnection().prepareStatement("SELECT MAX(sub.nombre) AS max FROM " +
				"(SELECT COUNT(evenement.eve_id) AS nombre FROM edt.evenement " +
				"INNER JOIN edt.evenementappartient ea ON ea.eve_id = evenement.eve_id " +
				"WHERE evenement.eve_createur = ? AND evenement.eve_datedebut < ? AND evenement.eve_datefin > ? " +
				"AND ea.cal_id IN (" + strCalendriers + ") " +
				"GROUP BY ea.cal_id) sub");
			
			statement.setInt(1, userId);
			statement.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			statement.setTimestamp(3, new java.sql.Timestamp(dateDebut.getTime()));
			
			ResultSet reponse = statement.executeQuery();
			
			reponse.next();
			int res = reponse.getInt("max");
			reponse.close();
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Test de la répétition d'un événement : vérification qu'il n'y a pas de conflits
	 * @param idEvenement ID de l'événement à répéter
	 * @param nb Nombre de répétitions
	 * @param periode Période de répétition
	 * @return Tableau résumant l'ensemble de tentatives de répétition
	 * @throws DatabaseException
	 */
	public ArrayList<TestRepetitionEvenement> testRepetitionEvenement(int idEvenement, int nb, int periode, boolean createTransaction) throws DatabaseException {
		
		if(createTransaction) {
			_bdd.startTransaction();
		}
		
		JourFerieGestion jourFerieGestion = new JourFerieGestion(_bdd);
		PeriodeBloqueeGestion periodeBloqueeGestion = new PeriodeBloqueeGestion(_bdd);
		
		// Récupération de l'événement à répéter
		EvenementIdentifie evenement = getEvenement(idEvenement);
		
		int repetitionsCourantes = 0;
		int nbRecherchesRepetition = 0;
		
		Date newDateDebut = DateUtils.addDays(evenement.getDateDebut(), periode);
		Date newDateFin = DateUtils.addDays(evenement.getDateFin(), periode);
		
		ArrayList<Integer> idSalles = new ArrayList<Integer>(evenement.getSalles().size());
		for(SalleIdentifie s : evenement.getSalles()) {
			idSalles.add(s.getId());
		}
		
		ArrayList<TestRepetitionEvenement> res = new ArrayList<TestRepetitionEvenement>();
		
		while(repetitionsCourantes < nb && nbRecherchesRepetition < MAX_RECHERCHE_REPETITIONS) {
			
			ArrayList<Probleme> problemes = new ArrayList<Probleme>();
			
			// Vérification de l'occupation de la salle
			if(idSalles.size() > 0) {
				List<EvenementIdentifie> evenementsCours = listerEvenementsSalleCoursOuPas(idSalles, newDateDebut, newDateFin, true, false);
				
				if(evenementsCours.size() > 0) {
					// Le toString des Evenement utilise leur attribut nom
					problemes.add(new Probleme(ProblemeStatus.SALLE_OCCUPEE_COURS, StringUtils.join(evenementsCours, ", "), evenementsCours));
				}
				else { // Inutile d'examiner les non-cours s'il y a déjà un cours
					List<EvenementIdentifie> evenementsNonCours = listerEvenementsSalleCoursOuPas(idSalles, newDateDebut, newDateFin, false, false);
					if(evenementsNonCours.size() > 0) {
						problemes.add(new Probleme(ProblemeStatus.SALLE_OCCUPEE_NON_COURS, StringUtils.join(evenementsNonCours, ", "), evenementsNonCours));
					}
				}
			}
				
			// Vérification de la disponibilité du public
			try {
				Hashtable<Integer, ArrayList<EvenementComplet>> mapEvenementsPublic = this.listerEvenementsGroupesCalendrier(evenement.getIdCalendriers(), newDateDebut, newDateFin, false);
				ArrayList<EvenementComplet> evenementsPublic = getEvenementsResultatGroupes(mapEvenementsPublic);
				if(evenementsPublic.size() > 0) {
					problemes.add(new Probleme(ProblemeStatus.PUBLIC_OCCUPE, StringUtils.join(evenementsPublic, ", "), evenementsPublic));
				}
			} catch (MaxRowCountExceededException e) {
				problemes.add(new Probleme(ProblemeStatus.PUBLIC_OCCUPE, "Public super-ultra-oversurbooké", null));
			}
			
			// Vérification de la présence d'un jour férié
			Date debutJournee = DateUtils.ceiling(evenement.getDateDebut(), Calendar.DAY_OF_MONTH);
			Date finJournee = DateUtils.addSeconds(debutJournee, 3600*23 + 60*59 + 59);
			List<JourFerieIdentifie> joursFeries = jourFerieGestion.getJoursFeries(debutJournee, finJournee);
			
			if(joursFeries.size() > 0) {
				problemes.add(new Probleme(ProblemeStatus.JOUR_BLOQUE, joursFeries.get(0).getLibelle()));
			}
			
			// Vérification de la présence d'une période bloquée
			List<PeriodeBloqueeIdentifie> periodesBloquees = periodeBloqueeGestion.getPeriodesBloquees(newDateDebut, newDateFin);
			
			if(periodesBloquees.size() > 0) {
				problemes.add(new Probleme(ProblemeStatus.JOUR_BLOQUE, periodesBloquees.get(0).getLibelle()));
			}
			
			// Vérification de la présence d'un dimanche
			if(containsDimanche(newDateDebut, newDateFin)) {
				problemes.add(new Probleme(ProblemeStatus.DIMANCHE, "Dimanche"));
			}
			

			// On affiche le numéro de la répétition qu'on essaie d'effectuer (et pas le nombre déjà effectué)
			int numAffiche = repetitionsCourantes + 1;
			if(problemes.size() == 0) {
				repetitionsCourantes = numAffiche;
			}
			
			res.add(new TestRepetitionEvenement(numAffiche, newDateDebut, newDateFin, problemes));
			
			newDateDebut = DateUtils.addDays(newDateDebut, periode);
			newDateFin = DateUtils.addDays(newDateFin, periode);
			nbRecherchesRepetition++;
		}
		
		if(createTransaction) {
			_bdd.commit();
		}
		
		return res;
	}
	
	/**
	 * Récupération des événements contenus dans une table de résultats, comme celle renvoyée par listerEvenementsGroupesCalendrier.
	 * La liste résultat contient tous les événements sans doublons
	 * @param resultatGroupes Table de résultats
	 * @return Liste d'événements contenus dans la table de résultats, sans doublon
	 */
	protected static ArrayList<EvenementComplet> getEvenementsResultatGroupes(Hashtable<Integer, ArrayList<EvenementComplet>> resultatGroupes) {
		
		ArrayList<EvenementComplet> res = new ArrayList<EvenementComplet>();
		HashSet<Integer> idSet = new HashSet<Integer>();
		for(Entry<Integer, ArrayList<EvenementComplet>> entry : resultatGroupes.entrySet()) {
			
			for(EvenementComplet even : entry.getValue()) {
				if(!idSet.contains(even.getId())) {
					idSet.add(even.getId());
					res.add(even);
				}
			}
		}
		
		return res;
	}
	
	protected static boolean containsDimanche(Date debut, Date fin) {
		Calendar calDebut = Calendar.getInstance(Locale.FRANCE);
		calDebut.setTime(debut);
		
		Calendar calFin = Calendar.getInstance(Locale.FRANCE);
		calFin.setTime(fin);
		
		boolean dimanche = false;
		
		if(DateUtils.isSameDay(calDebut, calFin)) {
			if(calFin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				dimanche = true;
			}
		}
		else {
			while (!DateUtils.isSameDay(calDebut, calFin)) {
				if(calDebut.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					dimanche = true;
					break;
				}
				
				calDebut.add(Calendar.DATE, 1);
			} 
		}
		
		return dimanche;
	}
	
}
