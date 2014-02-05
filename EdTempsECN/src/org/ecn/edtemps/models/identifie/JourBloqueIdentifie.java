package org.ecn.edtemps.models.identifie;

import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.JourBloque;

/**
 * Classe modèle identifiée d'un jour bloqué
 * 
 * @author Joffrey
 */
public class JourBloqueIdentifie extends JourBloque implements JSONAble {

	/** Identifiant du jour bloqué */
	protected int id;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param id Identifiant du jour bloqué
	 * @param libelle Libellé du jour bloqué
	 * @param dateDebut Date de début du jour bloqué
	 * @param dateFin Date de fin du jour bloqué
	 * @param listeGroupes Liste des groupes rattachés à ce jour bloqué
	 * @param vacances VRAI si la période bloquée correspond à des vacances
	 */
	public JourBloqueIdentifie(int id, String libelle, Date dateDebut, Date dateFin, List<GroupeIdentifie> listeGroupes, boolean vacances) {
		super(libelle, dateDebut, dateFin, listeGroupes, vacances);
		this.id = id;
	}
	

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", this.id)
				.add("libelle", this.libelle)
				.add("vacances", this.vacances)
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
