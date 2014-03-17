package org.ecn.edtemps.models;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;

/**
 * Classe modèle d'un jour férié
 * 
 * @author Joffrey
 */
public class JourFerie implements JSONAble {

	/** Libellé */
	protected String libelle;
	
	/** Date */
	protected Date date;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param libelle Libellé du jour férié
	 * @param date Date du jour férié
	 */
	public JourFerie (String libelle, Date date) {
		this.libelle = libelle;
		this.date = date;
	}
	

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("libelle", this.libelle)
				.add("date", this.date.getTime());
		return builder.build();
	}


	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
