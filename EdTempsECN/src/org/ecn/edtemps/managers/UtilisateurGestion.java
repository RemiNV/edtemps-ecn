package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;

public class UtilisateurGestion {
	
	// TODO : déplacer en base de données (et garder bien secret !)
	private static final String KEY_TOKENS = "IAmASecretKey";
	
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
	 * - Générer une chaîne de 10 caractères aléatoires (exemple 1è483A5e35) qu’on appelle s
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
	public String genererToken(int idUtilisateur) throws InvalidKeyException, NoSuchAlgorithmException {
		// Génération de 10 caractères aléatoires
		String randomSeed = RandomStringUtils.randomAscii(10);
		String tokenHeader = randomSeed + "_" + idUtilisateur;
		
		String res = hmac_sha256(KEY_TOKENS, tokenHeader);
		
		return tokenHeader + "_" + res;
	}
}
