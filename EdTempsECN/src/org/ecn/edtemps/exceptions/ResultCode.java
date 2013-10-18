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
	WRONG_PARAMETERS_FOR_REQUEST(6);
	
	private int code;
	
	ResultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
