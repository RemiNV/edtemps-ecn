package org.ecn.edtemps.models.identifie;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.JourFerie;

/**
 * Classe modèle identifiée d'un jour férié
 * 
 * @author Joffrey
 */
public class JourFerieIdentifie extends JourFerie implements JSONAble {

	/** Identifiant du jour férié */
	protected int id;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param id Identifiant du jour férié
	 * @param libelle Libellé du jour férié
	 * @param date Date du jour férié
	 * @param fermeture Vrai, si c'est un jour de fermeture
	 */
	public JourFerieIdentifie(int id, String libelle, Date date, boolean fermeture) {
		super(libelle, date, fermeture);
		this.id = id;
	}
	
	
	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", this.id)
				.add("libelle", this.libelle)
				.add("fermeture", this.fermeture)
				.add("date", this.date.getTime());
		return builder.build();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
