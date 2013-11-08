package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Groupe;

/**
 * Classe modèle d'un groupe de participants identifié
 * auquel on rajoute l'information "abonnementObligatoire" (booléen) [valable pour un utilisateur donnée]. 
 * 
 * @author Maxime Terrade
 */
public class GroupeIdentifieAbonnement extends GroupeIdentifie implements JSONAble {

	/** information "abonnementObligatoire", pour un utilisateur donnée */
	protected boolean abonnementObligatoire;

	/**
	 * Constructeur utilisant les informations indispensables
	 * @param id
	 * @param idProprietaires
	 * @param nom
	 * @param rattachementAutorise
	 * @param estCours
	 * @param estCalendrierUnique
	 * @param abonnementObligatoire
	 */
	public GroupeIdentifieAbonnement(int id, String nom, List<Integer> idProprietaires, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique, boolean abonnementObligatoire) {
		super(id, nom, idProprietaires, rattachementAutorise, estCours, estCalendrierUnique);
		this.abonnementObligatoire = abonnementObligatoire;
	}

	/**
	 * @return abonnementObligatoire
	 */
	public boolean getAbonnementObligatoire() {
		return abonnementObligatoire;
	}

	/**
	 * Affecte une valeur à l'attribut abonnementObligatoire
	 * 
	 * @param abonnementObligatoire
	 */
	public void setAbonnementObligatoire(boolean abonnementObligatoire) {
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
