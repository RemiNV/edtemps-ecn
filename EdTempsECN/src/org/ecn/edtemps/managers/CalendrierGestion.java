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
import org.ecn.edtemps.managers.UtilisateurGestion.ActionsEdtemps;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.inflaters.CalendrierCompletInflater;
import org.ecn.edtemps.models.inflaters.CalendrierIdentifieInflater;

/** 
 * Classe de gestion des calendriers
 * 
 * @author Maxime TERRADE
 */
public class CalendrierGestion {
	
	/** Gestionnaire de base de données */
	protected BddGestion _bdd;
	
	/** Nombre maximum de calendriers qu'un utilisateur peut créer */
	public static final int LIMITE_CALENDRIERS_PAR_UTILISATEUR = 20;

	/** Nombre maximum de calendriers qu'un utilisateur peut créer avec un droit étendu */
	public static final int LIMITE_CALENDRIERS_PAR_UTILISATEUR_ETENDUE = 100;

	
	/**
	 * Initialise un gestionnaire de calendriers
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public CalendrierGestion(BddGestion bdd) {
		_bdd = bdd;
	}
	
	
	/**
	 * Enregistrement d'un calendrier en la base de données
	 *
	 * NB : le rattachement d'un calendrier à un groupeDeParticipants est réalisé dans cette fonction.
	 * 
	 * @param calendrier Calendrier à sauvegarder
	 * @param idGroupesParents ID des groupes à rattacher au calendrier
	 * @return ID du calendrier sauvegardé
	 * @throws EdtempsException
	 */
	public int sauverCalendrier(Calendrier calendrier, List<Integer> idGroupesParents) throws EdtempsException {
		
		// Vérifier si l'utilisateur n'a pas créé trop de calendriers
		UtilisateurGestion userGestion = new UtilisateurGestion(_bdd);
		boolean limiteEtendue = userGestion.aDroit(ActionsEdtemps.LIMITE_CALENDRIERS_ETENDUE, calendrier.getIdCreateur());
		int nbCalendriersDejaCrees = this.getNombresCalendriersCres(calendrier.getIdCreateur());
		int nbCalendriersAutorisesACreer = limiteEtendue ? LIMITE_CALENDRIERS_PAR_UTILISATEUR_ETENDUE : LIMITE_CALENDRIERS_PAR_UTILISATEUR;
		if (nbCalendriersDejaCrees >= nbCalendriersAutorisesACreer) {
			throw new EdtempsException(ResultCode.QUOTA_EXCEEDED, "Quota de création de calendriers atteint");
		}
		
		// Récupération des attributs du calendrier
		String matiere = calendrier.getMatiere();
		String nom = calendrier.getNom();
		String type = calendrier.getType();
		Integer idCreateur = calendrier.getIdCreateur();
		List<Integer> idProprietaires = calendrier.getIdProprietaires(); 
		
		if(idCreateur==null) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un calendrier doit avoir un créateur");
		}
		
		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un calendrier doit être alphanumérique");
		}
		
		try {
			// Début transaction
			_bdd.startTransaction();
			
			// Vérifier que le nom n'est pas déjà pris
			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT COUNT(*) FROM edt.calendrier WHERE cal_nom=?");
			nomDejaPris.setString(1, nom);
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			nomDejaPrisResult.next();
			if (nomDejaPrisResult.getInt(1)>0) {
				throw new EdtempsException(ResultCode.NAME_TAKEN, "Tentative de créer un calendrier avec un nom déjà utilisé.");
			}
			
			// Requete préparée pour la création du calendrier dans la base de données
			PreparedStatement rs_ligneCreee_prepare = _bdd.getConnection().prepareStatement(
					"INSERT INTO edt.calendrier (cal_nom, matiere_id, typeCal_id, cal_createur)" +
					" VALUES (?, ?, ?, ?) RETURNING cal_id");
			
			// Ajout du nom du calendrier à la requete préparée
			rs_ligneCreee_prepare.setString(1, nom);
			
			// Ajout de l'id du proprietaire à la requete preparée
			rs_ligneCreee_prepare.setInt(4, idCreateur);
			
			// Ajout de la matiere du calendrier à la requete préparée (NULL si pas de matiere)
			if (StringUtils.isEmpty(matiere)) { 
				rs_ligneCreee_prepare.setNull(2, java.sql.Types.INTEGER);
			}
			else {
				// Récupération de l'id de la matière
				PreparedStatement matiere_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.matiere WHERE matiere_nom=?");
				matiere_id_prepare.setString(1, matiere);
				int matiere_id = _bdd.recupererId(matiere_id_prepare, "matiere_id");
				if (matiere_id == -1) { //le nom de la matiere match aucun/plusieurs id -> ERREUR
					throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID matiere non existant ou non unique");
				}
				else {
					// Ajout à la requete préparée
					rs_ligneCreee_prepare.setInt(2, matiere_id);
				}
				
			}
			
			// Ajout du type du calendrier à la requete préparée (NULL si pas de type)
			if (StringUtils.isEmpty(type)) { 
				rs_ligneCreee_prepare.setNull(3, java.sql.Types.INTEGER);
			}
			else {
				// Récupération de l'id du type
				PreparedStatement type_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.typecalendrier WHERE typecal_libelle=?");
				type_id_prepare.setString(1, type);
				int type_id = _bdd.recupererId(type_id_prepare, "typecal_id");
				if (type_id == -1) { //le nom du type match aucun/plusieurs id -> ERREUR
					throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID type non existant ou non unique");
				}
				else {
					// Ajout à la requete préparée
					rs_ligneCreee_prepare.setInt(3, type_id);
				}
				
				
			}
			
			// On effectue la requete et recupère l'id du calendrier créé
			ResultSet rs_ligneCreee = rs_ligneCreee_prepare.executeQuery();
			rs_ligneCreee.next();
			int idCalendrier = rs_ligneCreee.getInt(1);
			
			// Requete préparée pour la création du groupe unique associé 
			PreparedStatement req = _bdd.getConnection().prepareStatement(
						"INSERT INTO edt.groupeparticipant "
						+ "(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estcalendrierunique, groupeparticipant_createur) "
						+ "VALUES (?, 'FALSE', 'TRUE', ?) "
						+ "RETURNING groupeparticipant_id"
						);
			// nom du groupe unique = nom du calendrier
			req.setString(1, nom); 
			// créateur du groupe unique = créateur du calendrier
			req.setInt(2, idCreateur); 
			// On effectue la requete et récupère l'id du gpe de paricipant créé
			ResultSet req_ligneCreee = req.executeQuery();
			req_ligneCreee.next();
			int idGroupeCree = req_ligneCreee.getInt(1);
			
			// On lie le calendrier au groupe unique
			_bdd.executeUpdate(
					"INSERT INTO edt.calendrierappartientgroupe (groupeparticipant_id, cal_id) "
					+ "VALUES (" + idGroupeCree + ", " + idCalendrier + ")"
					);
			
			// On lie le calendrier aux groupes parents désirés (en argument) 
			//   -> rattachement en attente (champ "_tmp") si le créateur n'est pas propriétaire du groupe parent
			Iterator<Integer> itr = idGroupesParents.iterator();
			while (itr.hasNext()){
				int idGroupeParent = itr.next();
				// Requête pour savoir si le créateur est propriétaire du groupe parent 
				ResultSet estProprietaireGroupeParent = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.proprietairegroupeparticipant"
						+ " WHERE groupeparticipant_id="+idGroupeParent
						+ " AND utilisateur_id="+calendrier.getIdCreateur()
				).executeQuery();
				//Si le créateur est propriétaire (ie si le resultat de la requete est non vide)
				if (estProprietaireGroupeParent.next()) {
					_bdd.executeUpdate(
							"INSERT INTO edt.calendrierappartientgroupe (groupeparticipant_id, cal_id) "
							+ "VALUES (" + idGroupeParent + ", " + idCalendrier + ")"
							);
				}
				//S'il n'est pas propriétaire
				else {
					_bdd.executeUpdate(
							"INSERT INTO edt.calendrierappartientgroupe (groupeparticipant_id_tmp, cal_id) "
							+ "VALUES (" + idGroupeParent + ", " + idCalendrier + ")"
							);
				}
			}
			
			// Définition des propriétaires du calendrier 
			itr = idProprietaires.iterator();
			while (itr.hasNext()){
				int idProprietaire = itr.next();
				_bdd.executeUpdate(
						"INSERT INTO edt.proprietairecalendrier (utilisateur_id, cal_id) "
						+ "VALUES (" + idProprietaire + ", " + idCalendrier + ")"
						);
				/* SUPPRIMER POUR EVITER REDONDANCE DANS LA BBD
				_bdd.executeUpdate(
						"INSERT INTO edt.proprietairegroupeparticipant "
						+ "(utilisateur_id, groupeparticipant_id) "
						+ "VALUES (" + idProprietaire + ", " + idGroupeCree	+ ")"
				);
				*/
			}
			
			// Fin transaction
			_bdd.commit();
			return idCalendrier;

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} 	
	}
	
	
	/**
	 * Récupèrer un calendrier
	 * @param idCalendrier ID du calendrier à récupérer
	 * @return calendrier récupéré
	 * @throws EdtempsException Si le calendrier n'existe pas
	 * @throws DatabaseException
	 */
	public CalendrierIdentifie getCalendrier(int idCalendrier) throws EdtempsException, DatabaseException {
		
		CalendrierIdentifie result;
		
		try {
			
			// Récupération du calendrier (nom, matiere, type) cherché sous forme de ResultSet
			ResultSet rs_calendrier = _bdd.executeRequest(
					"SELECT * FROM edt.calendrier "
					+ "LEFT JOIN edt.matiere ON calendrier.matiere_id = matiere.matiere_id "
					+ "LEFT JOIN edt.typecalendrier ON typecalendrier.typeCal_id = calendrier.typeCal_id "
					+ "WHERE cal_id = " + idCalendrier );

			if(rs_calendrier.next()){
				 result = new CalendrierIdentifieInflater().inflateCalendrier(rs_calendrier, _bdd);
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
	 * Modifier un calendrier : remplacement du calendrier de la base par celui passé en paramètre
	 * @param calId Calendrier à modifier en base de données
	 * @param idGroupesParents IDs des groupes rattachés / à rattacher au calendrier
	 * @throws EdtempsException
	 */
	public void modifierCalendrier(CalendrierIdentifie calId, List<Integer> idGroupesParents) throws EdtempsException {
		
		if(!StringUtils.isAlphanumericSpace(calId.getNom())) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un calendrier doit être alphanumérique");
		}
		
		try {
			// Début transaction
			_bdd.startTransaction();
			
			// Vérifier que le nom n'est pas déjà pris
			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT COUNT(*) FROM edt.calendrier WHERE cal_nom=? AND cal_id<>?");
			nomDejaPris.setString(1, calId.getNom());
			nomDejaPris.setInt(2, calId.getId());
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			nomDejaPrisResult.next();
			if (nomDejaPrisResult.getInt(1)>0 ) {
				throw new EdtempsException(ResultCode.NAME_TAKEN, "Tentative de modifier un calendrier avec un nom déjà utilisé.");
			}
			
			// Requete préparée pour la modification du calendrier
			PreparedStatement requete = _bdd.getConnection().prepareStatement(
					"UPDATE edt.calendrier SET (cal_nom, matiere_id, typeCal_id) = "
					+ "(?, ?, ?) "
					+ "WHERE cal_id = " + calId.getId() );
			
			// Ajout du nom du calendrier à la requete préparée
			requete.setString(1, calId.getNom());
			
			// Ajout de la matiere du calendrier à la requete préparée (NULL si pas de matiere)
			String matiere = calId.getMatiere();
			if (StringUtils.isEmpty(matiere)) { 
				requete.setNull(2, java.sql.Types.INTEGER);
			}
			else {
				// Récupération de l'id de la matière
				PreparedStatement matiere_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.matiere WHERE matiere_nom=?");
				matiere_id_prepare.setString(1, matiere);
				int matiere_id = _bdd.recupererId(matiere_id_prepare, "matiere_id");
				if (matiere_id == -1) { //le nom de la matiere match aucun/plusieurs id -> ERREUR
					throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID matiere non existant ou non unique");
				}
				else {
					// Ajout à la requete préparée
					requete.setInt(2, matiere_id);
				}
				
			}
			
			// Ajout du type du calendrier à la requete préparée (NULL si pas de type)
			String type = calId.getType();
			if (StringUtils.isEmpty(type)) { 
				requete.setNull(3, java.sql.Types.INTEGER);
			}
			else {
				// Récupération de l'id du type
				PreparedStatement type_id_prepare = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.typecalendrier WHERE typecal_libelle=?");
				type_id_prepare.setString(1, type);
				int type_id = _bdd.recupererId(type_id_prepare, "typecal_id");
				if (type_id == -1) { //le nom du type match aucun/plusieurs id -> ERREUR
					throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID type non existant ou non unique");
				}
				else {
					// Ajout à la requete préparée
					requete.setInt(3, type_id);
				}
				
				
			}
			
			// Executer la requete de modification du calendrier
			requete.executeUpdate();
			
			// Requete préparée pour la modification du groupe unique associé
			PreparedStatement requeteGpe = _bdd.getConnection().prepareStatement(
					"UPDATE edt.groupeparticipant AS gp "
					+ "SET (groupeparticipant_nom) = (?) "
					+ "FROM edt.calendrierappartientgroupe AS cag "
					+ "WHERE gp.groupeparticipant_id = cag.groupeparticipant_id "
					+ "AND cag.cal_id = " + calId.getId() + " "
					+ "AND gp.groupeparticipant_estcalendrierunique = TRUE "
					+ "RETURNING gp.groupeparticipant_id");
			// Ajout du nom du groupe unique à la requete préparée
			requeteGpe.setString(1, calId.getNom());
			// Executer modification du groupe unique associé et récupérer ID du groupe unique
			int idGroupeUnique = _bdd.recupererId(requeteGpe, "groupeparticipant_id");
			
			/* Parcours des anciens rattachements (hormis le groupe unique associé) :
			 *     Si le parent est dans la liste idGroupesParents, 
			 *     		- ne rien faire sur la base = parent non modifié
			 *     		- supprimer l'id de la liste idParentsGroupes
			 *     Si le parent n'est pas dans la liste idGroupesParents, 
			 *     		- supprimer le lien entre le calendrier et le groupe parent (dans la BDD)
			 *     
			 * Une fois le parcourt terminé, les éléments restants dans idParentsGroupes sont de nouveaux parents du calendrier
			 */
			PreparedStatement requeteAnciensRattachements = _bdd.getConnection().prepareStatement(
					  "SELECT groupeparticipant_id, groupeparticipant_id_tmp "
					+ "FROM edt.calendrierappartientgroupe "
					+ "WHERE cal_id = ? "
					+ "AND (groupeparticipant_id != " + idGroupeUnique + " OR groupeparticipant_id IS NULL)"
					);
			requeteAnciensRattachements.setInt(1, calId.getId());
			ResultSet rs_ancienRattachements = requeteAnciensRattachements.executeQuery();
			while(rs_ancienRattachements.next()){
				// On initialise idGroupe avec la bonne valeur (qui se trouve dans groupeparticipant_id ou groupeparticipant_id_tmp)
				int idGroupe = rs_ancienRattachements.getInt("groupeparticipant_id");
				if (rs_ancienRattachements.wasNull()) {
					idGroupe = rs_ancienRattachements.getInt("groupeparticipant_id_tmp");
				}
				// Si l'id du parent parcouru est dans la liste idGroupesParents
				Integer idGroupeInteger = new Integer(idGroupe);
				if (idGroupesParents.contains(idGroupeInteger)) {
					idGroupesParents.remove(idGroupeInteger);
				}
				//Sinon
				else {
					_bdd.executeUpdate(
						"DELETE FROM edt.calendrierappartientgroupe "
						+ "WHERE cal_id = " + calId.getId()
						+ " AND (groupeparticipant_id = " + idGroupe
						+ " OR groupeparticipant_id_tmp = " + idGroupe + ")"
					);
				}
			}
			// Ajout des rattachements restants
			Iterator<Integer> itr = idGroupesParents.iterator();
			while (itr.hasNext()){
				int idGroupeARattacher = itr.next();
				// Requête pour savoir si le créateur est propriétaire du groupe parent 
				ResultSet estProprietaireGroupeParent = _bdd.getConnection().prepareStatement(
						"SELECT * FROM edt.proprietairegroupeparticipant"
						+ " WHERE groupeparticipant_id="+idGroupeARattacher
						+ " AND utilisateur_id="+calId.getIdCreateur()  //l'attribut "créateur" contient l'id du propriétaire effectuant la modification (cf Servlet)
				).executeQuery();
				//Si le créateur est propriétaire (ie si le resultat de la requete est non vide)
				if (estProprietaireGroupeParent.next()) {
					_bdd.executeUpdate(
							"INSERT INTO edt.calendrierappartientgroupe (groupeparticipant_id, cal_id) "
							+ "VALUES (" + idGroupeARattacher + ", " + calId.getId() + ")"
							);
				}
				//S'il n'est pas propriétaire
				else {
					_bdd.executeUpdate(
							"INSERT INTO edt.calendrierappartientgroupe (groupeparticipant_id_tmp, cal_id) "
							+ "VALUES (" + idGroupeARattacher + ", " + calId.getId() + ")"
							);
				}
				
			}
			
			// Supprimer ancienne liste de propriétaires du calendrier
			_bdd.executeUpdate("DELETE FROM edt.proprietairecalendrier WHERE cal_id = " + calId.getId());
			
			// Ajouter nouvelle liste de propriétaires du calendrier		
			Iterator<Integer> itrProprios = calId.getIdProprietaires().iterator();
			while (itrProprios.hasNext()){
				int idProprio = itrProprios.next();
				_bdd.executeUpdate(
						"INSERT INTO edt.proprietairecalendrier "
						 + "(utilisateur_id, cal_id) "
						 + "VALUES (" + idProprio + ", " + calId.getId() + ") " 
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
	 * Supprimer un calendrier dans la base de données 
	 * @param idCalendrier Identifiant du calendrier
	 * @param startTransaction Démarrer une transaction dans cette méthode, sinon elle doit être appelée à l'intérieur d'une transaction
	 * @throws EdtempsException
	 */
	public void supprimerCalendrier(int idCalendrier, boolean startTransaction) throws DatabaseException {
		
		try {
			if(startTransaction) {
				_bdd.startTransaction();
			}
			
			// Supprimer liste de propriétaires du calendrier
			_bdd.executeUpdate(
					"DELETE FROM edt.proprietairecalendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Trouver l'id du groupe unique associé 
			PreparedStatement requeteIdGpe = _bdd.getConnection().prepareStatement(
					  "SELECT gp.groupeparticipant_id "
					+ "FROM  edt.groupeparticipant AS gp "
					+ "INNER JOIN edt.calendrierappartientgroupe AS cag "
					+ "ON gp.groupeparticipant_id = cag.groupeparticipant_id "
					+ "WHERE gp.groupeparticipant_estcalendrierunique = TRUE "
					+ "AND cag.cal_id = " + idCalendrier
					);
			int idGroupeUnique = _bdd.recupererId(requeteIdGpe, "groupeparticipant_id");
			if (idGroupeUnique == -1) {
				throw new DatabaseException("ID groupe unique associé au calendrier à supprimer non existant ou non unique"); 
			}
			// Supprimer dépendance avec les groupes de participants
			_bdd.executeUpdate(
					"DELETE FROM edt.calendrierAppartientGroupe "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Supprimer les abonnements au groupe unique 
			_bdd.executeUpdate("DELETE FROM edt.abonnegroupeparticipant WHERE groupeparticipant_id=" + idGroupeUnique);
			// Supprime le groupe unique
			_bdd.executeUpdate("DELETE FROM edt.groupeparticipant WHERE groupeparticipant_id=" + idGroupeUnique);
			/* Supprimer les événements associés au calendrier
			 * 		1 - Récupération des id des evenements associés
			 * 		2 - Suppression du lien entre les evenements et le calendrier
			 * 		3 - Suppression des evenements eux-même
			 */
			ResultSet rs_evenementsAssocies = _bdd.executeRequest(
					"SELECT * FROM  edt.evenementAppartient "
					+ "WHERE cal_id = " + idCalendrier 
					);
			_bdd.executeUpdate(
					"DELETE FROM edt.evenementAppartient "
					 + "WHERE cal_id = " + idCalendrier  
					 );
			while(rs_evenementsAssocies.next()){
				EvenementGestion eveGestionnaire = new EvenementGestion(this._bdd);
				eveGestionnaire.supprimerEvenement(rs_evenementsAssocies.getInt("eve_id"), false);
			}
			// Supprimer calendrier
			_bdd.executeUpdate(
					"DELETE FROM edt.calendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			
			if(startTransaction) {
				_bdd.commit();
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}	
	
	
	/**
	 * Supprimer le lien "propriétaire" entre un utilisateur et un calendrier
	 * @param idCalendrier Identifiant du calendrier
	 * @param idProprietaire Identifiant du propriétaire
	 * @throws EdtempsException 
	 */
	public void nePlusEtreProprietaire(int idCalendrier, int idProprietaire) throws EdtempsException {
		try {
			// Requete préparée
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"DELETE FROM edt.proprietairecalendrier "
					+ "WHERE cal_id = ? "
					+ "AND utilisateur_id = ?");
			// Ajout des paramètres à la requete préparée
			req.setInt(1, idCalendrier);
			req.setInt(2, idProprietaire);
			req.executeQuery();
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
	}
	
	
	/**
	 * Listing des types de calendrier disponibles dans la base de données
	 * @return Hashmap indiquant les ID (clé) et nom (string) des types de calendrier disponibles
	 * @throws DatabaseException
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
	 * @return Hashmap indiquant les ID (clé) et nom (string) des matières disponibles
	 * @throws DatabaseException 
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
	 * @param createTransaction Vrai s'il faut créer une transaction
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return Liste des calendriers
	 * @throws EdtempsException
	 */
	public ArrayList<CalendrierIdentifie> listerCalendriersAbonnementsUtilisateur(int userId, boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
		try {
			if(createTransaction)
				_bdd.startTransaction();
			
			if(!reuseTempTableAbonnements)
				GroupeGestion.makeTempTableListeGroupesAbonnement(_bdd, userId);
			
			// Récupération des calendriers des collections abonnement
			ResultSet results = _bdd.executeRequest("SELECT calendrier.cal_id, calendrier.cal_nom, calendrier.cal_createur, matiere.matiere_nom, typecalendrier.typecal_libelle, calendrier.cal_createur FROM edt.calendrier " +
					"LEFT JOIN edt.matiere ON calendrier.matiere_id = matiere.matiere_id " +
					"LEFT JOIN edt.typecalendrier ON typecalendrier.typecal_id = calendrier.typecal_id " +
					"INNER JOIN edt.calendrierappartientgroupe appartenance ON appartenance.cal_id=calendrier.cal_id " +
					"INNER JOIN " + GroupeGestion.NOM_TEMPTABLE_ABONNEMENTS + " tmpAbonnements ON tmpAbonnements.groupeparticipant_id=appartenance.groupeparticipant_id");
			
			ArrayList<CalendrierIdentifie> res = new ArrayList<CalendrierIdentifie>();
			
			while(results.next()) {
				res.add(new CalendrierIdentifieInflater().inflateCalendrier(results, _bdd));
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
	 * @throws DatabaseException
	 */
	public ArrayList<CalendrierComplet> listerCalendriersUtilisateur(int userId) throws DatabaseException {
		ResultSet results = _bdd.executeRequest("SELECT calendrier.cal_id, calendrier.cal_nom, calendrier.cal_createur, matiere.matiere_nom, typecalendrier.typecal_libelle, " +
				"COUNT(groupecours.groupeparticipant_id) > 0 AS estcours " + // Nombre de groupes "cours" auquel le calendrier est rattaché > 0
				"FROM edt.calendrier " +
				"LEFT JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id " +
				"LEFT JOIN edt.typecalendrier ON typecalendrier.typecal_id = calendrier.typecal_id " +
				"INNER JOIN edt.proprietairecalendrier ON calendrier.cal_id = proprietairecalendrier.cal_id AND proprietairecalendrier.utilisateur_id = " + userId + 
				"LEFT JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = calendrier.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id=calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours)" +
				"GROUP BY calendrier.cal_id, calendrier.cal_nom, matiere.matiere_nom, typecalendrier.typecal_libelle " +
				"ORDER BY calendrier.cal_nom");
		
		try {
			ArrayList<CalendrierComplet> res = new ArrayList<CalendrierComplet>();
			while (results.next()) {
				// Création du calendrier complet avec la ligne de base de données
				CalendrierComplet calendrier = new CalendrierCompletInflater().inflateCalendrier(results, _bdd);
				
				// Ajoute le calendrier au résultat
				res.add(calendrier);
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
	 * @param userId Identifiant de l'utilisateur
	 * @param calendriersIds Liste des identifiants des calendriers à vérifier
	 * @return résumé des droits des calendriers indiqués
	 */
	public DroitsCalendriers getDroitsCalendriers(int userId, List<Integer> calendriersIds) throws DatabaseException {
		
		if(calendriersIds.size() == 0) {
			return new DroitsCalendriers(true, false);
		}
		
		String strIds = StringUtils.join(calendriersIds, ",");
		
		ResultSet results = _bdd.executeRequest("SELECT COUNT(DISTINCT calendrier.cal_id) AS nb_calendriers_proprietaire, " +
					"COUNT(groupecours.groupeparticipant_id) AS nb_groupe_cours " +
				"FROM edt.proprietairecalendrier " +
				"LEFT JOIN edt.calendrier ON proprietairecalendrier.cal_id=calendrier.cal_id " +
					"AND proprietairecalendrier.utilisateur_id=" + userId +
				" LEFT JOIN edt.calendrierappartientgroupe ON proprietairecalendrier.cal_id=calendrierappartientgroupe.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id=calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) " +
				"WHERE proprietairecalendrier.cal_id IN (" + strIds + ")");
		
		try {
			results.next();
			
			return new DroitsCalendriers(results.getInt(1) == calendriersIds.size(), results.getInt(2) > 0);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	/**
	 * Récupère la liste des calendriers d'un groupe de participants
	 * @param groupeId Identifiant du groupe à traiter
	 * @return Liste des calendriers du groupe
	 * @throws DatabaseException
	 */
	public List<CalendrierIdentifie> listerCalendriersGroupeParticipants(int groupeId) throws DatabaseException {
		ResultSet reponse = _bdd.executeRequest("SELECT calendrier.cal_id, calendrier.matiere_id, calendrier.cal_nom, calendrier.typecal_id, calendrier.cal_createur, matiere.matiere_nom, typecalendrier.typecal_libelle" +
				" FROM edt.calendrier" +
				" INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id=calendrier.cal_id" +
				" AND calendrierappartientgroupe.groupeparticipant_id=" + groupeId +
				" LEFT JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id" +
				" LEFT JOIN edt.typecalendrier ON typecalendrier.typecal_id=calendrier.typecal_id");

		try {
			List<CalendrierIdentifie> res = new ArrayList<CalendrierIdentifie>();
			while(reponse.next()) {
				res.add(new CalendrierIdentifieInflater().inflateCalendrier(reponse, _bdd));
			}
			
			reponse.close();
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Récupérer le nombre de calendriers créés par un utilisateur
	 * @param userId Identifiant de l'utilisateur
	 * @return nombre de groupes
	 * @throws DatabaseException 
	 */
	public int getNombresCalendriersCres(int userId) throws DatabaseException {
		try {
			ResultSet res = _bdd.executeRequest("SELECT COUNT(*) FROM edt.calendrier WHERE cal_createur="+userId);
			res.next();
			return res.getInt(1);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
