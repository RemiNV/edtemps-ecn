package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;


/**
 * Classe de gestion des administrateurs
 * 
 * @author Joffrey
 */
public class AdministrateurGestion {
	
	/** Gestionnaire de base de données */
	protected BddGestion bdd;

	private static final String KEY_CRYPTAGE_PASSWORD = "Chaine de cryptage";
	
	/**
	 * Initialise un gestionnaire d'administrateurs
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public AdministrateurGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Lister tous les administrateurs de la base de données
	 * @return la liste des administrateurs
	 * @throws DatabaseException En cas d'erreur avec la base de données
	 */
	public Map<Integer, String> listerAdministrateurs() throws DatabaseException {
		
		try  {
			Map<Integer, String> liste = new HashMap<Integer, String>();

			// Exécute la requête
			ResultSet requete = bdd.executeRequest("SELECT admin_id, admin_login FROM edt.administrateurs ORDER BY admin_login");
			
			// Rempli la map de résultat
			while (requete.next()) {
				liste.put(requete.getInt("admin_id"), requete.getString("admin_login"));
			}

			return liste;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}
	
	
	/**
	 * Se connecter à l'espace d'administration
	 * @param login Identifiant de connexion
	 * @param password Mot de passe
	 * @return VRAI si la connexion est réussie
	 * @throws DatabaseException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	public boolean seConnecter(String login, String password) throws DatabaseException, InvalidKeyException, NoSuchAlgorithmException {

		try {
			
			String cryptedPassword = UtilisateurGestion.hmac_sha256(KEY_CRYPTAGE_PASSWORD, password);
			
			// Prépare la requête
			PreparedStatement reqPreparee = bdd.getConnection().prepareStatement(
					"SELECT * FROM edt.administrateurs WHERE admin_login=? AND admin_password=?");
			reqPreparee.setString(1, login);
			reqPreparee.setString(2, cryptedPassword);

			// Exécute la requête
			ResultSet reqResultat = reqPreparee.executeQuery();
			
			return (reqResultat.next());
			
		} catch (SQLException e) {
			throw new DatabaseException(e); 
		}

	}
	
	
	/**
	 * Ajouter un administrateur
	 * @param login Identifiant de connexion
	 * @param password Mot de passe
	 * @return l'identifiant de l'administrateur ajouté
	 * @throws DatabaseException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public int ajouterAdministrateur(String login, String password) throws DatabaseException, InvalidKeyException, NoSuchAlgorithmException {

		try {
			
			String cryptedPassword = UtilisateurGestion.hmac_sha256(KEY_CRYPTAGE_PASSWORD, password);
			
			// Prépare la requête
			PreparedStatement reqPreparee = bdd.getConnection().prepareStatement(
					"INSERT INTO edt.administrateurs (admin_login, admin_password) VALUES (?,?) RETURNING admin_id");
			reqPreparee.setString(1, login);
			reqPreparee.setString(2, cryptedPassword);

			// Exécute la requête
			ResultSet resultat = reqPreparee.executeQuery();

			// Récupère l'identifiant de la ligne ajoutée
			resultat.next();
			int idInsertion = resultat.getInt(1);
			resultat.close();

			return idInsertion;
		} catch (SQLException e) {
			throw new DatabaseException(e); 
		}
		
	}

	
	/**
	 * Supprimer un administrateur
	 * @param id Identifiant de l'administrateur à supprimer
	 * @throws DatabaseException 
	 */
	public void supprimerAdministrateur(int id) throws DatabaseException {
		
		bdd.executeRequest("DELETE FROM edt.administrateurs WHERE admin_id="+id);
		
	}


	/**
	 * Lister les actions de l'emploi du temps
	 * @return la liste des actions sous forme d'une map : identifiant <> libellé
	 * @throws DatabaseException 
	 */
	public Map<Integer, String> listerActionsEdtemps() throws DatabaseException {

		try {

			// Exécute la requête
			ResultSet liste = bdd.executeRequest("SELECT droits_id, droits_libelle FROM edt.droits");

			Map<Integer, String> resultat = new HashMap<Integer, String>();
			while (liste.next()) {
				resultat.put(liste.getInt("droits_id"), liste.getString("droits_libelle"));
			}
			liste.close();
			
			return (resultat);
			
		} catch (SQLException e) {
			throw new DatabaseException(e); 
		}

	}
	

	/**
	 * Récupère la liste actions autorisées pour un type d'utilisateur
	 * @param idType identifiant du type d'utilisateur
	 * @return la liste des identifiants des actions que l'utilisateur peut réaliser
	 * @throws DatabaseException
	 */
	public List<Integer> getListeActionsTypeUtilisateurs(int idType) throws DatabaseException {
		
		try {
			ResultSet liste = bdd.executeRequest(
					"SELECT droits.droits_id, droits.droits_libelle  "
					+ "FROM edt.droits "
					+ "INNER JOIN edt.aledroitde ON droits.droits_id = aledroitde.droits_id "
					+ "INNER JOIN edt.typeutilisateur ON typeutilisateur.type_id = aledroitde.type_id "
					+ "WHERE typeutilisateur.type_id="+idType);

			List<Integer> resultat = new ArrayList<Integer>();
			while (liste.next()) {
				resultat.add(liste.getInt("droits_id"));
			}
			liste.close();
			
			return (resultat);
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}

	
	/**
	 * Modifier les drois pour un type d'utilisateur
	 * @param idType identifiant du type d'utilisateur
	 * @param listeIdDroits liste des identifiants des actions autorisées
	 * @param nom Nouveau nom du type
	 * @throws DatabaseException
	 */
	public void modifierDroitsTypeUtilisateurs(int idType, List<Integer> listeIdDroits, String nom) throws DatabaseException {
		
		if(StringUtils.isBlank(nom)) {
			throw new DatabaseException("Le nom du type d'utilisateur doit être spécifié");
		}

		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new DatabaseException("Le nom du type d'utilisateur doit être alphanumérique");
		}

		try {

			// Démarre une transaction
			bdd.startTransaction();
		
			// Vérifie que le nom n'est pas déjà en base de données
			PreparedStatement nomDejaPris = bdd.getConnection().prepareStatement("SELECT type_id FROM edt.typeutilisateur WHERE type_libelle=?");
			nomDejaPris.setString(1, nom);
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			if (nomDejaPrisResult.next()) {
				if (nomDejaPrisResult.getInt("type_id")!=idType) {
					throw new DatabaseException("Le nom du type d'utilisateur doit être unique");
				}
			}
		
			// Modifie le nom du type
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement("UPDATE edt.typeutilisateur SET type_libelle=? WHERE type_id="+idType);
			requetePreparee.setString(1, nom);
			requetePreparee.execute();
			
			// Supprime les anciens droits
			bdd.executeRequest("DELETE FROM edt.aledroitde WHERE type_id="+idType);
	
			// Ajoute les nouveaux droits
			if (CollectionUtils.isNotEmpty(listeIdDroits)) {
				StringBuilder requete = new StringBuilder("INSERT INTO edt.aledroitde (type_id, droits_id) VALUES ");
				for (Integer idDroit : listeIdDroits) {
					requete.append("("+idType+","+idDroit+"), ");
				}
				
				// Exécute la requête (en supprimant les deux derniers caractères : ', '
				bdd.executeRequest(requete.toString().substring(0, requete.length()-2));
			}
		
			// Commit la transaction
			bdd.commit();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

	}
	

	/**
	 * Ajouter un type d'utilisateur
	 * @param nom Nom du type d'utilisateur
	 * @throws DatabaseException
	 */
	public void ajouterTypeUtilisateurs(String nom) throws DatabaseException {
		
		if(StringUtils.isBlank(nom)) {
			throw new DatabaseException("Le nom du type d'utilisateur doit être spécifié");
		}

		if(!StringUtils.isAlphanumericSpace(nom)) {
			throw new DatabaseException("Le nom du type d'utilisateur doit être alphanumérique");
		}

		try {

			// Démarre une transaction
			bdd.startTransaction();

			// Vérifie que le nom n'est pas déjà en base de données
			PreparedStatement nomDejaPris = bdd.getConnection().prepareStatement("SELECT * FROM edt.typeutilisateur WHERE type_libelle=?");
			nomDejaPris.setString(1, nom);
			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
			if (nomDejaPrisResult.next()) {
				throw new DatabaseException("Le nom du type d'utilisateur doit être alphanumérique");
			}

			// Ajouter le type
			PreparedStatement ajout = bdd.getConnection().prepareStatement("INSERT INTO edt.typeutilisateur (type_libelle) VALUES (?)");
			ajout.setString(1, nom);
			ajout.execute();
			
			// Termine une transaction
			bdd.commit();
				
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}
	

	/**
	 * Supprimer un type d'utilisateur
	 * @param typeId Identifiant du type à supprimer
	 * @throws DatabaseException
	 */
	public void supprimerTypeUtilisateurs(int typeId) throws DatabaseException {

		// Démarre une transaction
		bdd.startTransaction();

		// Supprimer les liens avec les utilisateurs
		bdd.executeRequest("DELETE FROM edt.estdetype WHERE type_id="+typeId);

		// Supprimer le type
		bdd.executeRequest("DELETE FROM edt.typeutilisateur WHERE type_id="+typeId);
		
		// Termine une transaction
		bdd.commit();

	}
}
