package org.ecn.edtemps.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;

/**
 * Classe d'outils pour se connecter à la base de données et exécuter des
 * requêtes
 * 
 * @author Joffrey
 */
public class BddGestion {
	
	private static Connection _connection;

	/**
	 * Méthode de connexion à la base de données
	 * 
	 * @return le connecteur
	 * 
	 * @throws DatabaseException
	 *             si une erreur intervient lors de la connexion
	 */
	public static Connection connect() throws DatabaseException {
		
		if(_connection != null)
			return _connection;

		try {

			Class.forName("org.postgresql.Driver");
			System.out.println("Driver chargé");

			String url = "jdbc:postgresql://localhost:5432/edtemps-ecn";
			String user = "edtemps-ecn";
			String passwd = "passwordEdtemps";

			_connection = DriverManager.getConnection(url, user, passwd);
			System.out.println("Connexion OK");

		} catch (Exception e) {
			throw new DatabaseException(e);
		}

		return _connection;
	}

	/**
	 * Exécuter une requête SQL
	 * 
	 * @param request
	 *            requête à effectuer
	 * 
	 * @return le résultat de la requête ou NULL si la requête ne renvoie rien
	 *         ou si la requete est vide
	 * 
	 * @throws DatabaseException
	 *             si une erreur intervient lors d'exécution de la requête
	 */
	public static ResultSet executeRequest(String request)
			throws DatabaseException {

		ResultSet resultat = null;

		if (StringUtils.isNotBlank(request)) {
			try {

				// Connexion à la base de données
				Connection connexion = connect();

				// Préparation de la requête
				PreparedStatement requetePreparee = connexion
						.prepareStatement(request);

				// Exécute la requête et récupère le résultat s'il y en a un
				if (requetePreparee.execute()) {
					resultat = requetePreparee.getResultSet();
				}

			} catch (Exception e) {
				throw new DatabaseException(e);
			}
		}

		return resultat;
	}

	
	/**
	 * Récupérer l'ID (contenu dans la colonne <nomColonne>) d'une ligne spécifique d'une table
	 * 
	 * @param requete : requête à effectuer, devant retourner une et une seule ligne 
	 * @param nomColonne : nom de la colonne contenant l'id
	 * 
	 * @return id : id de la ligne cherchée 
	 * 			  ou -1 si le résultat n'existe pas / n'est pas unique
	 * 
	 * @throws DatabaseException
	 *            si une erreur intervient lors d'exécution de la requête
	 */
	public static int recupererId(String request, String nomColonne)
			throws DatabaseException {
		
		int id = -1;
		
		try {
			// Execution requete de récupération de la ligne cherchée
			ResultSet resultat = BddGestion.executeRequest(request);
			// Parcourt du resultat
			while(resultat.next()){
				 id = resultat.getInt(nomColonne);
			}
			// Si le nombre de lignes du ResultSet n'est pas égal à 1 : erreur ! On renvoie -1
			if (resultat.getRow() != 1) {
				id = -1;
			}
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
		
	
		return id;
		
	}

	
	
}
