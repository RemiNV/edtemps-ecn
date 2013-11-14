package org.ecn.edtemps.json;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.exceptions.ResultCode;

/**
 * Classe permettant de générer des réponses JSON à partir d'objets à transmettre
 * 
 * @author Remi
 */
public class ResponseManager {

	/**
	 * Génération d'une réponse sous forme de texte à partir des éléments nécessaires 
	 * @param repCode Code de réponse à la requête
	 * @param message Message à indiquer, souvent une chaîne vide si pas d'erreur
	 * @param data Données à transmettre, ou null si non nécessaire
	 * @return Chaîne de réponse JSON générée
	 */
	public static String generateResponse(ResultCode repCode, String message, JsonValue data) {
		
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("resultCode", repCode.getCode())
				.add("message", message);
		
		if(data == null)
			builder.addNull("data");
		else
			builder.add("data", data);
				
		JsonObject res = builder.build();
		
		// Représentation JSON de l'objet
		return res.toString();
	}
}
