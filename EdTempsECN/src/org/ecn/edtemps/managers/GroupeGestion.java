package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.identifie.GroupeComplet;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifieAbonnement;
import org.ecn.edtemps.models.inflaters.GroupeCompletInflater;
import org.ecn.edtemps.models.inflaters.GroupeIdentifieInflater;

/**
 * Classe de gestion des groupes de participants
 * 
 * @author Joffrey
 */
public class GroupeGestion {
	
	/** Gestionnaire de base de données */
	protected BddGestion _bdd;

	public static final String NOM_TEMPTABLE_ABONNEMENTS = "tmp_requete_abonnements_groupe";

	/**
	 * Initialise un gestionnaire de groupes de participants
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public GroupeGestion(BddGestion bdd) {
		_bdd = bdd;
	}
	

	/**
	 * Récupérer un groupe identifié de participants dans la base de données
	 * 
	 * @param identifiant
	 *            identifiant du groupe à récupérer
	 * 
	 * @return le groupe identifié (standard)
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public GroupeIdentifie getGroupe(int identifiant) throws EdtempsException {

		GroupeIdentifie groupeRecupere = null;

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère le groupe en base
			ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp," +
							"groupeparticipant_estcours, groupeparticipant_estcalendrierunique" +
							" FROM edt.groupeparticipant WHERE groupeparticipant_id="+identifiant);

			// Accède au premier élément du résultat
			if(requeteGroupe.next()) {
				groupeRecupere = new GroupeIdentifieInflater().inflateGroupe(requeteGroupe, _bdd);
				
				requeteGroupe.close();
			}

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return groupeRecupere;
	}
	

	/**
	 * Récupérer un groupe complet de participants dans la base de données
	 * 
	 * @param identifiant
	 *            identifiant du groupe à récupérer
	 * 
	 * @return le groupe complet
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public GroupeComplet getGroupeComplet(int identifiant) throws EdtempsException {

		GroupeComplet groupeRecupere = null;

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère le groupe en base
			ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp," +
							"groupeparticipant_estcours, groupeparticipant_estcalendrierunique" +
							" FROM edt.groupeparticipant WHERE groupeparticipant_id="+identifiant);

			// Accède au premier élément du résultat
			if(requeteGroupe.next()) {
				groupeRecupere = new GroupeCompletInflater().inflateGroupe(requeteGroupe, _bdd);
				requeteGroupe.close();
			}

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return groupeRecupere;
	}
	
	
	/**
	 * Groupe à enregistrer en base de données
	 * 
	 * @param nom
	 *            nom du groupe de participants à ajouter
	 * @param idGroupeParent
	 *            identifiant du groupe parent du groupe de participants à ajouter
	 * @param rattachementAutorise
	 *            VRAI si le groupe de participants à ajouter accepte le rattachement
	 * @param estCours
	 *            VRAI si le groupe de participants à ajouter est un cours
	 * @param listeIdProprietaires
	 *            liste des identifiants des propriétaires du groupe de participants à ajouter
	 * 
	 * @return l'identifiant de la ligne insérée
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public int sauverGroupe(String nom, Integer idGroupeParent, boolean rattachementAutorise, boolean estCours, List<Integer> listeIdProprietaires, int userId) throws EdtempsException {

		int idInsertion = -1;
		
		if(StringUtils.isBlank(nom) || CollectionUtils.isEmpty(listeIdProprietaires)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe doit avoir un nom et au moins un responsable");
		}

		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un groupe doit être alphanumérique");
		}

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Vérifie que le nom n'est pas déjà en base de données
			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT COUNT(*) FROM edt.groupeparticipant WHERE groupeparticipant_nom=?");
			nomDejaPris.setString(1, nom);
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			nomDejaPrisResult.next();
			if (nomDejaPrisResult.getInt(1)>0) {
				throw new EdtempsException(ResultCode.NAME_TAKEN,
						"Tentative d'enregistrer un groupe en base de données avec un nom déjà utilisé.");
			}
			
			// Prépare la requête avec un traitement différent si un groupe parent a été indiqué (else) 
			PreparedStatement req = null;
			if (idGroupeParent == null) {
				req = _bdd.getConnection().prepareStatement("INSERT INTO edt.groupeparticipant (groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estcalendrierunique, groupeparticipant_estcours) VALUES (" +
						"?, '" + rattachementAutorise + "', 'FALSE', '"+ estCours +"') RETURNING groupeparticipant_id ");
				req.setString(1, nom);
			} else {
				// Requête pour récupérer le propriétaire du groupe parent 
				ResultSet idProprietaireGroupeParent = _bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+idGroupeParent).executeQuery();
				idProprietaireGroupeParent.next();
				
				// Préparation de la requête avec un idParent temporaire si le propriétaire du groupe parent n'est pas l'utilisateur en cours 
				req = _bdd.getConnection().prepareStatement("INSERT INTO edt.groupeparticipant (groupeparticipant_nom, groupeparticipant_rattachementautorise, "+(idProprietaireGroupeParent.getInt(1)==userId ? "groupeparticipant_id_parent" : "groupeparticipant_id_parent_tmp")+", groupeparticipant_estcalendrierunique, groupeparticipant_estcours) VALUES (" +
						"?, '"	+ rattachementAutorise + "', " + idGroupeParent + ", 'FALSE', '"+ estCours +"') RETURNING groupeparticipant_id");
				req.setString(1, nom);
			}

			// Exécute la requête
			ResultSet resultat = req.executeQuery(); 

			// Récupère l'identifiant de la ligne ajoutée
			resultat.next();
			idInsertion = resultat.getInt(1);
			resultat.close();

			// Ajout des propriétaires
			if (CollectionUtils.isNotEmpty(listeIdProprietaires)) {
				for (Integer idProprietaire : listeIdProprietaires) {
					_bdd.executeRequest("INSERT INTO edt.proprietairegroupeparticipant (utilisateur_id, groupeparticipant_id) VALUES (" +
							idProprietaire + ", "	+
							idInsertion	+ ")");
				}
			} else {
				throw new EdtempsException(ResultCode.DATABASE_ERROR,
						"Tentative d'enregistrer un groupe en base de données sans propriétaire.");
			}

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return idInsertion;
	}


	/**
	 * Groupe à modifier en base de données
	 * 
	 * @param id
	 *            identifiant du groupe de participants à modifier
	 * @param nom
	 *            nom du groupe de participants à modifier
	 * @param idGroupeParent
	 *            identifiant du groupe parent du groupe de participants à modifier
	 * @param rattachementAutorise
	 *            VRAI si le groupe de participants à modifier accepte le rattachement
	 * @param estCours
	 *            VRAI si le groupe de participants à modifier est un cours
	 * @param listeIdProprietaires
	 *            liste des identifiants des propriétaires du groupe de participants à modifier
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public void modifierGroupe(int id, String nom, Integer idGroupeParent, boolean rattachementAutorise, boolean estCours, List<Integer> listeIdProprietaires, int userId) throws EdtempsException {

		if(StringUtils.isBlank(nom) || CollectionUtils.isEmpty(listeIdProprietaires)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe doit avoir un nom et au moins un responsable");
		}

		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un groupe doit être alphanumérique");
		}

		if(idGroupeParent != null && idGroupeParent.equals(id)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe ne peut pas être son propre parent");
		}
		
		try {

			// Démarre une transaction
			_bdd.startTransaction();
			
			if (StringUtils.isNotBlank(nom)) {

				// Récupération de l'ancien groupe
				ResultSet ancienGroupeParent = _bdd.executeRequest(
						"SELECT groupeparticipant_nom, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp" +
						" FROM edt.groupeparticipant WHERE groupeparticipant_id="+id);
				ancienGroupeParent.next();
				String ancienNom = ancienGroupeParent.getString("groupeparticipant_nom");
				int ancienIdParent = ancienGroupeParent.getInt("groupeparticipant_id_parent");
				int ancienIdParentTmp = ancienGroupeParent.getInt("groupeparticipant_id_parent_tmp");
				
				// Vérifie que le nom n'est pas déjà en base de données
				if (!StringUtils.equals(nom, ancienNom)) {
					PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT COUNT(*) FROM edt.groupeparticipant WHERE groupeparticipant_nom=?");
					nomDejaPris.setString(1, nom);
					ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
					nomDejaPrisResult.next();
					if (nomDejaPrisResult.getInt(1)>0) {
						throw new EdtempsException(ResultCode.NAME_TAKEN, "Tentative de modifier un groupe en base de données avec un nom déjà utilisé.");
					}
				}
				
				// Prépare la requête avec un traitement différent si un groupe parent a été indiqué (else) 
				PreparedStatement req = null;
				if (idGroupeParent == null) {
					
					// Suppression du lien avec un potentiel groupe parent antérieur
					_bdd.executeUpdate("UPDATE edt.groupeparticipant SET" +
						" groupeparticipant_id_parent=NULL, groupeparticipant_id_parent_tmp=NULL" +
						" WHERE groupeParticipant_id="+id);
					
					// Préparation de la requête
					req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
							" groupeParticipant_nom=?," +
							" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
							" groupeparticipant_estcours='"+estCours+"'," +
							" groupeparticipant_id_parent=NULL" +
							" WHERE groupeParticipant_id="+id);
				} else {
					// S'il n'y a pas eu de modification
					if (idGroupeParent.equals(ancienIdParent) || idGroupeParent.equals(ancienIdParentTmp)) {
						// Préparation de la requête
						req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
								" groupeParticipant_nom=?," +
								" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
								" groupeparticipant_estcours='"+estCours+"'" +
								" WHERE groupeParticipant_id=" + id);
					} else {
						// Requête pour récupérer le propriétaire du groupe parent 
						ResultSet idProprietaireGroupeParent = _bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+idGroupeParent).executeQuery();
						idProprietaireGroupeParent.next();
						
						// Préparation de la requête avec un idParent temporaire si le propriétaire du groupe parent n'est pas l'utilisateur en cours 
						req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
								" groupeParticipant_nom=?," +
								" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
								" groupeparticipant_estcours='"+estCours+"'," +
								" groupeparticipant_id_parent="+(idProprietaireGroupeParent.getInt(1)==userId ? idGroupeParent : "NULL")+"," +
								" groupeparticipant_id_parent_tmp="+(idProprietaireGroupeParent.getInt(1)==userId ? "NULL" : idGroupeParent) +
								" WHERE groupeParticipant_id=" + id);
					}
					
				}
				
				// Execute la requête
				req.setString(1, nom);
				req.execute();

				// Supprime les liens avec les propriétaires
				_bdd.executeRequest("DELETE FROM edt.proprietairegroupeparticipant WHERE groupeParticipant_id=" + id);

				// Ajout des nouveaux propriétaires
				if (CollectionUtils.isNotEmpty(listeIdProprietaires)) {
					for (int idProprietaire : listeIdProprietaires) {
						_bdd.executeRequest("INSERT INTO edt.proprietairegroupeparticipant (utilisateur_id, groupeParticipant_id) VALUES ("
								+ idProprietaire + ", " + id + ")");
					}
				} else {
					throw new EdtempsException(ResultCode.INVALID_OBJECT,
							"Tentative de modifier un groupe en base de données sans propriétaire.");
				}

				
			} else {
				throw new EdtempsException(ResultCode.DATABASE_ERROR,
						"Tentative de modifier un groupe en base de données sans nom.");
			}

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}

	
	
	/**
	 * Supprime un groupe en base de données
	 * 
	 * @param idGroupe
	 *            identifiant du groupe à supprimer
	 * 
	 * @throws EdtempsException
	 *             si le groupe n'est pas supprimable
	 */
	public void supprimerGroupe(int idGroupe) throws EdtempsException {

		// Vérifie si c'est un groupe unique et, le cas échéant, arrêter la suppression
		if (this.getGroupe(idGroupe).estCalendrierUnique()) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, "Impossible de supprimer le groupe unique lié à un calendrier.");
		}

		// Démarre une transaction
		_bdd.startTransaction();

		// Supprime les liens avec les propriétaires
		_bdd.executeRequest("DELETE FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id=" + idGroupe);

		// Supprime les liens avec les calendriers
		_bdd.executeRequest("DELETE FROM edt.calendrierappartientgroupe WHERE groupeparticipant_id=" + idGroupe);

		// Supprime les abonnements
		_bdd.executeRequest("DELETE FROM edt.abonnegroupeparticipant WHERE groupeparticipant_id=" + idGroupe);

		// Pour tous les fils, suppression du parent ID
		_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent=NULL WHERE groupeparticipant_id_parent=" + idGroupe);
		
		// Supprime le groupe
		_bdd.executeRequest("DELETE FROM edt.groupeparticipant WHERE groupeparticipant_id=" + idGroupe);

		// Termine la transaction
		_bdd.commit();
	}

	
	/**
	 * Créé une table temporaire de groupes d'utilisateur contenant les abonnements de l'utilisateur fourni.
	 * Les abonnements listés dans cette table comprennent les abonnements directs, mais aussi les parents et enfants dans l'arbre.
	 * La table temporaire contient les mêmes colonnes que la table groupeparticipant.
	 * Elle est supprimée automatiquement lors d'un commit. Cette méthode doit donc être appelée à l'intérieur d'une transaction.
	 * Le nom de la table créée est défini par la constante {@link GroupeGestion#NOM_TEMPTABLE_ABONNEMENTS}
	 * @param idUtilisateur
	 * @throws DatabaseException
	 */
	public static void makeTempTableListeGroupesAbonnement(BddGestion bdd, int idUtilisateur) throws DatabaseException {
		// Création d'une table temporaire pour les résultats
		bdd.executeRequest("CREATE TEMP TABLE " + NOM_TEMPTABLE_ABONNEMENTS + " (groupeparticipant_id INTEGER NOT NULL, groupeparticipant_nom VARCHAR, " +
				"groupeparticipant_rattachementautorise BOOLEAN NOT NULL,groupeparticipant_id_parent INTEGER, groupeparticipant_id_parent_tmp INTEGER, groupeparticipant_estcours BOOLEAN, " +
				"groupeparticipant_estcalendrierunique BOOLEAN NOT NULL) ON COMMIT DROP");
		
		// Ajout des abonnements directs
		bdd.executeRequest("INSERT INTO tmp_requete_abonnements_groupe(groupeparticipant_id," +
				"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
				"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
				"SELECT groupeparticipant.groupeparticipant_id," +
				"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
				"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique " +
				"FROM edt.groupeparticipant " +
				"INNER JOIN edt.abonnegroupeparticipant ON abonnegroupeparticipant.groupeparticipant_id=groupeparticipant.groupeparticipant_id " +
				"AND abonnegroupeparticipant.utilisateur_id=" + idUtilisateur);
		
		// Ajout des parents et enfants
		int nbInsertions = -1;
		
		// Parents : utilisation d'une requête préparée pour accélérer les traitements consécutifs
		PreparedStatement statementParents;
		try {
			statementParents = bdd.getConnection().prepareStatement("INSERT INTO tmp_requete_abonnements_groupe(groupeparticipant_id," +
					"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
					"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
					"SELECT DISTINCT parent.groupeparticipant_id," +
					"parent.groupeparticipant_nom, parent.groupeparticipant_rattachementautorise," +
					"parent.groupeparticipant_id_parent, parent.groupeparticipant_id_parent_tmp, parent.groupeparticipant_estcours, parent.groupeparticipant_estcalendrierunique " +
					"FROM edt.groupeparticipant parent " +
					"INNER JOIN tmp_requete_abonnements_groupe enfant " +
					"ON parent.groupeparticipant_id=enfant.groupeparticipant_id_parent " +
					"LEFT JOIN tmp_requete_abonnements_groupe deja_inseres " +
					"ON deja_inseres.groupeparticipant_id=parent.groupeparticipant_id " +
					"WHERE deja_inseres.groupeparticipant_id IS NULL");
			
			while(nbInsertions != 0) {
				nbInsertions = statementParents.executeUpdate();
			}
			
			// Enfants
			nbInsertions = -1;
			PreparedStatement statementEnfants = bdd.getConnection().prepareStatement("INSERT INTO tmp_requete_abonnements_groupe(groupeparticipant_id," +
					"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
					"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
					"SELECT DISTINCT enfant.groupeparticipant_id," +
					"enfant.groupeparticipant_nom, enfant.groupeparticipant_rattachementautorise," +
					"enfant.groupeparticipant_id_parent, enfant.groupeparticipant_id_parent_tmp, enfant.groupeparticipant_estcours, enfant.groupeparticipant_estcalendrierunique " +
					"FROM edt.groupeparticipant enfant " +
					"INNER JOIN tmp_requete_abonnements_groupe parent " +
					"ON parent.groupeparticipant_id=enfant.groupeparticipant_id_parent " +
					"LEFT JOIN tmp_requete_abonnements_groupe deja_inseres " +
					"ON deja_inseres.groupeparticipant_id=parent.groupeparticipant_id " +
					"WHERE deja_inseres.groupeparticipant_id IS NULL");
			
			while(nbInsertions != 0) {
				nbInsertions = statementEnfants.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Listing de l'ensemble des groupes de participants existants en base
	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon (false), elle DOIT être appelée à l'intérieur d'une transaction.
	 * @param rattachementAutorise indique s'il faut uniquement lister les groupes dont le rattachement est autorisé
	 * @return Liste de groupes de participants trouvés
	 * @throws DatabaseException
	 */
	public ArrayList<GroupeIdentifie> listerGroupes(boolean createTransaction, boolean rattachementAutorise) throws DatabaseException {
		
		try {
			if(createTransaction){
				_bdd.startTransaction();
			}
			
			// Lecture des groupes de la table
			ResultSet resGroupes = _bdd.executeRequest(
					"SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, "
					+ "groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique "
					+ "FROM edt.groupeparticipant"
					+ (rattachementAutorise ? " WHERE groupeparticipant_rattachementautorise = TRUE" : ""));
			
			// Création d'objets "groupes identifiés" pour les groupes rencontrés dans la table
			ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
			while(resGroupes.next()) {
				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
			}

			if(createTransaction){
				_bdd.commit();
			}
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}

	
	/**
	 * Listing des groupes auxquels est abonné l'utilisateur, soit directement soit indirectement (par parenté d'un groupe à l'autre)
	 * @param idUtilisateur ID de l'utilisateur en question
	 * @param createTransaction Créer une transaction pour les requêtes. Si false, doit obligatoirement être appelé à l'intérieur d'une transaction
	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
	 * 
	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
	 * 
	 * @return Liste de groupes trouvés
	 * @throws DatabaseException
	 */
	public ArrayList<GroupeIdentifie> listerGroupesAbonnement(int idUtilisateur, boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
		
		try {
			if(createTransaction)
				_bdd.startTransaction(); // Définit la durée de vie de la table temporaire
			
			if(!reuseTempTableAbonnements)
				makeTempTableListeGroupesAbonnement(_bdd, idUtilisateur);
			
			// Lecture des groupes de la table
			ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, " +
					"groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique FROM " + NOM_TEMPTABLE_ABONNEMENTS);

			ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
			
			while(resGroupes.next()) {
				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
			}
			
			// Supprime aussi la table temporaire
			if(createTransaction)
				_bdd.commit();
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}

	
	/**
	 * Listing des groupes auxquels n'est pas abonné l'utilisateur directement
	 * @param idUtilisateur ID de l'utilisateur en question
	 * 
	 * @return Liste de groupes trouvés
	 * @throws DatabaseException
	 */
	public ArrayList<GroupeIdentifieAbonnement> listerGroupesNonAbonnement(int idUtilisateur) throws DatabaseException {
		
		try {
			// Requete des groupes auxquels l'utilisateur n'est pas abonné
			ResultSet resGroupes = _bdd.executeRequest(
					"SELECT * FROM edt.groupeparticipant" +
					" LEFT JOIN (SELECT * FROM edt.abonnegroupeparticipant WHERE utilisateur_id = 9) AS gpesUser" + 
					" ON (gpesUser.groupeparticipant_id = groupeparticipant.groupeparticipant_id)" + 
					" WHERE utilisateur_id IS NULL"  
			);
			
			
			ArrayList<GroupeIdentifieAbonnement> res = new ArrayList<GroupeIdentifieAbonnement>();
			
			while(resGroupes.next()) {
				int id = resGroupes.getInt("groupeparticipant_id");
				String nom = resGroupes.getString("groupeparticipant_nom");
				int parentId = resGroupes.getInt("groupeparticipant_id_parent");
				boolean abonnementObligatoire = resGroupes.getBoolean("abonnementgroupeparticipant_obligatoire");
				boolean estCalendrierUnique = resGroupes.getBoolean("groupeparticipant_estcalendrierunique");
				res.add(new GroupeIdentifieAbonnement(id, nom, parentId, estCalendrierUnique, abonnementObligatoire));
			}
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Listing des groupes auxquels n'est pas abonné l'utilisateur directement
	 * @param idUtilisateur ID de l'utilisateur en question
	 * 
	 * @return Liste de groupes trouvés
	 * @throws DatabaseException
	 */
	public ArrayList<GroupeIdentifieAbonnement> listerGroupesAbonnementDirect(int idUtilisateur) throws DatabaseException {
		
		try {
			// Requete des groupes auxquels l'utilisateur est abonné directement
			ResultSet resGroupes = _bdd.executeRequest(
					"SELECT * FROM edt.abonnegroupeparticipant "
					+ "INNER JOIN edt.groupeparticipant "
					+ "ON (abonnegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id)"
					+ "WHERE utilisateur_id = " + idUtilisateur  
			);
			
			ArrayList<GroupeIdentifieAbonnement> res = new ArrayList<GroupeIdentifieAbonnement>();
			
			while(resGroupes.next()) {
				int id = resGroupes.getInt("groupeparticipant_id");
				String nom = resGroupes.getString("groupeparticipant_nom");
				int parentId = resGroupes.getInt("groupeparticipant_id_parent");
				boolean abonnementObligatoire = resGroupes.getBoolean("abonnementgroupeparticipant_obligatoire");
				boolean estCalendrierUnique = resGroupes.getBoolean("groupeparticipant_estcalendrierunique");
				res.add(new GroupeIdentifieAbonnement(id, nom, parentId, estCalendrierUnique, abonnementObligatoire));
			}
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Listing des groupes auxquels l'utilisateur est abonné directement (sans remonter ni descendre les parents/enfants)
	 * @param idUtilisateur Utilisateur dont les abonnements sont à lister
	 * @return Liste des groupes trouvés
	 * @throws DatabaseException Erreur d'accès à la base de données
	 */
	public ArrayList<GroupeIdentifie> listerGroupesAssocies(int idUtilisateur) throws DatabaseException {
		ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique " +
					"FROM edt.groupeparticipant " +
					"INNER JOIN edt.abonnegroupeparticipant ON abonnegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id " +
					"AND abonnegroupeparticipant.utilisateur_id = " + idUtilisateur);
		
		ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();

		try {
			while(resGroupes.next()) {
				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	

	/**
	 * Listing des groupes desquels l'utilisateur fait parti des propriétaires (sans remonter ni descendre les parents/enfants)
	 * @param idUtilisateur Utilisateur dont les groupes sont à lister
	 * @return Liste des groupes trouvés
	 * @throws DatabaseException Erreur d'accès à la base de données
	 */
	public ArrayList<GroupeIdentifie> listerGroupesProprietaire(int idProprietaire) throws DatabaseException {
		
		ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique" +
					" FROM edt.groupeparticipant" +
					" INNER JOIN edt.proprietairegroupeparticipant ON proprietairegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id" +
					" AND groupeparticipant.groupeparticipant_estcalendrierunique=FALSE " +
					" AND proprietairegroupeparticipant.utilisateur_id="+idProprietaire +
					" ORDER BY groupeparticipant.groupeparticipant_nom");
		
		ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();

		try {
			while(resGroupes.next()) {
				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return res;
	}
	
	/**
	 * Fonction permettant à un utilisateur de s'abonner à un groupe de participants
	 * @param idUtilisateur
	 * @param idGroupe
	 * @param obligatoire : booléen indiquant si le rattachement est obligatoire
	 * @throws DatabaseException
	 */
	public void sAbonner(int idUtilisateur, int idGroupe, boolean obligatoire) throws DatabaseException {
		_bdd.executeRequest(
			"INSERT INTO edt.abonnegroupeparticipant "
			+ "(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire) "
			+ "VALUES (" + idUtilisateur + ", " + idGroupe + ", " + obligatoire + ")"
		);
	}
	
	/**
	 * Fonction permettant à un utilisateur de se désabonner d'un groupe de participants
	 * @param idUtilisateur
	 * @param idGroupe
	 * @param uniquementSiCoursNonObligatoire : booléen indiquant si on supprime le rattachement dans le cas où il est obligatoire
	 * @throws DatabaseException
	 */
	public void seDesabonner(int idUtilisateur, int idGroupe, boolean uniquementSiCoursNonObligatoire) throws DatabaseException {
		String s = "DELETE FROM edt.abonnegroupeparticipant " +
				   "WHERE utilisateur_id = " + idUtilisateur +
				   " AND groupeparticipant_id = " + idGroupe ;
		if (uniquementSiCoursNonObligatoire) {
			  s += " AND abonnementgroupeparticipant_obligatoire = FALSE" ;
		}
		_bdd.executeRequest(s);
	}
	
	
	/**
	 * Lister les groupes qui sont en attente de rattachement, pour un utilisateur
	 * @param userId Identifiant de l'utilisateur
	 * @return la liste des groupes
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public List<GroupeComplet> listerDemandesDeRattachement(int userId) throws DatabaseException, SQLException {
		
		List<GroupeComplet> groupesEnAttenteDeValidation = new ArrayList<GroupeComplet>();

		// Démarre une transaction
		_bdd.startTransaction();

		// Récupère les groupes qui sont en attente de rattachement
		ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom," +
				" groupeparticipant.groupeparticipant_rattachementautorise, groupeparticipant.groupeparticipant_id_parent, groupeparticipant.groupeparticipant_id_parent_tmp," +
				" groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique" +
				" FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_id_parent_tmp IN" +
				" (SELECT proprietairegroupeparticipant.groupeparticipant_id" +
				" FROM edt.proprietairegroupeparticipant WHERE proprietairegroupeparticipant.utilisateur_id="+userId+")");

		// Récupère et traite le résultat
		while (requeteGroupe.next()) {
			groupesEnAttenteDeValidation.add(new GroupeCompletInflater().inflateGroupe(requeteGroupe, _bdd));
		}
		requeteGroupe.close();
		
		return groupesEnAttenteDeValidation;
	}
	
	
	/**
	 * Décide du rattachement (accepté ou refusé) à un groupe de participant
	 * @param accepte VRAI si le rattachement est accepté
	 * @param groupeId Identifiant du groupe dont le rattachement a été décidé
	 * @throws DatabaseException 
	 */
	public void deciderRattachement(boolean accepte, int groupeId) throws DatabaseException {
		
		// Démarre une transaction
		_bdd.startTransaction();

		// Décide le rattachement
		if (accepte) {
			_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent=groupeparticipant_id_parent_tmp, groupeparticipant_id_parent_tmp=NULL WHERE groupeparticipant_id="+groupeId);
		} else {
			_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent_tmp=NULL WHERE groupeparticipant_id="+groupeId);
		}

		// Termine la transaction
		_bdd.commit();

	}
}
