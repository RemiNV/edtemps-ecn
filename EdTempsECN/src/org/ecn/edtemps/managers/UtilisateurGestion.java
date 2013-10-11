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
	 * @param password
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
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
	 * - Définir t = s + “_” + id d’utilisateur
	 * - Calculer le hmac_sha256 de t, au format base64. On le note h. La clé du hmac_sha256 est un mot de passe stocké sur le serveur.
	 * - Renvoyer t + “_” + h
	 * 
	 * Le token généré permet d'identifier l'utilisateur (contient son ID en clair), change à chaque connexion (partie aléatoire),
	 * n'est pas falsifiable (besoin de la clé du serveur pour générer le hmac de vérification), et est vérifiable par le serveur
	 * (il suffit de recalculer le hmac à partir de la 1ère partie de la chaîne et de le comparer à la 2ème partie).
	 * @param idUtilisateur ID de l'utilisateur pour lequel générer un token
	 * @return token généré (non inséré en BDD)
	 * @throws InvalidKeyException Clé serveur invalide (ne devrait jamais se produire)
	 * @throws NoSuchAlgorithmException La machine Java hôte est incapable de produire un HMAC_SHA256 (ne devrait jamais se produire)
	 */
	public static String genererToken(long idUtilisateur) throws InvalidKeyException, NoSuchAlgorithmException {
		// Génération de 10 caractères aléatoires
		String randomSeed = RandomStringUtils.randomAlphanumeric(10);
		String tokenHeader = randomSeed + "_" + idUtilisateur;
		
		String res = hmac_sha256(KEY_TOKENS, tokenHeader);
		
		return tokenHeader + "_" + res;
	}
	
	public static String seConnecter(String utilisateur, String pass) throws IdentificationException, DatabaseException {
		
		// Connexion à LDAP
		String dn = "uid=" + utilisateur + ",ou=people,dc=ec-nantes,dc=fr";
		try {
			
			// SocketFactory selon l'utilisation de SSL
			SocketFactory socketFactoryConnection;
			if(USE_SSL_LDAP) {
				socketFactoryConnection = SSLSocketFactory.getDefault();
			}
			else {
				socketFactoryConnection = SocketFactory.getDefault();
			}
			
			LDAPConnection connection = new LDAPConnection(socketFactoryConnection, ADRESSE_LDAP, PORT_LDAP, dn, pass);
			
			// Succès de la connexion : récupération de l'identifiant entier uid (uidnumber) de l'utilisateur
			String filtre = "(uid=" + utilisateur + ")";
			SearchRequest request = new SearchRequest("ou=people, dc=ec-nantes, dc=fr", SearchScope.SUB, filtre, "uidNumber");
			
			SearchResult searchResult = connection.search(request);
			List<SearchResultEntry> lstResults = searchResult.getSearchEntries();
			
			// Récupération de l'entrée de l'utilisateur
			if(lstResults.isEmpty()) {
				System.out.println("Erreur de récupération de l'ID LDAP de l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Impossible de récupérer l'ID LDAP de l'utilisateur.");
			}
			
			Long uidNumber = lstResults.get(0).getAttributeValueAsLong("uidNumber");
			
			if(uidNumber == null) {
				System.out.println("Format d'uidNumer invalide pour l'utilisateur : " + utilisateur);
				throw new IdentificationException(ResultCode.LDAP_CONNECTION_ERROR, "Format d'uidNumber invalide sur le serveur LDAP");
			}
			
			// Insertion du token en base
			String token = genererToken(uidNumber);
			
			ResultSet results = BddGestion.executeRequest("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_id_ldap=" + uidNumber);
			
			if(results.next()) { // Utilisateur déjà présent en base
				// Token valable 1h, heure du serveur de base de donnée. Le token est constitué de caractères alphanumériques et de "_" : pas d'échappement nécessaire
				BddGestion.executeRequest("UPDATE edt.utilisateur SET utilisateur_token='" + token + "', utilisateur_token_expire=now() + interval '1 hour'");
			}
			else { // Utilisateur absent de la base : insertion
				BddGestion.executeRequest("INSERT INTO edt.utilisateur(utilisateur_id_ldap, utilisateur_token, utilisateur_token_expire) VALUES(" +
						uidNumber + ",'" + token + "',now() + interval '1 hour')");
			}
			
			results.close();
			
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
}
