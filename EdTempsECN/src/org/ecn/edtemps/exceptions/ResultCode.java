package org.ecn.edtemps.exceptions;

/**
 * Codes de retour des requêtes 
 * @author Remi
 *
 */
public enum ResultCode {

	/** Requête effectuée avec succès */
	SUCCESS(0),
	
	/** Erreur d'identification : identifiants ou token incorrects ou expirés notamment */
	// Note : la valeur -1 est utilisée côté client pour les erreurs réseau
	IDENTIFICATION_ERROR(1),
	LDAP_CONNECTION_ERROR(3),
	DATABASE_ERROR(4),
	CRYPTOGRAPHIC_ERROR(5),
	
	/** La requête a été effectuée sans tous les paramètres nécessaires */
	WRONG_PARAMETERS_FOR_REQUEST(6),
	
	/** Un objet invalide a été fourni à une méthode d'enregistrement */
	INVALID_OBJECT(7),

	/** Un objet du même nom est déjà en base de données */
	NAME_TAKEN(8);

	private int code;
	
	ResultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
