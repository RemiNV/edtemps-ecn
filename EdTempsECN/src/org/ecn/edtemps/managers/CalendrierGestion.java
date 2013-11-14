package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/** 
 * Classe de gestion des calendriers
 * 
 * @author Maxime TERRADE
 */
public class CalendrierGestion {
	
	/** Gestionnaire de base de données */
	protected BddGestion _bdd;
	
	/**
	 * Initialise un gestionnaire de calendriers
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public CalendrierGestion(BddGestion bdd) {
		_bdd = bdd;
	}
	
	
	/**
	 * Méthode d'enregistrement du Calendrier "calendrier" dans la base de données
	 *
	 * NB : le rattachement d'un calendrier à un groupeDeParticipants n'est pas réalisé dans cette fonction.
	 * 
	 * @param calendrier Calendrier à sauvegarder
	 * @return nouvel ID du calendrier sauvegarder
	 */
	public int sauverCalendrier(Calendrier calendrier) throws EdtempsException {
		
		// Récupération des attributs du calendrier
		String matiere = calendrier.getMatiere();
		String nom = calendrier.getNom();
		String type = calendrier.getType();
		List<Integer> idProprietaires = calendrier.getIdProprietaires(); 
				
		// Récupération de l'id de la matiere et du type
		int matiere_id;
		int type_id;
		try {
			// Récupération de l'id de la matière
			PreparedStatement matiere_id_prepare = _bdd.getConnection().prepareStatement(
					"SELECT * FROM edt.matiere WHERE matiere_nom=?");
			matiere_id_prepare.setString(1, matiere);
			matiere_id = _bdd.recupererId(matiere_id_prepare, "matiere_id");

			// Récupération de l'id du type
			PreparedStatement type_id_prepare = _bdd.getConnection().prepareStatement(
					"SELECT * FROM edt.typecalendrier WHERE typecal_libelle=?");
			type_id_prepare.setString(1, type);
			type_id = _bdd.recupererId(type_id_prepare, "typecal_id");
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
		// Vérification unicité/existence des id récupérés 
		if ((matiere_id != -1) && (type_id != -1)) { 			
			
			try {
				// Début transaction
				_bdd.startTransaction();
				
				// On crée le calendrier dans la base de données
				PreparedStatement rs_ligneCreee_prepare = _bdd.getConnection().prepareStatement(
						"INSERT INTO edt.calendrier (matiere_id, cal_nom, typeCal_id)" +
						" VALUES (" + matiere_id + ", ?, " + type_id + ") RETURNING cal_id");
				rs_ligneCreee_prepare.setString(1, nom);
				ResultSet rs_ligneCreee = rs_ligneCreee_prepare.executeQuery();
				
				// On récupère l'id du calendrier créé
				rs_ligneCreee.next();
				int id_calendrier = rs_ligneCreee.getInt(1);
	
				// On définit les utilisateurs idProprietaires comme proprietaires du calendrier créé
				Iterator<Integer> itr = idProprietaires.iterator();
				while (itr.hasNext()){
					int id_utilisateur = itr.next();
					_bdd.executeRequest(
							"INSERT INTO edt.proprietairecalendrier (utilisateur_id, cal_id) "
							+ "VALUES (" + id_utilisateur + ", " + id_calendrier + ")"
							);
				}
				
				// Fin transaction
				_bdd.commit();
				return id_calendrier;
			} 
			catch (DatabaseException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}
			catch (SQLException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}
			
		}
		else {
			throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID matiere ou type non existant ou non unique");
		}
			
	}
	
	/**
	 * Créé un calendrier à partir d'une ligne de base de données
	 * 
	 * Colonnes nécessaires : cal_id, cal_nom, matiere_nom (obtenu depuis la table matiere), typecal_libelle (obtenu depuis la table typecalendrier)
	 * 
	 * @param row ResultSet placé à la ligne de base de données à lire
	 * @return CalendrierIdentifie créé
	 * @throws EdtempsException 
	 * @throws SQLException 
	 */
	private CalendrierIdentifie inflateCalendrierFromRow(ResultSet row) throws DatabaseException, SQLException {
		
		int id = row.getInt("cal_id");
		 String nom = row.getString("cal_nom");
		 String matiere = row.getString("matiere_nom");
		 String type = row.getString("typecal_libelle");
		

		// Récupération des propriétaires du calendrier
		ResultSet rs_proprios = _bdd.executeRequest(
				"SELECT * FROM edt.proprietairecalendrier WHERE cal_id = " + id );
		
		ArrayList<Integer> idProprietaires = new ArrayList<Integer>();
		while(rs_proprios.next()){
			 idProprietaires.add(rs_proprios.getInt("utilisateur_id"));
		}
		
		/* Si au moins un proprio existe, le ou les ajouter aux attibuts du Calendrier. 
		 * Sinon, exception EdtempsException
		 */
		if (idProprietaires.size() != 0) {
			return new CalendrierIdentifie(nom, type, matiere, idProprietaires, id);
		}
		else {
			throw new DatabaseException();
		}
	}
	
	/**
	 * Récupère le calendrier repéré par l'ID donné
	 * @param idCalendrier ID du calendrier à récupérer
	 * @return Calendrier récupéré
	 * @throws EdtempsException Si le calendrier n'existe pas
	 * @throws DatabaseException En cas d'erreur d'accès à la BDD
	 */
	public CalendrierIdentifie getCalendrier(int idCalendrier) throws EdtempsException, DatabaseException {
		
		CalendrierIdentifie result;
		
		try {
			
			// Récupération du calendrier (nom, matiere, type) cherché sous forme de ResultSet
			ResultSet rs_calendrier = _bdd.executeRequest(
					"SELECT * FROM edt.calendrier "
					+ "INNER JOIN edt.matiere ON calendrier.matiere_id = matiere.matiere_id "
					+ "INNER JOIN edt.typecalendrier ON typecalendrier.typeCal_id = calendrier.typeCal_id "
					+ "WHERE cal_id = " + idCalendrier );

			if(rs_calendrier.next()){
				 result = inflateCalendrierFromRow(rs_calendrier);
			}
			else {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, "getCalendrier() error : pas de calendrier correspondant à l'idCalendrier en argument");
			}
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return result;
	}
	
	
	/**
	 * Méthode modifierCalendrier(CalendrierIdentifie calId)
	 * 
	 * Permet de remplacer, dans la base de données, 
	 * les anciennes valeurs du calendrier défini par l'id de calId (en attribut) 
	 * par les valeurs contenues dans calId (en attributs)
	 * 
	 * @param calId : CalendrierIdentifie
	 * @throws EdtempsException
	 */
	public void modifierCalendrier(CalendrierIdentifie calId) 
			throws EdtempsException {
		
		try {
			
			// Commencer une transaction
			_bdd.startTransaction();
			
			// Récupération de l'id de la matiere et du type
			int matiere_id;
			int type_id;
			try {
				// Récupération de l'id de la matière
				PreparedStatement matiere_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.matiere WHERE matiere_nom LIKE ?");
				matiere_id_prepare.setString(1, calId.getMatiere());
				matiere_id = _bdd.recupererId(matiere_id_prepare, "matiere_id");

				// Récupération de l'id du type
				PreparedStatement type_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.typecalendrier WHERE typecal_libelle LIKE ?");
				type_id_prepare.setString(1, calId.getType());
				type_id = _bdd.recupererId(type_id_prepare, "typecal_id");
			} catch (SQLException e){
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}
			
			// Modifier matiere, nom, type du calendrier
			PreparedStatement requete = _bdd.getConnection().prepareStatement(
					"UPDATE edtcalendrier SET (matiere_id, cal_nom, typeCal_id) = "
					+ "(" + matiere_id + ", ?, " + type_id + ") "
					+ "WHERE cal_id = " + calId.getId() );
			requete.setString(1, calId.getNom());
			requete.execute();
			
			// Supprimer ancienne liste de propriétaires du calendrier
			_bdd.executeRequest("DELETE FROM edt.proprietairecalendrier WHERE cal_id = " + calId.getId());
			
			// Ajouter nouvelle liste de propriétaires du calendrier		
			Iterator<Integer> itrProprios = calId.getIdProprietaires().iterator();
			while (itrProprios.hasNext()){
				int idProprio = itrProprios.next();
				_bdd.executeRequest(
						"INSERT INTO edt.proprietairecalendrier "
						 + "VALUES (utilisateur_id, cal_id) = "
						 + "(" + idProprio +", " + calId.getId() + ") " 
				);
			}
			
			// Fin transaction
			_bdd.commit();
			
		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
	}

	
	
	/**
	 * Méthode supprimerCalendrier(int idCalendrier)
	 * 
	 * Permet de supprimer un calendrier dans la base de données,
	 * calendrier défini par l'entier en argument, correspondant à l'ID du calendrier 
	 * 
	 * @param idCalendrier
	 * @throws EdtempsException
	 */
	public void supprimerCalendrier(int idCalendrier) 
			throws EdtempsException {
		
		try {
			// Début transaction
			_bdd.startTransaction();
			
			// Supprimer liste de propriétaires du calendrier
			_bdd.executeRequest(
					"DELETE FROM edt.proprietairecalendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Supprimer dépendance avec les groupes de participants
			_bdd.executeRequest(
					"DELETE FROM edt.calendrierAppartientGroupe "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			/* Supprimer les événements associés au calendrier
			 * 		1 - Récupération des id des evenements associés
			 * 		2 - Suppression du lien entre les evenements et le calendrier
			 * 		3 - Suppression des evenements eux-même
			 */
			ResultSet rs_evenementsAssocies = _bdd.executeRequest(
					"SELECT * FROM  edt.evenementAppartient "
					+ "WHERE cal_id = " + idCalendrier 
					);
			_bdd.executeRequest(
					"DELETE FROM edt.evenementAppartient "
					 + "WHERE cal_id = " + idCalendrier  
					 );
			while(rs_evenementsAssocies.next()){
				EvenementGestion eveGestionnaire = new EvenementGestion(this._bdd);
				eveGestionnaire.supprimerEvenement(rs_evenementsAssocies.getInt("eve_id"), false);
			}
			// Supprimer calendrier
			_bdd.executeRequest(
					"DELETE FROM edt.calendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Fin transaction
			_bdd.commit(); 

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
	}	
	
	/**
	 * Listing des types de calendrier disponibles dans la base de données
	 * @return Hashmap indiquant les ID (clés) et noms (string) des types de calendrier disponibles
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	public HashMap<Integer, String> listerTypesCalendrier() throws DatabaseException {
		HashMap<Integer, String> res = new HashMap<Integer, String>();
		
		ResultSet bddRes = _bdd.executeRequest("SELECT typecal_id, typecal_libelle FROM edt.typecalendrier");
		
		try {
			while(bddRes.next()) {
				res.put(bddRes.getInt(1), bddRes.getString(2));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		return res;
	}
	
	/**
	 * Listing des matières disponibles dans la base de données
	 * @return Hashmap indiquant les ID (clés) et noms (string) des matières disponibles
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	public HashMap<Integer, String> listerMatieres() throws DatabaseException {
		HashMap<Integer, String> res = new HashMap<Integer, String>();
		
		ResultSet bddRes = _bdd.executeRequest("SELECT matiere_id, matiere_nom FROM edt.matiere");
		
		try {
			while(bddRes.next()) {
				res.put(bddRes.getInt(1), bddRes.getString(2));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		return res;
	}
	
	/**
	 * Récupération des calendriers correspondant aux abonnements de l'utilisateur
	 * 
	 * Cette méthode <b>doit</b> être exécutée à l'intérieur d'une transaction, ou créer soi-même sa transaction.
	 * Dans le cas où ceci ne serait pas le cas, une exception indiquant qu'une table temporaire n'existe pas se produira.
	 * 
	 * @param userId ID de l'utilisateur dont les calendriers sont à récupérer
	 * @param createTransaction Transaction à créer à l'intérieur de cette méthode (si pas déjà créée).
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return
	 * @throws EdtempsException
	 */
	public ArrayList<CalendrierIdentifie> listerCalendriersAbonnementsUtilisateur(int userId, boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
		try {
			if(createTransaction)
				_bdd.startTransaction();
			
			if(!reuseTempTableAbonnements)
				GroupeGestion.makeTempTableListeGroupesAbonnement(_bdd, userId);
			
			// Récupération des calendriers des collections abonnement
			ResultSet results = _bdd.executeRequest("SELECT calendrier.cal_id, calendrier.cal_nom, matiere.matiere_nom, typecalendrier.typecal_libelle FROM edt.calendrier " +
					"INNER JOIN edt.matiere ON calendrier.matiere_id = matiere.matiere_id " +
					"INNER JOIN edt.typecalendrier ON typecalendrier.typecal_id = calendrier.typecal_id " +
					"INNER JOIN edt.calendrierappartientgroupe appartenance ON appartenance.cal_id=calendrier.cal_id " +
					"INNER JOIN " + GroupeGestion.NOM_TEMPTABLE_ABONNEMENTS + " tmpAbonnements ON tmpAbonnements.groupeparticipant_id=appartenance.groupeparticipant_id");
			
			ArrayList<CalendrierIdentifie> res = new ArrayList<CalendrierIdentifie>();
			
			while(results.next()) {
				res.add(this.inflateCalendrierFromRow(results));
			}
			
			if(createTransaction)
				_bdd.commit(); // Supprime la table temporaire
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}
	
	/**
	 * Listing des calendriers dont un utilisateur est propriétaire
	 * @param userId ID de l'utilisateur propriétaire
	 * @return Liste des calendriers trouvés
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	public ArrayList<CalendrierComplet> listerCalendriersUtilisateur(int userId) throws DatabaseException {
		ResultSet results = _bdd.executeRequest("SELECT calendrier.cal_id, calendrier.cal_nom, matiere.matiere_nom, typecalendrier.typecal_libelle FROM edt.calendrier " +
				"INNER JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id " +
				"INNER JOIN edt.typecalendrier ON typecalendrier.typecal_id = calendrier.typecal_id " +
				"INNER JOIN edt.proprietairecalendrier ON calendrier.cal_id = proprietairecalendrier.cal_id AND proprietairecalendrier.utilisateur_id = " + userId);
		
		try {
			ArrayList<CalendrierComplet> res = new ArrayList<CalendrierComplet>();
			while (results.next()) {
				CalendrierIdentifie calendrier = inflateCalendrierFromRow(results);
				
				ResultSet resultaaaaaas = _bdd.executeRequest("SELECT COUNT(*) FROM edt.groupeparticipant " +
				"INNER JOIN edt.calendrierappartientgroupe ON groupeparticipant.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
				"AND groupeparticipant.groupeparticipant_estcours = 'TRUE' " +
				"AND calendrierappartientgroupe.cal_id = " + calendrier.getId());
				resultaaaaaas.next();
				boolean estCours = (resultaaaaaas.getInt(1)==0) ? false : true;
				
				res.add(new CalendrierComplet(calendrier, estCours));
			}
			
			results.close();
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Résumé des droits d'un utilisateur sur un ensemble de calendriers fourni
	 */
	public static class DroitsCalendriers {
		/**
		 * Indique que l'utilisateur est propriétaire de tous les calendriers
		 */
		public final boolean estProprietaire;
		
		/**
		 * Indique qu'un des calendriers est associé à un groupe de cours
		 */
		public final boolean contientCours;
		
		public DroitsCalendriers(boolean estProprietaire, boolean contientCours) {
			this.estProprietaire = estProprietaire;
			this.contientCours = contientCours;
		}
	}
	
	/**
	 * Récupère les informations de droits sur les calendriers indiqués
	 * 
	 * @param userId ID de l'utilisateur
	 * @param calendriersIds IDs des calendriers à vérifier
	 * @return résumé des droits des calendriers indiqués
	 */
	public DroitsCalendriers getDroitsCalendriers(int userId, List<Integer> calendriersIds) throws DatabaseException {
		
		if(calendriersIds.size() == 0) {
			return new DroitsCalendriers(true, false);
		}
		
		String strIds = StringUtils.join(calendriersIds, ",");
		// TODO : gérer le cas cours mais pas proprio
		ResultSet results = _bdd.executeRequest("SELECT COUNT(DISTINCT proprietairecalendrier.cal_id) AS nb_calendriers_proprietaire, " +
					"COUNT(groupecours.groupeparticipant_id) AS nb_groupe_cours " +
				"FROM edt.proprietairecalendrier " +
				"INNER JOIN edt.calendrier ON proprietairecalendrier.cal_id=calendrier.cal_id " +
				"LEFT JOIN edt.calendrierappartientgroupe ON calendrier.cal_id=calendrierappartientgroupe.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id=calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) " +
				"WHERE proprietairecalendrier.utilisateur_id=" + userId + " AND proprietairecalendrier.cal_id IN (" + strIds + ")");
		
		try {
			results.next();
			
			return new DroitsCalendriers(results.getInt(1) == calendriersIds.size(), results.getInt(2) > 0);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
}
