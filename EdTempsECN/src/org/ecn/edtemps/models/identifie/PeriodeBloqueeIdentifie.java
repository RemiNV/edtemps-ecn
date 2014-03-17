package org.ecn.edtemps.models.identifie;

import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.PeriodeBloquee;

/**
 * Classe modèle identifiée d'une période bloquée : jours bloqués, vacances ou fermeture de l'école
 * 
 * @author Joffrey
 */
public class PeriodeBloqueeIdentifie extends PeriodeBloquee implements JSONAble {

	/** Identifiant de la période bloquée */
	protected int id;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param id Identifiant de la période
	 * @param libelle Libellé de la période
	 * @param dateDebut Date de début de la période
	 * @param dateFin Date de fin de la période
	 * @param listeGroupes Liste des groupes rattachés à la période
	 * @param vacances VRAI si la période bloquée correspond à des vacances
	 * @param fermeture VRAI si la période bloquée correspond à une fermeture
	 */
	public PeriodeBloqueeIdentifie(int id, String libelle, Date dateDebut, Date dateFin, List<GroupeIdentifie> listeGroupes, boolean vacances, boolean fermeture) {
		super(libelle, dateDebut, dateFin, listeGroupes, vacances, fermeture);
		this.id = id;
	}
	

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", this.id)
				.add("libelle", this.libelle)
				.add("vacances", this.vacances)
				.add("fermeture", this.fermeture)
				.add("dateDebut", this.dateDebut.getTime())
				.add("dateFin", this.dateFin.getTime())
				.add("listeGroupes", JSONUtils.getJsonArray(this.listeGroupes));
		return builder.build();
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
