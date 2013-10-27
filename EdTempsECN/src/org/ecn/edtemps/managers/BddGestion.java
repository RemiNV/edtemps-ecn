package org.ecn.edtemps.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;

/**
 * Classe d'outils pour se connecter à la base de données et exécuter des
 * requêtes
 * 
 * @author Joffrey
 */
public class BddGestion {
	
	private Connection _connection;
	
	private static Logger logger = LogManager.getLogger(BddGestion.class.getName());

	/**
	 * Méthode de connexion à la base de données
	 * 
	 * @return le connecteur
	 * 
	 * @throws DatabaseException
	 *             si une erreur intervient lors de la connexion
	 */
	private static Connection connect() throws DatabaseException {

		Connection connection;
		try {

			Class.forName("org.postgresql.Driver");
			logger.debug("Driver postgres chargé");

			String url = "jdbc:postgresql://localhost:5432/edtemps-ecn";
			String user = "edtemps-ecn";
			String passwd = "passwordEdtemps";

			connection = DriverManager.getConnection(url, user, passwd);
			logger.debug("Connexion à la base de données réalisée");

		} catch (Exception e) {
			throw new DatabaseException(e);
		}

		return connection;
	}
	
	/**
	 * Constructeur principale. Effectue une connection à la base de données
	 * @throws DatabaseException Erreur de connexion à la base de données
	 */
	public BddGestion() throws DatabaseException {
		_connection = connect();
	}
	
	/**
	 * Fermeture de la connexion associée à cet objet
	 * @throws DatabaseException Erreur de fermeture de la connexion
	 */
	public boolean close() {
		try {
			_connection.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Démarre une transaction avec la connection associée à cet objet BddGestion 
	 * @throws SQLException
	 */
	public void startTransaction() throws SQLException {
		_connection.setAutoCommit(false);
	}
	
	/**
	 * Commit une transaction associée à la connection de cet objet BddGestion
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		_connection.commit();
	}
	
	/**
	 * Rollback une transaction associée à la connection de cet objet BddGestion
	 * @throws SQLException 
	 */
	public void rollback() throws SQLException {
		_connection.rollback();
	}

	/**
	 * Récupération de l'objet Connection pour des usages plus avancées
	 * (requêtes préparées avec paramètres par exemple)
	 * 
	 * Ne PAS fermer cette connexion si le manager est encore utilisé ensuite
	 * @return Objet connection sur lequel est basé ce gestionnaire
	 */
	public Connection getConnection() {
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
	public ResultSet executeRequest(String request)
			throws DatabaseException {

		ResultSet resultat = null;

		if (StringUtils.isNotBlank(request)) {
			try {

				// Préparation de la requête
				PreparedStatement requetePreparee = _connection.prepareStatement(request);
				
				// Exécute la requête et récupère le résultat s'il y en a un
				if (requetePreparee.execute()) {
					resultat = requetePreparee.getResultSet();
				}
			} catch (SQLException e) {
				throw new DatabaseException(e);
			}
		}

		return resultat;
	}

	
	/**
	 * Exécution d'une requête SQL de type INSERT ou UPDATE, indique le nombre de lignes insérées/mises à jour
	 * @param request Requête à exécuter
	 * @return Nombre de lignes mises à jour/insérées, 0 si la requête ne retourne rien, -1 si la requête est vide
	 * @throws DatabaseException 
	 */
	public int executeUpdate(String request) throws DatabaseException  {
		int count = -1;

		if (StringUtils.isNotBlank(request)) {
			try {

				// Préparation de la requête
				PreparedStatement requetePreparee = _connection.prepareStatement(request);

				// Exécute la requête et récupère le résultat s'il y en a un
				count = requetePreparee.executeUpdate();

			} catch (SQLException e) {
				throw new DatabaseException(e);
			}
		}

		return count;
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
	public int recupererId(String request, String nomColonne) throws DatabaseException {
		
		ArrayList<Integer> ids = recupererIds(request, nomColonne);
		
		if(ids.size() != 1) {
			return -1;
		}
		else {
			return ids.get(0);
		}
	}
	
	
	/**
	 * Récupération des IDs (contenus dans la colonne <nomColonne>) des éléments retournés par une requête
	 * @param request Requête à effectuer
	 * @param nomColonne Colonne à examiner pour les IDs
	 * @return Liste des IDs trouvés
	 * @throws DatabaseException
	 */
	public ArrayList<Integer> recupererIds(String request, String nomColonne) throws DatabaseException {
		
		ArrayList<Integer> lstIds = new ArrayList<Integer>();
		
		try {
			// Execution requete de récupération de la ligne cherchée
			ResultSet resultat = this.executeRequest(request);
			// Parcourt du resultat
			while(resultat.next()){
				 lstIds.add(resultat.getInt(nomColonne));
			}
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return lstIds;
	}

}