package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
	 * Supprime un administrateur
	 * @param id Identifiant de l'administrateur à supprimer
	 * @throws DatabaseException 
	 */
	public void supprimerAdministrateur(int id) throws DatabaseException {
		
		bdd.executeRequest("DELETE FROM edt.administrateurs WHERE admin_id="+id);
		
	}

}
