package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Evenement;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

/** 
 * Classe de gestion des evenements
 * 
 * @author Maxime TERRADE
 *
 */
public class EvenementGestion {

	protected BddGestion _bdd;
	
	/**
	 * Initialise un gestionnaire d'evenements
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public EvenementGestion(BddGestion bdd) {
		_bdd = bdd;
	}
		
	
	/**
	 * Méthode d'enregistrement d'un evenement dans la base de données
	 * 
	 * @param evenement
	 */
	public void sauverEvenement(Evenement evenement) throws EdtempsException {
		
		// Récupération des attributs de l'evenement
		String nom = evenement.getNom();
		Date dateDebut = evenement.getDateDebut();
		Date dateFin = evenement.getDateFin();
		List<Integer> idCalendriers = evenement.getIdCalendriers();
		List<UtilisateurIdentifie> idIntervenants = evenement.getIntervenants();
		// int idSalle = evenement.getSalle().getId();
				
		/*
		 * IMPORTANT POUR CONTINUER
		 * Liste de String pour les intervenants oO ? Comment on retrouve l'id ??
		 * Note (Rémi) suite à réunion du 16/10, modification des String en UtilisateurIdentifie
		 * Note (Rémi) les évènements peuvent être dans plusieurs salles, j'ai modifié la classe salle -> getSalle() devient getSalles()
		 */
			
		try {
			
			// On met les dates au format DATETIME
			String dateDebutFormatee = "";
			String dateFinFormatee = "";
			
			// Début transaction
			_bdd.startTransaction();			
			
			// On crée l'événement dans la base de données
	
			ResultSet rs_ligneCreee = _bdd.executeRequest(
					"INSERT INTO edt.evenement (eve_nom, eve_dateDebut, eve_dateFin) "
					+ "VALUES ( '" + nom + "', '" + dateDebutFormatee + "', '" + dateFinFormatee + "') "
				    + "RETURNING eve_id");
			
			// On récupère l'id de l'evenement créé
			rs_ligneCreee.next();
			int id_evenement = rs_ligneCreee.getInt(1);
			
			// On rattache l'evenement aux calendriers 
			Iterator<Integer> itr = idCalendriers.iterator();
			while (itr.hasNext()){
				int id_calendrier = itr.next();
				_bdd.executeRequest(
						"INSERT INTO edt.evenementappartient (eve_id, cal_id) "
						+ "VALUES (" + id_evenement + ", " + id_calendrier + ")"
						);
			}
			
			// On rattache la salle à l'evenement
			
			// On rattache le matériel nécessité à l'évenement
			
			// On indique le(s) responsable(s) dans la base
			
			// On indique le(s) intervenant(s) dans la base
			
			// Fin transaction
			_bdd.commit();
		} 
		catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
			
	}
	
	/**
	 * Modification d'un événement existant (en base de données)
	 * 
	 * <p>Permet d'actualiser dans la base de données les anciens attributs d'un événements (date, nom, intervenant, ...)
	 * avec de nouveaux ayant été modifiés par un utilisateur</p>
	 * 
	 * @param 
	 * @throws EdtempsException
	 */
	public void modifierEvenement(EvenementIdentifie evenementIdentifie) throws EdtempsException{
		try {
			//début d'une transaction
			_bdd.startTransaction();
			
			// Modifier l'évenement (nom, date début, date fin)
			_bdd.executeUpdate(
					"UPDATE evenement"
					+ "SET eve_nom = " + evenementIdentifie.getNom()
					+ "SET eve_datedebut = " + evenementIdentifie.getDateDebut() 
					+ "SET eve_datefin = " +  evenementIdentifie.getDateFin()
					+ "WHERE eve_id = " + evenementIdentifie.getId());
			
			// Modifier  les intervenants de l'évenement (supprimer les anciens puis ajouter les nouveaux)
			_bdd.executeRequest(
					"DELETE FROM intervenantevenement "
					 + "WHERE eve_id = " + evenementIdentifie.getId());
			for (int i=0; i<evenementIdentifie.getIntervenants().size();i++){
				_bdd.executeRequest(
						"INSERT INTO intervenantevenement"
						+ "VALUES (utilisateur_id, eve_id) = "
						+ "(" + evenementIdentifie.getIntervenants().get(i).getId() +", " + evenementIdentifie.getId() + ")");
			}
			
			// Modifier le matériel nécessaire à l'évenement
			_bdd.executeRequest(
					"DELETE FROM necessitemateriel "
					 + "WHERE eve_id = " + evenementIdentifie.getId());
			for (int i=0; i<evenementIdentifie.getMateriels().size();i++){
				_bdd.executeRequest(
						"INSERT INTO necessitemateriel"
						+ "VALUES (materiel_id, necessitemateriel_quantite, eve_id) = "
						+ "(" + evenementIdentifie.getMateriels().get(i).getId() + ", " + evenementIdentifie.getMateriels().get(i).getQuantite() + ", " + evenementIdentifie.getId() + ")");
			}
			
			// Modifier  les responsables de l'événement
			_bdd.executeRequest(
					"DELETE FROM responsableevenement "
					 + "WHERE eve_id = " + evenementIdentifie.getId());
			for (int i=0; i<evenementIdentifie.getIntervenants().size();i++){
				_bdd.executeRequest(
						"INSERT INTO responsableevenement"
						+ "VALUES (utilisateur_id, eve_id) = "
						+ "(" + evenementIdentifie.getResponsables().get(i).getId() +", " + evenementIdentifie.getId() + ")");
			}
			
			// Modifier les calendriers associés à l'événement
			_bdd.executeRequest(
					"DELETE FROM evenementappartient"
					+ "WHERE eve_id = " + evenementIdentifie.getId());
			for (int i=0; i<evenementIdentifie.getIdCalendriers().size();i++){
				_bdd.executeRequest(
						"INSERT INTO evenementappartient"	
						+ "VALUES (cal_id, eve_id) = "
						+ "(" + evenementIdentifie.getIdCalendriers().get(i) + ", " + evenementIdentifie.getId() + ")");
			}
			
			// Modifier les salles de l'�v�nement
			_bdd.executeRequest(
					"DELETE FROM alieuensalle"
					+ "WHERE eve_id = " + evenementIdentifie.getId());
			for (int i=0; i<evenementIdentifie.getSalles().size();i++){
				_bdd.executeRequest(
						"INSERT INTO alieuensalle"	
						+ "VALUES (salle_id, eve_id) = "
						+ "(" + evenementIdentifie.getSalles().get(i) + ", " + evenementIdentifie.getId() + ")");
			}
			
			// fin transaction
			_bdd.commit();
			
		} catch (DatabaseException e){
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Suppresion d'un évènement
	 * 
	 * Permet de supprimer un évènement dans la base de données,
	 * l'évènement est identifié par son ID entier
	 * 
	 * @param idEvement idetifiant de l'événement
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @throws EdtempsException
	 */
	// ajouté à la volée pour éviter erreur dans la méthode supprimerCalendrier, qui utilise cette méthode
	public void supprimerEvenement(int idEvenement, boolean createTransaction) throws EdtempsException {
		try {
			// Début transaction si nécessaire
			if (createTransaction) {
				_bdd.startTransaction();
			}
			
			// Supprimer l'association aux intervenants de l'�venement
			_bdd.executeRequest(
					"DELETE FROM intervenantevenement "
					 + "WHERE eve_id = " + idEvenement);
			
			// Supprimer l'association au mat�riel n�cessaire pour l'�v�nement
			_bdd.executeRequest(
					"DELETE FROM necessitemateriel"
					 + "WHERE eve_id = " + idEvenement);
			
			// Supprimer l'association aux intervenants de l'�venement
			_bdd.executeRequest(
					"DELETE FROM responsabletevenement "
					 + "WHERE eve_id = " + idEvenement);
			
			// Supprimer l'asosciation aux salles de l'�v�nement
			_bdd.executeRequest(
					"DELETE FROM alieuensalle "
					 + "WHERE eve_id = " + idEvenement);
			
			// Supprimer l'association aux calendriers
			_bdd.executeRequest(
					"DELETE FROM evenementappartient "
					 + "WHERE eve_id = " + idEvenement);
			
			// Supprimer l'�venement
			_bdd.executeRequest(
					"DELETE FROM evenement "
					 + "WHERE eve_id = " + idEvenement);

			
			// fin transaction si nécessaire
			if (createTransaction){
				_bdd.commit();
			}
			
		} catch (DatabaseException e){
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Créé un évènement à partir de l'entrée de base de données fournie.
	 * Colonnes nécessaires pour le ResultSet fourni : eve_id, eve_nom, eve_datedebut, eve_datefin
	 * 
	 * @param reponse Réponse de la base de données, curseur déjà placé sur la ligne à lire
	 * @return Evènement créé
	 * @throws DatabaseException 
	 */
	private EvenementIdentifie inflateEvenementFromRow(ResultSet reponse) throws SQLException, DatabaseException {

		int id = reponse.getInt("eve_id");
		String nom = reponse.getString("eve_nom");
		Date dateDebut = reponse.getTimestamp("eve_datedebut");
		Date dateFin = reponse.getTimestamp("eve_datefin");
		
		// Récupération des IDs des calendriers
		ArrayList<Integer> idCalendriers = _bdd.recupererIds(
				"SELECT cal_id "
				+ "FROM edt.evenementappartient "
				+ "WHERE eve_id=" + id, "cal_id");
		
		// Récupération des salles
		SalleGestion salleGestion = new SalleGestion(_bdd);
		ArrayList<SalleIdentifie> salles = salleGestion.getSallesEvenement(id);
		
		// Récupération des intervenants
		UtilisateurGestion utilisateurGestion = new UtilisateurGestion(_bdd);
		ArrayList<UtilisateurIdentifie> intervenants = utilisateurGestion.getIntervenantsEvenement(id);
		
		// Récupération des responsables
		ArrayList<UtilisateurIdentifie> responsables = utilisateurGestion.getResponsablesEvenement(id);
		
		// Matériel
		ResultSet reponseMateriel = _bdd.executeRequest(
				"SELECT materiel.materiel_id AS id, materiel.materiel_nom AS nom, necessitemateriel.necessitemateriel_quantite AS quantite " +
				"FROM edt.materiel INNER JOIN edt.necessitemateriel ON necessitemateriel.materiel_id = materiel.materiel_id "
				+ "AND necessitemateriel.eve_id=" + id);
		
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		while(reponseMateriel.next()) {
			materiels.add(new Materiel(reponseMateriel.getInt("id"), reponseMateriel.getString("nom"), reponseMateriel.getInt("quantite")));
		}
		reponseMateriel.close();
		
		return new EvenementIdentifie(nom, dateDebut, dateFin, idCalendriers, salles, intervenants, responsables, materiels, id);
	}
	
	/**
	 * Récupération d'un évènement en base
	 * @param idEvenement ID de l'évènement à récupérer
	 * @return Evènement récupéré
	 * @throws DatabaseException 
	 */
	public EvenementIdentifie getEvenement(int idEvenement) throws DatabaseException {
		ResultSet reponse = _bdd.executeRequest(
				"SELECT eve_id, eve_nom, eve_datedebut, eve_datefin "
				+ "FROM edt.evenement "
				+ "WHERE eve_id=" + idEvenement);
		
		EvenementIdentifie res = null;
		try {
			if(reponse.next()) {
				res = inflateEvenementFromRow(reponse);
			}
			reponse.close();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
			
		return res;
	}
	
	/**
	 * Liste les évènements liés à un groupe d'utilisateurs
	 * @param idGroupe groupe dont les évènements sont à récupérer
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsGroupe(int idGroupe, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
						
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
					"FROM edt.evenement " +
					"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
					"INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
					"WHERE calendrierappartientgroupe.cal_id = " + idGroupe
					+ "AND evenement.eve_datefin >= ? "
					+ "AND evenement.eve_datedebut <= ?");
			
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			res = new ArrayList<EvenementIdentifie>();
			while(reponse.next()) {
				res.add(inflateEvenementFromRow(reponse));
			}
			
			reponse.close();
			
			if(createTransaction){
				_bdd.commit();
			}	
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
	/**
	 * Liste les évènements auxquels un utilisateur est abonné par l'intermédiaire de ses abonnements aux groupes, et donc aux calendriers
	 * @param idUtilisateur Utilisateur dont les évènements sont à récupérer
	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsUtilisateur(int idUtilisateur, Date dateDebut, Date dateFin, 
			boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		try {
			if(createTransaction)
				_bdd.startTransaction();
			
			if(!reuseTempTableAbonnements)
				GroupeGestion.makeTempTableListeGroupesAbonnement(_bdd, idUtilisateur);
			
			PreparedStatement req = _bdd.getConnection().prepareStatement("SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
					"FROM edt.evenement " +
					"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
					"INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
					"INNER JOIN " + GroupeGestion.NOM_TEMPTABLE_ABONNEMENTS + " abonnements ON abonnements.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
					"WHERE evenement.eve_datefin >= ? AND evenement.eve_datedebut <= ?");
			
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			res = new ArrayList<EvenementIdentifie>();
			while(reponse.next()) {
				res.add(inflateEvenementFromRow(reponse));
			}
			
			reponse.close();
			
			if(createTransaction)
				_bdd.commit();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
	/**
	 * Liste les évènements liés à une salle
	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsSalle(int idSalle, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
						
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
					"FROM edt.evenement " +
					"INNER JOIN edt.alieuensalle ON evenement.eve_id = alieuensalle.eve_id " +
					"WHERE alieuensalle.salle_id = " + idSalle
					+ "AND evenement.eve_datefin >= ? "
					+ "AND evenement.eve_datedebut <= ?");
			
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			res = new ArrayList<EvenementIdentifie>();
			while(reponse.next()) {
				res.add(inflateEvenementFromRow(reponse));
			}
			
			reponse.close();
			
			if(createTransaction){
				_bdd.commit();
			}	
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
	/**
	 * Liste les évènements liés à un responsable
	 * @param idResponsable identifiant de l'utilisateur responsable des évènements à récupérer
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsResponsable(int idResponsable, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
						
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
					"FROM edt.evenement " +
					"INNER JOIN edt.responsablevenement ON evenement.eve_id = responsableevenement.eve_id " +
					"WHERE responsableevenement.utilisateur_id = " + idResponsable
					+ "AND evenement.eve_datefin >= ? "
					+ "AND evenement.eve_datedebut <= ?");
			
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			res = new ArrayList<EvenementIdentifie>();
			while(reponse.next()) {
				res.add(inflateEvenementFromRow(reponse));
			}
			
			reponse.close();
			
			if(createTransaction){
				_bdd.commit();
			}	
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
	/**
	 * Liste les évènements liés à un calendrier
	 * @param idCalendrier identifiant du calendrier dont les évènements sont à récupérer
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
	 * 
	 * @return Liste d'évènements récupérés
	 * @throws DatabaseException
	 */
	public ArrayList<EvenementIdentifie> listerEvenementsCalendrier(int idCalendrier, Date dateDebut, Date dateFin, 
			boolean createTransaction) throws DatabaseException {
		
		ArrayList<EvenementIdentifie> res = null;
	
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
						
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
					"FROM edt.evenement " +
					"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
					"WHERE evenementappartient.cal_id = " + idCalendrier
					+ "AND evenement.eve_datefin >= ? "
					+ "AND evenement.eve_datedebut <= ?");
			
			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			
			res = new ArrayList<EvenementIdentifie>();
			while(reponse.next()) {
				res.add(inflateEvenementFromRow(reponse));
			}
			
			reponse.close();
			
			if(createTransaction){
				_bdd.commit();
			}	
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
}
