package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class UtilisateurGestion {
	
	private static final String KEY_TOKENS = "F.Lecuyer,R.NguyenVan,A.Pouchoulin,J.Terrade,M.Terrade,R.Traineau,OnCrypteToutAvecNosNomsYeah";
	
	// Configuration pour accéder à LDAP depuis l'extérieur
	private static final String ADRESSE_LDAP = "ldaps.nomade.ec-nantes.fr";
	private static final int PORT_LDAP = 636;
	private static final boolean USE_SSL_LDAP = true;
	
	protected BddGestion _bdd;
	
	/**
	 * Initialise un gestionnaire d'utilisateurs
	 * @param bdd Base de données à utiliser
	 */
	public UtilisateurGestion(BddGestion bdd) {
		_bdd = bdd;
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
	 * - Définir t = s + id d’utilisateur. ID LDAP pour un token de connexion, ID local pour un token iCal.
	 * - Calculer le hmac_sha256 de t, au format base64. On le note h. La clé du hmac_sha256 est un mot de passe stocké sur le serveur.
	 * - Renvoyer t + h
	 * 
	 * Le token généré permet d'identifier l'utilisateur (contient son ID Ldap en clair), change à chaque connexion (partie aléatoire),
	 * n'est pas falsifiable (besoin de la clé du serveur pour générer le hmac de vérification), et est vérifiable par le serveur
	 * (il suffit de recalculer le hmac à partir de la 1ère partie de la chaîne et de le comparer à la 2ème partie).
	 * 
	 * En pratique on effectue la vérification en comparant avec le token stocké en base.
	 * 
	 * @param idUtilisateur ID de l'utilisateur pour lequel générer un token, peut être son ID LDAP (token de connexion) ou local (token iCal)
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
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	public int verifierConnexion(String token) throws IdentificationException, DatabaseException {
		
		if(!StringUtils.isAlphanumeric(token))
			throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Format de token invalide");
		
		ResultSet res = _bdd.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='" + token + "' AND utilisateur_token_expire > now()");
		
		try {
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
	 * @throws DatabaseException Erreur de connexion à la base de données
	 */
	public int verifierTokenIcal(String token) throws IdentificationException, DatabaseException {
		if(!StringUtils.isAlphanumeric(token))
			throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Format de token invalide");
		
		ResultSet res = _bdd.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_url_ical='" + token + "'");
		
		try {
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
	
	public String creerTokenIcal(int idUtilisateur) throws EdtempsException {
		try {
			String tokenIcal = genererToken(idUtilisateur);
			
			// Le token généré est alphanumérique (pas de problèmes de SQL)
			_bdd.executeRequest("UPDATE edt.utilisateur SET utilisateur_url_ical='" + tokenIcal + "' WHERE utilisateur_id=" + idUtilisateur);
			
			return tokenIcal;
			
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.out.println("Erreur de génération d'un token ICal : problème de cryptographie.");
			throw new EdtempsException(ResultCode.CRYPTOGRAPHIC_ERROR, "Erreur de génération d'un token ICal : problème de cryptographie.");
		}
	}
	
	/**
	 * Récupération de l'ID d'un utilisateur connu dans la base de données depuis son ID LDAP.
	 * L'utilisateur doit déjà avoir été enregistré sur le système emploi du temps
	 * @param ldapId ID LDAP de l'utilisateur
	 * @return ID de l'utilisateur, ou null si il n'est pas présent dans la base
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	private Integer getUserIdFromLdapId(long ldapId) throws DatabaseException {
		ResultSet results = _bdd.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_id_ldap=" + ldapId);
		
		Integer id = null;
		
		try {
			if(results.next()) {
				id = results.getInt(1);
			}
			results.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return id;
	}
	
	/**
	 * Déconnexion d'un utilisateur
	 * @param idUtilisateur ID de l'utilisateur à déconnecter
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	public void seDeconnecter(int idUtilisateur) throws DatabaseException {
		// Invalidation du token
		_bdd.executeRequest("UPDATE edt.utilisateur SET utilisateur_token=NULL,utilisateur_token_expire=NULL WHERE utilisateur_id=" + idUtilisateur);
	}
	
	/**
	 * Connexion de l'utilisateur et création d'un token de connexion (inséré en base de données)
	 * @param utilisateur Nom d'utilisateur
	 * @param pass Mot de passe
	 * @return Token de connexion créé
	 * @throws IdentificationException Identifiants invalides ou erreur de connexion à LDAP
	 * @throws DatabaseException Erreur relative à la base de données
	 */
	public String seConnecter(String utilisateur, String pass) throws IdentificationException, DatabaseException {
		
		// Connexion à LDAP
		String dn = "uid=" + utilisateur + ",ou=people,dc=ec-nantes,dc=fr";
		try {
			
			// SocketFactory selon l'utilisation de SSL
			SocketFactory socketFactoryConnection = USE_SSL_LDAP ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
			
			LDAPConnection connection = new LDAPConnection(socketFactoryConnection, ADRESSE_LDAP, PORT_LDAP, dn, pass);
			
			// Succès de la connexion : récupération de l'identifiant entier uid (uidnumber) de l'utilisateur
			String filtre = "(uid=" + utilisateur + ")";
			SearchRequest request = new SearchRequest("ou=people, dc=ec-nantes, dc=fr", SearchScope.SUB, filtre, "uidNumber", "sn", "givenName", "mail");
			
			SearchResult searchResult = connection.search(request);
			List<SearchResultEntry> lstResults = searchResult.getSearchEntries();
			
			// Entrée correspondant à l'utilisateur dans la recherche
			if(lstResults.isEmpty()) {
				System.out.println("Erreur de récupération de l'ID LDAP de l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Impossible de récupérer l'ID LDAP de l'utilisateur.");
			}
			
			// uiNumber LDAP de l'utilisateur récupéré
			Long uidNumber = lstResults.get(0).getAttributeValueAsLong("uidNumber");
			String nom = lstResults.get(0).getAttributeValue("sn");
			String prenom = lstResults.get(0).getAttributeValue("givenName");
			String mail = lstResults.get(0).getAttributeValue("mail");
			
			if(uidNumber == null) {
				System.out.println("Format d'uidNumer invalide pour l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Format d'uidNumber invalide sur le serveur LDAP");
			}
			
			// Insertion du token en base
			String token = genererToken(uidNumber);
			
			_bdd.startTransaction();
			
			Integer userId = getUserIdFromLdapId(uidNumber);
			
			Connection conn = _bdd.getConnection();
			if(userId != null) { // Utilisateur déjà présent en base
				// Token valable 1h, heure du serveur de base de donnée. Le token est constitué de caractères alphanumériques et de "_" : pas d'échappement nécessaire
				PreparedStatement statement = conn.prepareStatement("UPDATE edt.utilisateur SET utilisateur_token=?, utilisateur_nom=?, utilisateur_prenom=?, " +
						"utilisateur_email=?, utilisateur_token_expire=now() + interval '1 hour' WHERE utilisateur_id=?");
				
				statement.setString(1, token);
				statement.setString(2, nom);
				statement.setString(3, prenom);
				statement.setString(4,  mail);
				statement.setInt(5, userId);				
				
				statement.execute();
			}
			else { // Utilisateur absent de la base : insertion
				PreparedStatement statement = conn.prepareStatement("INSERT INTO edt.utilisateur(utilisateur_id_ldap, utilisateur_token, utilisateur_nom, utilisateur_prenom, " +
						"utilisateur_email, utilisateur_token_expire) VALUES(?, ?, ?, ?, ?, now() + interval '1 hour')");
				statement.setLong(1, uidNumber);
				statement.setString(2, token);
				statement.setString(3, nom);
				statement.setString(4, prenom);
				statement.setString(5,  mail);
				
				statement.execute();
			}
			
			_bdd.commit();
			
			return token;
			
		} catch (com.unboundid.ldap.sdk.LDAPException e) {
			
			if(e.getResultCode() == com.unboundid.ldap.sdk.ResultCode.INVALID_CREDENTIALS) {
				throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Identifiants LDAP invalides.");
			}
			else {
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Erreur de connexion à LDAP : " + e.getResultCode().getName());
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			System.out.println("Erreur de génération de token : machine Java hôte incompatible");
			e.printStackTrace();
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
	
	public ArrayList<UtilisateurIdentifie> getIntervenantsEvenement(int evenementId) throws DatabaseException {
		ResultSet reponse = _bdd.executeRequest("SELECT utilisateur.utilisateur_id, utilisateur.utilisateur_nom, " +
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
	
	public ArrayList<UtilisateurIdentifie> getResponsablesEvenement(int evenementId) throws DatabaseException {
		ResultSet reponse = _bdd.executeRequest("SELECT utilisateur.utilisateur_id, utilisateur.utilisateur_nom, " +
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
}
