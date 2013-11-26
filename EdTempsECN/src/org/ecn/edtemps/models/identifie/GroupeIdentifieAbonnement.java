package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;

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
	
	/** [uniquement dans le cas d'un groupe unique] ID des groupes de participants rattachés au calendrier associé au groupe unique */
	protected List<Integer> rattachementsDuCalendrier;
	
	/**
	 * Constructeur utilisant uniquement les informations utiles pour  
	 * la page d'Abonnements/Désabonnements 
	 * 
	 * @param id
	 * @param nom
	 * @param idParent
	 * @param estCalendrierUnique
	 * @param abonnementObligatoire
	 * @param rattachementsDuCalendrier
	 */
	public GroupeIdentifieAbonnement(int id, String nom, int idParent, boolean estCalendrierUnique, 
			boolean abonnementObligatoire, List<Integer> rattachementsDuCalendrier) {
		super(id, nom, idParent, estCalendrierUnique);
		this.abonnementObligatoire = abonnementObligatoire;
		this.rattachementsDuCalendrier = rattachementsDuCalendrier;
	}

	@Override
	public JsonValue toJson() {
		return Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("parentId", parentId)
				.add("abonnementObligatoire", abonnementObligatoire)
				.add("estCalendrierUnique", estCalendrierUnique)
				.add("rattachementsDuCalendrier", JSONUtils.getJsonIntArray(this.rattachementsDuCalendrier))
				.build();
	}

}
