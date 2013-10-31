package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Groupe;

/**
 * Classe modèle d'un groupe de participants identifié
 * 
 * @author Joffrey
 */
public class GroupeIdentifie extends Groupe implements JSONAble {

	/** Identifiant de la salle */
	protected int id;

	/**
	 * Constructeur utilisant les informations indispensables
	 * @param id
	 * @param idProprietaires
	 * @param nom
	 * @param rattachementAutorise
	 * @param estCours
	 * @param estCalendrierUnique
	 */
	public GroupeIdentifie(int id, String nom, List<Integer> idProprietaires, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique) {
		super(nom, idProprietaires, rattachementAutorise, estCours, estCalendrierUnique);
		this.id = id;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Affecte une valeur à l'attribut id
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public JsonValue toJson() {
		return Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("parentId", parentId)
				.add("rattachementAutorise", rattachementAutorise)
				.add("estCours", estCours)
				.add("estCalendrierUnique", estCalendrierUnique)
				.add("calendriers", JSONUtils.getJsonIntArray(idCalendriers))
				.add("proprietaires", JSONUtils.getJsonIntArray(idProprietaires))
				.build();
	}

}
