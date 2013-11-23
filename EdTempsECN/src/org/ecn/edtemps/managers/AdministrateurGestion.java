package org.ecn.edtemps.managers;

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
	
	/**
	 * Initialise un gestionnaire
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


}
