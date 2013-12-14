package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationErrorException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

/**
 * Classe de gestion des utilisateurs
 * 
 * @author Remi
 */
public class UtilisateurGestion {
	
	/** Gestionnaire de base de données */
	protected BddGestion bdd;

	/** Clé permettant de générer les jetons de connexion (token) */
	private static final String KEY_TOKENS = "F.Lecuyer,R.NguyenVan,A.Pouchoulin,J.Terrade,M.Terrade,R.Traineau,OnCrypteToutAvecNosNomsYeah";
	
	/** Configuration pour accéder à LDAP depuis l'extérieur */
	private static final String ADRESSE_LDAP = "ldaps.nomade.ec-nantes.fr";
	private static final int PORT_LDAP = 636;
	private static final boolean USE_SSL_LDAP = true;
	
	private static Logger logger = LogManager.getLogger(UtilisateurGestion.class.getName());
	
	/**
	 * Objet spécifique de retour de la méthode seConnecter().
	 * Il contient le token de connexion et l'identifiant de l'utilisateur
	 * Ces deux informations sont ensuite retournées par le servlet au client
	 *  
	 * @author Joffrey
	 */
	public class ObjetRetourMethodeConnexion {
		
		private Integer userId;
		private String token;
		private List<Integer> actionsAutorisees;
		
		public ObjetRetourMethodeConnexion(Integer userId, String token, List<Integer> actionsAutorisees) {
			this.userId = userId;
			this.token = token;
			if (CollectionUtils.isNotEmpty(actionsAutorisees)) {
				this.actionsAutorisees = actionsAutorisees;
			} else {
				this.actionsAutorisees = new ArrayList<Integer>();
			}
		}

		public Integer getUserId() {
			return userId;
		}
		public String getToken() {
			return token;
		}
		public List<Integer> getActionsAutorisees() {
			return actionsAutorisees;
		}
	}
	
	
	/**
	 * Définit les actions qui peuvent être faites, pour vérification des autorisations.
	 * L'ID du droit doit correspondre avec l'ID en base de donnée.
	 * @author Remi
	 */
	public static enum ActionsEdtemps {
		
		// Actions possibles à compléter
		CREER_GROUPE_COURS(3),
		LIMITE_CALENDRIERS_ETENDUE(5);
		
		private int id;
		
		ActionsEdtemps(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
	}
	
	/**
	 * Initialise un gestionnaire d'utilisateurs
	 * @param bdd Base de données à utiliser
	 */
	public UtilisateurGestion(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	
	/**
	 * Calcul du HMAC_SHA256 d'une chaîne avec un mot de passe donné.
	 * Voir : http://fr.wikipedia.org/wiki/Keyed-Hash_Message_Authentication_Code (accédé 11/10/2013)
	 * 
	 * @param password Clé à utiliser pour le calcul du HMAC
	 * @param input Chaîne dont le hash est à calculer
	 * @return hmac_sha256 calculé
	 * @throws InvalidKeyException Clé fournie de format invalide
	 * @throws NoSuchAlgorithmException La machine Java hôte est incapable de produire un HMAC_SHA256 (ne devrait jamais se produire)
	 */
	public static String hmac_sha256(String password, String input) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec keySpec = new SecretKeySpec(password.getBytes(), "HmacSHA256");

		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(keySpec);
		byte[] result = mac.doFinal(input.getBytes());
		
		// Conversion en une chaîne base64
		String strResult = "";
        for (final byte element : result)
        {
        	// Conversion du byte en byte non signé, affichage en base 16 
        	strResult += Integer.toString((element & 0xff) + 0x100, 16).substring(1);
        }

		return strResult;
	}
	
	/**
	 * Génération d'un token de connexion pour l'utilisateur. Les tokens générés sont aléatoires.
	 * Algorithme : 
	 * - Générer une chaîne de 10 caractères alphanumériques aléatoires (exemple 1b483A5e35) qu’on appelle s
	 * - Définir t = s + id LDAP d’utilisateur.
	 * - Calculer le hmac_sha256 de t, au format base64. On le note h. La clé du hmac_sha256 est un mot de passe stocké sur le serveur.
	 * - Renvoyer t + h
	 * 
	 * Le token généré permet d'identifier l'utilisateur (contient son ID Ldap en clair), change à chaque connexion (partie aléatoire),
	 * n'est pas falsifiable (besoin de la clé du serveur pour générer le hmac de vérification), et est vérifiable par le serveur
	 * (il suffit de recalculer le hmac à partir de la 1ère partie de la chaîne et de le comparer à la 2ème partie).
	 * 
	 * En pratique on effectue la vérification en comparant avec le token stocké en base.
	 * 
	 * @param idUtilisateur ID LDAP de l'utilisateur pour lequel générer un token
	 * @return token généré (non inséré en BDD), qui est une chaîne alphanumérique
	 * @throws InvalidKeyException Clé serveur invalide (ne devrait jamais se produire)
	 * @throws NoSuchAlgorithmException La machine Java hôte est incapable de produire un HMAC_SHA256 (ne devrait jamais se produire)
	 */
	public static String genererToken(long idUtilisateur) throws InvalidKeyException, NoSuchAlgorithmException {
		// Génération de 10 caractères aléatoires
		String randomSeed = RandomStringUtils.randomAlphanumeric(10);
		String tokenHeader = randomSeed + idUtilisateur;
		
		String res = hmac_sha256(KEY_TOKENS, tokenHeader);
		
		return tokenHeader + res;
	}
	
	/**
	 * Vérifie la validité d'un token de connexion fourni par un utilisateur
	 * @param token Token à vérifier
	 * @return ID de l'utilisateur si le token est valide
	 * @throws IdentificationException Token de l'utilisateur invalide ou expiré
	 * @throws DatabaseException
	 */
	public int verifierConnexion(String token) throws IdentificationException, DatabaseException {
		
		if(!StringUtils.isAlphanumeric(token))
			throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Format de token invalide");

		try {
		
			PreparedStatement req = bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token=? AND utilisateur_token_expire > now()");
			req.setString(1, token);
			ResultSet res = req.executeQuery();
		
			if(res.next()) {
				return res.getInt(1);
			}
			else {
				throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Token invalide ou expiré");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Vérifie la validité d'un token iCal fourni par un utilisateur
	 * @param token Token à vérifier
	 * @return ID de l'utilisateur déduit du token
	 * @throws IdentificationException Le token est invalide
	 * @throws DatabaseException
	 */
	public int verifierTokenIcal(String token) throws IdentificationException, DatabaseException {
		if(!StringUtils.isAlphanumeric(token))
			throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Format de token invalide");
		
		try {
			
			PreparedStatement req = bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_url_ical=?");
			req.setString(1, token);
			ResultSet res = req.executeQuery();

			if(res.next()) {
				return res.getInt(1);
			}
			else {
				throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Token invalide.");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	/**
	 * Récupère l'url ICal d'un utilisateur
	 * @param idUtilisateur Identifiant de l'utilisateur
	 * @return l'url du fichier ical
	 * @throws DatabaseException
	 */
	public String getTokenICal(int idUtilisateur) throws DatabaseException {
		ResultSet res = bdd.executeRequest("SELECT utilisateur_url_ical FROM edt.utilisateur WHERE utilisateur_id=" + idUtilisateur);
		
		try {
			if(res.next()) {
				return res.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Récupération de l'ID d'un utilisateur connu dans la base de données depuis son ID LDAP.
	 * L'utilisateur doit déjà avoir été enregistré sur le système emploi du temps
	 * @param dn dn LDAP de l'utilisateur
	 * @return ID de l'utilisateur, ou null si il n'est pas présent dans la base
	 * @throws DatabaseException
	 */
	private Integer getUserIdFromDN(String dn) throws DatabaseException {
		
		try {
			PreparedStatement statement = bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_dn=?");
			
			statement.setString(1, dn);
			
			ResultSet results = statement.executeQuery();
			
			Integer id = null;

			if(results.next()) {
				id = results.getInt(1);
			}
			results.close();

			return id;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Déconnexion d'un utilisateur
	 * @param idUtilisateur ID de l'utilisateur à déconnecter
	 * @throws DatabaseException
	 */
	public void seDeconnecter(int idUtilisateur) throws DatabaseException {
		// Invalidation du token
		bdd.executeUpdate("UPDATE edt.utilisateur SET utilisateur_token=NULL,utilisateur_token_expire=NULL WHERE utilisateur_id=" + idUtilisateur);
	}
	
	/**
	 * Connexion de l'utilisateur et création d'un token de connexion (inséré en base de données)
	 * @param utilisateur Nom d'utilisateur
	 * @param pass Mot de passe
	 * @return ObjetRetourMethodeConnexion qui contient le yoken de connexion créé et l'identifiant de l'utilisateur
	 * @throws IdentificationException Identifiants invalides ou erreur de connexion à LDAP
	 * @throws DatabaseException
	 */
	public ObjetRetourMethodeConnexion seConnecter(String utilisateur, String pass) throws IdentificationException, DatabaseException {
		
		// Connexion à LDAP
		String dn = "uid=" + utilisateur + ",ou=people,dc=ec-nantes,dc=fr";
		try {
			
			// SocketFactory selon l'utilisation de SSL
			SocketFactory socketFactoryConnection = USE_SSL_LDAP ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
			
			LDAPConnection connection = new LDAPConnection(socketFactoryConnection, ADRESSE_LDAP, PORT_LDAP, dn, pass);
			
			// Succès de la connexion : récupération des nom, prénom, mail de l'utilisateur
			String filtre = "(uid=" + utilisateur + ")";
			SearchRequest request = new SearchRequest("ou=people, dc=ec-nantes, dc=fr", SearchScope.SUB, filtre, "uidNumber", "sn", "givenName", "mail");
			
			SearchResult searchResult = connection.search(request);
			List<SearchResultEntry> lstResults = searchResult.getSearchEntries();
			
			connection.close();
			
			// Entrée correspondant à l'utilisateur dans la recherche
			if(lstResults.isEmpty()) {
				logger.error("Erreur de récupération des informations LDAP de l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Impossible de récupérer les informations LDAP de l'utilisateur.");
			}
			
			Long uidNumber = lstResults.get(0).getAttributeValueAsLong("uidNumber");
			String nom = lstResults.get(0).getAttributeValue("sn");
			String prenom = lstResults.get(0).getAttributeValue("givenName");
			String mail = lstResults.get(0).getAttributeValue("mail");
			
			if(uidNumber == null) {
				logger.error("Format d'uidNumer invalide pour l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Format d'uidNumber invalide sur le serveur LDAP");
			}
			
			// Insertion du token en base
			String token = genererToken(uidNumber);
			
			bdd.startTransaction();
			
			Integer userId = getUserIdFromDN(dn);
			
			Connection conn = bdd.getConnection();
			if(userId != null) { // Utilisateur déjà présent en base
				// Token valable 1h, heure du serveur de base de donnée. Le token est constitué de caractères alphanumériques et de "_" : pas d'échappement nécessaire
				PreparedStatement statement = conn.prepareStatement("UPDATE edt.utilisateur SET utilisateur_token=?, utilisateur_nom=?, utilisateur_prenom=?, " +
						"utilisateur_email=?, utilisateur_active=TRUE, utilisateur_token_expire=now() + interval '1 hour' WHERE utilisateur_id=?");
				
				statement.setString(1, token);
				statement.setString(2, nom);
				statement.setString(3, prenom);
				statement.setString(4,  mail);
				statement.setInt(5, userId);				
				
				statement.execute();
			}
			else { // Utilisateur absent de la base : insertion
				
				// Création d'un token ICal pour l'utilisateur
				String tokenIcal = genererToken(uidNumber);
				
				PreparedStatement statement = conn.prepareStatement("INSERT INTO edt.utilisateur(utilisateur_dn, utilisateur_token, utilisateur_nom, utilisateur_prenom, " +
						"utilisateur_email, utilisateur_token_expire, utilisateur_url_ical) VALUES(?, ?, ?, ?, ?, now() + interval '1 hour', ?) RETURNING utilisateur_id");
				statement.setString(1, dn);
				statement.setString(2, token);
				statement.setString(3, nom);
				statement.setString(4, prenom);
				statement.setString(5,  mail);
				statement.setString(6, tokenIcal);
				
				ResultSet reponse = statement.executeQuery();
				
				// Récupération de l'identifiant de l'utilisateur ajouté
				reponse.next();
				userId = reponse.getInt(1);
			}
			
			bdd.commit();
			
			return new ObjetRetourMethodeConnexion(userId, token, getListeActionsAutorisees(userId));
			
		} catch (com.unboundid.ldap.sdk.LDAPException e) {
			
			if(e.getResultCode() == com.unboundid.ldap.sdk.ResultCode.INVALID_CREDENTIALS) {
				throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Identifiants LDAP invalides.");
			}
			else {
				throw new IdentificationErrorException(ResultCode.LDAP_CONNECTION_ERROR, "Erreur de connexion à LDAP : " + e.getResultCode().getName(), e);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			logger.fatal("Erreur de génération de token : machine Java hôte incompatible", e);
			throw new IdentificationException(ResultCode.CRYPTOGRAPHIC_ERROR, "Erreur de génération de token : machine Java hôte incompatible");
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Création d'un utilisateur à partir d'une ligne de base de données.
	 * Colonnes nécessaires dans le ResultSet : 
	 * utilisateur_id, utilisateur_nom, utilisateur_prenom, utilisateur_email
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return Utilisateur créé
	 * @throws SQLException
	 */
	private UtilisateurIdentifie inflateUtilisateurFromRow(ResultSet row) throws SQLException {
		int id = row.getInt("utilisateur_id");
		String nom = row.getString("utilisateur_nom");
		String prenom = row.getString("utilisateur_prenom");
		String email = row.getString("utilisateur_email");
		
		return new UtilisateurIdentifie(id, nom, prenom, email);
	}
	
	
	/**
	 * Recherche les utilisateurs dont les noms comportent une chaine
	 * @param debutNomPrenomMail Chaine de caractères à rechercher
	 * @return une liste d'utilisateur correspondant
	 * @throws DatabaseException
	 */
	public ArrayList<UtilisateurIdentifie> rechercherUtilisateur(String debutNomPrenomMail) throws DatabaseException{
		try {
			ArrayList<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();
			
			PreparedStatement statement = bdd.getConnection().prepareStatement(
					"SELECT utilisateur_nom, utilisateur.prenom, utilisateur_email, utilisateur_id "
					+ "FROM edt.utilisateur "
					+ "WHERE utilisateur_nom LIKE ?% "
					+ "OR utilisateur_prenom LIKE ?% "
					+ "OR utilisateur_email LIKE ?% ");
			statement.setString(1, debutNomPrenomMail);
			statement.setString(2, debutNomPrenomMail);
			statement.setString(3, debutNomPrenomMail);
			
			ResultSet reponse = statement.executeQuery();

			while(reponse.next()) {
				res.add(inflateUtilisateurFromRow(reponse));
			}
			reponse.close();
			return res;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Savoir si l'utilisateur a le droit ou non de réaliser une action
	 * @param actionNom libellé de l'action sur laquelle on recherche les droits de l'utilisateur
	 * @param idUtilisateur identifiant de l'utilisateur
	 * @return true si l'action est autorisée pour l'utilisaetur, false si l'action est interdite à l'utilisateur
	 * @throws DatabaseException
	 */
	public boolean aDroit(ActionsEdtemps action, int idUtilisateur) throws DatabaseException{
		boolean isAble = false;
		try {
			ResultSet reponse = bdd.executeRequest(
					"SELECT droits.droits_libelle "
					+ "FROM edt.droits "
					+ "INNER JOIN edt.aledroitde ON droits.droits_id = aledroitde.droits_id "
					+ "INNER JOIN edt.typeutilisateur ON typeutilisateur.type_id = aledroitde.type_id "
					+ "INNER JOIN edt.estdetype ON estdetype.type_id = typeutilisateur.type_id "
					+ "WHERE estdetype.utilisateur_id = " + idUtilisateur + " "
					+ "AND droits.droits_id = " + action.getId());
			if(reponse.next()) {
				isAble = true;
			}
			reponse.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		return isAble;
	}
	
	/**
	 * Récupération d'un utilisateur en base
	 * @param idUtilisateur identifiant de l'utilisateur
	 * @return Utilisateur identifié correspondant à l'identifiant donné
	 * @throws DatabaseException
	 */
	public UtilisateurIdentifie getUtilisateur(int idUtilisateur) throws DatabaseException{
		try {
			ResultSet reponse = bdd.executeRequest(
					"SELECT utilisateur_nom, utilisateur_prenom, utilisateur_email, utilisateur_id "
					+ "FROM edt.utilisateur "
					+ "WHERE utilisateur_id = " + idUtilisateur);
			UtilisateurIdentifie res = null;
			if(reponse.next()) {
				res = inflateUtilisateurFromRow(reponse);
			}
			reponse.close();
			return res;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	
	/**
	 * Récupère la liste des intervenants d'un événement
	 * @param evenementId Identifiant de l'événement à traiter
	 * @return la liste des intervenants de l'événement
	 * @throws DatabaseException
	 */
	public ArrayList<UtilisateurIdentifie> getIntervenantsEvenement(int evenementId) throws DatabaseException {
		ResultSet reponse = bdd.executeRequest("SELECT utilisateur.utilisateur_id, utilisateur.utilisateur_nom, " +
				"utilisateur.utilisateur_prenom, utilisateur.utilisateur_email FROM edt.utilisateur INNER JOIN edt.intervenantevenement " +
				"ON intervenantevenement.utilisateur_id = utilisateur.utilisateur_id AND intervenantevenement.eve_id = " + evenementId);
		
		try {
			ArrayList<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();
			while(reponse.next()) {
				res.add(inflateUtilisateurFromRow(reponse));
			}
			
			reponse.close();
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Récupère la liste des responsables d'un événement
	 * @param evenementId Identifiant de l'événement à traiter
	 * @return la liste des responsables de l'évenement
	 * @throws DatabaseException
	 */
	public ArrayList<UtilisateurIdentifie> getResponsablesEvenement(int evenementId) throws DatabaseException {
		ResultSet reponse = bdd.executeRequest("SELECT utilisateur.utilisateur_id, utilisateur.utilisateur_nom, " +
				"utilisateur.utilisateur_prenom, utilisateur.utilisateur_email FROM edt.utilisateur INNER JOIN edt.responsableevenement " +
				"ON responsableevenement.utilisateur_id = utilisateur.utilisateur_id AND responsableevenement.eve_id = " + evenementId);
		
		try {
			ArrayList<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();
			while(reponse.next()) {
				res.add(inflateUtilisateurFromRow(reponse));
			}
			
			reponse.close();
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	

	/**
	 * Récupère la liste des responsables d'un groupe
	 * @param groupeId Identifiant du groupe à traiter
	 * @return la liste des responsables du groupe
	 * @throws DatabaseException
	 */
	public List<UtilisateurIdentifie> getResponsablesGroupe(int groupeId) throws DatabaseException {
		ResultSet reponse = bdd.executeRequest("SELECT utilisateur.utilisateur_id, utilisateur.utilisateur_nom, utilisateur.utilisateur_prenom, utilisateur.utilisateur_email" +
				" FROM edt.utilisateur" +
				" INNER JOIN edt.proprietairegroupeparticipant ON proprietairegroupeparticipant.utilisateur_id=utilisateur.utilisateur_id" +
				" AND proprietairegroupeparticipant.groupeparticipant_id="+groupeId);
		
		try {
			List<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();
			while(reponse.next()) {
				res.add(inflateUtilisateurFromRow(reponse));
			}
			
			reponse.close();
			
			return res;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	
	
	/**
	 * Récupère la liste de tous les utilisateurs qui peuvent potentiellement être propriétaires d'un groupe de participants
	 * @return liste des utilisateurs
	 * @throws DatabaseException
	 */
	public List<UtilisateurIdentifie> getResponsablesPotentiels() throws DatabaseException {
		ResultSet reponse = bdd.executeRequest("SELECT * FROM edt.utilisateur WHERE utilisateur_active");

		List<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();

		try {
			while(reponse.next()) {
				res.add(inflateUtilisateurFromRow(reponse));
			}
			reponse.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

		return res;
	}
	
	/**
	 * Récupère la liste actions autorisées pour un utilisateur
	 * @param idUtilisateur identifiant de l'utilisateur
	 * @return la liste des identifiants des actions que l'utilisateur peut réaliser
	 * @throws DatabaseException
	 */
	public List<Integer> getListeActionsAutorisees(int idUtilisateur) throws DatabaseException {

		List<Integer> resultat = new ArrayList<Integer>();
		
		try {
			ResultSet reponse = bdd.executeRequest(
					"SELECT droits.droits_id "
					+ "FROM edt.droits "
					+ "INNER JOIN edt.aledroitde ON droits.droits_id = aledroitde.droits_id "
					+ "INNER JOIN edt.typeutilisateur ON typeutilisateur.type_id = aledroitde.type_id "
					+ "INNER JOIN edt.estdetype ON estdetype.type_id = typeutilisateur.type_id "
					+ "WHERE utilisateur_id = " + idUtilisateur);
			while(reponse.next()) {
				resultat.add(reponse.getInt("droits_id"));
			}
			
			reponse.close();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

		return resultat;
		
	}
	
	
	/**
	 * Récupère la liste des types d'utilisateurs possibles
	 * @return liste des types d'utilisateurs
	 * @throws DatabaseException 
	 */
	public Map<Integer, String> getListeTypesUtilisateur() throws DatabaseException {

		Map<Integer, String> listeTypes = new HashMap<Integer, String>();

		try {
			ResultSet reponse = bdd.executeRequest("SELECT type_id, type_libelle FROM edt.typeutilisateur");
			while (reponse.next()) {
				listeTypes.put(reponse.getInt("type_id"), reponse.getString("type_libelle"));
			}
			reponse.close();
			
			return listeTypes;
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	/**
	 * Récupère la liste de tous les utilisateurs
	 * @return liste des utilisateurs
	 * @throws DatabaseException
	 */
	public List<UtilisateurIdentifie> getListeUtilisateurs() throws DatabaseException {
		ResultSet reponse = bdd.executeRequest("SELECT utilisateur_id, utilisateur_nom, utilisateur_prenom, utilisateur_email, utilisateur_active, utilisateur_token_expire" +
				" FROM edt.utilisateur ORDER BY utilisateur.utilisateur_prenom");

		List<UtilisateurIdentifie> res = new ArrayList<UtilisateurIdentifie>();

		try {
			while(reponse.next()) {
				int id = reponse.getInt("utilisateur_id");
				String nom = reponse.getString("utilisateur_nom");
				String prenom = reponse.getString("utilisateur_prenom");
				String email = reponse.getString("utilisateur_email");
				boolean active = reponse.getBoolean("utilisateur_active");
				Date tokenExpiration = reponse.getDate("utilisateur_token_expire");
				
				UtilisateurIdentifie utilisateur = new UtilisateurIdentifie(id, nom, prenom, email);
				utilisateur.setActive(active);
				utilisateur.setTokenExpiration(tokenExpiration);

				// Récupération des types de l'utilisateur
				utilisateur.setType(this.getListeTypes(id));
				
				res.add(utilisateur);
			}
			reponse.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

		return res;
	}
	
	
	/**
	 * Modifie le type d'un utilisateur
	 * @param userId Identifiant de l'utilisateur
	 * @param listeIdentifiants Liste des identifiants des types de l'utilisateur
	 * @throws DatabaseException 
	 */
	public void modifierTypeUtilisateur(int userId, List<Integer> listeIdentifiantsTypes) throws DatabaseException {

		// Démarre une transaction
		bdd.startTransaction();
		
		// Supprime les types actuels de l'utilisateur
		bdd.executeUpdate("DELETE FROM edt.estdetype WHERE utilisateur_id="+userId);
		
		// Ajoute les nouveaux types
		if (CollectionUtils.isNotEmpty(listeIdentifiantsTypes)) {
			StringBuilder requete = new StringBuilder("INSERT INTO edt.estdetype (utilisateur_id, type_id) VALUES ");
			for (Integer idType : listeIdentifiantsTypes) {
				requete.append("("+userId+", "+idType+"), ");
			}
			
			// Exécute la requête (en supprimant les deux derniers caractères : ', '
			bdd.executeUpdate(requete.toString().substring(0, requete.length()-2));
		}
		
		// Commit la transaction
		bdd.commit();
		
	}
	

	/**
	 * Activer/Désactiver un utilisateur : il reste dans la base de données mais ne sera pas remonté lors du remplissage des liste d'utilisateurs
	 * @param userId Identifiant de l'utilisateur
	 * @param statut VRAI si l'utilisateur est actif, FAUX sinon
	 * @throws DatabaseException 
	 */
	public void desactiverUtilisateur(int userId, boolean statut) throws DatabaseException {

		// Modifie le champ "utilisateur_active" de l'utilisateur
		bdd.executeUpdate("UPDATE edt.utilisateur SET utilisateur_active='"+statut+"' WHERE utilisateur_id="+userId);
		
	}
	

	/**
	 * Récupère la liste des types d'utilisateurs auxquels est rattaché un utilisateur
	 * @param idUtilisateur identifiant de l'utilisateur
	 * @return la liste des identifiants des types auxquels l'utilisateur est rattaché
	 * @throws DatabaseException
	 */
	public List<Integer> getListeTypes(int idUtilisateur) throws DatabaseException {

		List<Integer> resultat = new ArrayList<Integer>();
		
		try {
			ResultSet reponse = bdd.executeRequest(
					"SELECT typeutilisateur.type_id "
					+ "FROM edt.typeutilisateur "
					+ "INNER JOIN edt.estdetype ON estdetype.type_id = typeutilisateur.type_id "
					+ "WHERE utilisateur_id = " + idUtilisateur);
			while(reponse.next()) {
				resultat.add(reponse.getInt("type_id"));
			}
			
			reponse.close();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

		return resultat;
		
	}
}
