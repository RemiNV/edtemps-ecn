package org.ecn.edtemps.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;

/**
 * Classe d'outils pour se connecter à la base de données et exécuter des
 * requêtes
 * 
 * @author Joffrey
 */
public class BddGestion {

	/**
	 * Méthode de connexion à la base de données
	 * 
	 * @return le connecteur
	 * 
	 * @throws DatabaseException
	 *             si une erreur intervient lors de la connexion
	 */
	public static Connection connect() throws DatabaseException {

		Connection connexion = null;

		try {

			Class.forName("org.postgresql.Driver");
			System.out.println("Driver chargé");

			String url = "jdbc:postgresql://localhost:5432/edtemps-ecn";
			String user = "edtemps-ecn";
			String passwd = "passwordEdtemps";

			connexion = DriverManager.getConnection(url, user, passwd);
			System.out.println("Connexion OK");

		} catch (Exception e) {
			throw new DatabaseException(e);
		}

		return connexion;

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
					resultat.close();
				}

				// Fermeture de la requête
				requetePreparee.close();

			} catch (Exception e) {
				throw new DatabaseException(e);
			}
		}

		return resultat;

	}

	public static void main(String[] arg) throws DatabaseException {
		executeRequest("INSERT INTO edt.salle (salle_id, salle_batiment, salle_niveau, salle_numero, salle_capacite) VALUES (4, 'B', 1, 8, 60)");
		executeRequest("SELECT * FROM edt.salle");
	}

}
