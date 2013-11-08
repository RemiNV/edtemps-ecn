package org.ecn.edtemps.models.identifie;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;


/**
 * Classe modèle d'un groupe de participants identifié
 * auquel on rajoute l'information "abonnementObligatoire" (booléen) [valable pour un utilisateur donnée].
 * 
 *  Seules les informations utiles pour la page d'Abonnements/Désabonnements seront renseignées 
 * 
 * @author Maxime Terrade
 */
public class GroupeIdentifieAbonnement extends GroupeIdentifie implements JSONAble {

	/** information "abonnementObligatoire", pour un utilisateur donnée */
	protected boolean abonnementObligatoire;
	
	/**
	 * Constructeur utilisant uniquement les informations utiles pour  
	 * la page d'Abonnements/Désabonnements 
	 * 
	 * @param id
	 * @param nom
	 * @param idParent
	 * @param abonnementObligatoire
	 */
	public GroupeIdentifieAbonnement(int id, String nom, int idParent, boolean abonnementObligatoire) {
		super(id, nom, idParent);
		this.abonnementObligatoire = abonnementObligatoire;
	}

	@Override
	public JsonValue toJson() {
		return Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("parentId", parentId)
				.add("abonnementObligatoire", abonnementObligatoire)
				.build();
	}

}
