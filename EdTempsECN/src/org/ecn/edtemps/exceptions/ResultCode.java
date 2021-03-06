package org.ecn.edtemps.exceptions;

/**
 * Codes de retour des requêtes
 * 
 * @author Remi
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
	NAME_TAKEN(8),
	
	/** L'utilisateur n'est pas autorisé à effectuer cette action */
	AUTHORIZATION_ERROR(9),

	/** Une salle demandée est occupée pendant le créneau donné */
	SALLE_OCCUPEE(10),

	/** Une valeur alphanumérique est requise */
	ALPHANUMERIC_REQUIRED(11),
	
	/** Une requête a été annulée car le nombre de résultats était trop important */
	MAX_ROW_COUNT_EXCEEDED(12),
	
	/** L'utilisateur a dépassé son quota d'ajout */
	QUOTA_EXCEEDED(13),

	/** Jour déjà existant en base (pour les jours fériés) */
	DAY_TAKEN(14);

	private int code;
	
	ResultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
