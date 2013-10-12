package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPException;

public class UtilisateurGestion {
	
	// TODO : déplacer en base de données (et garder bien secret !)
	private static final String KEY_TOKENS = "IAmASecretKey";
	
	// Configuration pour accéder à LDAP depuis l'extérieur
	private static final String ADRESSE_LDAP = "ldaps.nomade.ec-nantes.fr";
	private static final int PORT_LDAP = 636;
	private static final boolean USE_SSL_LDAP = true;
	
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
	 * - Définir t = s + id LDAP d’utilisateur
	 * - Calculer le hmac_sha256 de t, au format base64. On le note h. La clé du hmac_sha256 est un mot de passe stocké sur le serveur.
	 * - Renvoyer t + h
	 * 
	 * Le token généré permet d'identifier l'utilisateur (contient son ID Ldap en clair), change à chaque connexion (partie aléatoire),
	 * n'est pas falsifiable (besoin de la clé du serveur pour générer le hmac de vérification), et est vérifiable par le serveur
	 * (il suffit de recalculer le hmac à partir de la 1ère partie de la chaîne et de le comparer à la 2ème partie).
	 * 
	 * En pratique on effectue la vérification en comparant avec le token stocké en base.
	 * 
	 * @param idUtilisateur ID de l'utilisateur pour lequel générer un token
	 * @return token généré (non inséré en BDD), qui est une chaîne alphanumérique
	 * @throws InvalidKeyException Clé serveur invalide (ne devrait jamais se produire)
	 * @throws NoSuchAlgorithmException La machine Java hôte est incapable de produire un HMAC_SHA256 (ne devrait jamais se produire)
	 */
	public static String genererToken(long idLdapUtilisateur) throws InvalidKeyException, NoSuchAlgorithmException {
		// Génération de 10 caractères aléatoires
		String randomSeed = RandomStringUtils.randomAlphanumeric(10);
		String tokenHeader = randomSeed + idLdapUtilisateur;
		
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
	public static int verifierConnexion(String token) throws IdentificationException, DatabaseException {
		
		if(!StringUtils.isAlphanumeric(token))
			throw new IdentificationException(ResultCode.IDENTIFICATION_ERROR, "Format de token invalide");
		
		ResultSet res = BddGestion.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='" + token + "' AND utilisateur_token_expire > now()");
		
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
	 * Récupération de l'ID d'un utilisateur connu dans la base de données depuis son ID LDAP.
	 * L'utilisateur doit déjà avoir été enregistré sur le système emploi du temps
	 * @param ldapId ID LDAP de l'utilisateur
	 * @return ID de l'utilisateur, ou null si il n'est pas présent dans la base
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	private static Integer getUserIdFromLdapId(long ldapId) throws DatabaseException {
		ResultSet results = BddGestion.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_id_ldap=" + ldapId);
		
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
	 * Connexion de l'utilisateur et création d'un token de connexion (inséré en base de données)
	 * @param utilisateur Nom d'utilisateur
	 * @param pass Mot de passe
	 * @return Token de connexion créé
	 * @throws IdentificationException Identifiants invalides ou erreur de connexion à LDAP
	 * @throws DatabaseException Erreur relative à la base de données
	 */
	public static String seConnecter(String utilisateur, String pass) throws IdentificationException, DatabaseException {
		
		// Connexion à LDAP
		String dn = "uid=" + utilisateur + ",ou=people,dc=ec-nantes,dc=fr";
		try {
			
			// SocketFactory selon l'utilisation de SSL
			SocketFactory socketFactoryConnection = USE_SSL_LDAP ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
			
			LDAPConnection connection = new LDAPConnection(socketFactoryConnection, ADRESSE_LDAP, PORT_LDAP, dn, pass);
			
			// Succès de la connexion : récupération de l'identifiant entier uid (uidnumber) de l'utilisateur
			String filtre = "(uid=" + utilisateur + ")";
			SearchRequest request = new SearchRequest("ou=people, dc=ec-nantes, dc=fr", SearchScope.SUB, filtre, "uidNumber");
			
			SearchResult searchResult = connection.search(request);
			List<SearchResultEntry> lstResults = searchResult.getSearchEntries();
			
			// Entrée correspondant à l'utilisateur dans la recherche
			if(lstResults.isEmpty()) {
				System.out.println("Erreur de récupération de l'ID LDAP de l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Impossible de récupérer l'ID LDAP de l'utilisateur.");
			}
			
			// uiNumber LDAP de l'utilisateur récupéré
			Long uidNumber = lstResults.get(0).getAttributeValueAsLong("uidNumber");
			
			if(uidNumber == null) {
				System.out.println("Format d'uidNumer invalide pour l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Format d'uidNumber invalide sur le serveur LDAP");
			}
			
			// Insertion du token en base
			String token = genererToken(uidNumber);
			
			Integer userId = getUserIdFromLdapId(uidNumber);
			
			if(userId != null) { // Utilisateur déjà présent en base
				// Token valable 1h, heure du serveur de base de donnée. Le token est constitué de caractères alphanumériques et de "_" : pas d'échappement nécessaire
				BddGestion.executeRequest("UPDATE edt.utilisateur SET utilisateur_token='" + token + "', utilisateur_token_expire=now() + interval '1 hour'");
			}
			else { // Utilisateur absent de la base : insertion
				BddGestion.executeRequest("INSERT INTO edt.utilisateur(utilisateur_id_ldap, utilisateur_token, utilisateur_token_expire) VALUES(" +
						uidNumber + ",'" + token + "',now() + interval '1 hour')");
			}
			
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
		}
	}
}
