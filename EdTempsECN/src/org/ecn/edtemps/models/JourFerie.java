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

	/** Jour de fermeture */
	protected boolean fermeture;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param libelle Libellé du jour férié
	 * @param date Date du jour férié
	 * @param fermeture Vrai, si c'est un jour de fermeture
	 */
	public JourFerie (String libelle, Date date, boolean fermeture) {
		this.libelle = libelle;
		this.date = date;
		this.fermeture = fermeture;
	}
	

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("libelle", this.libelle)
				.add("fermeture", this.fermeture)
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


	public boolean getFermeture() {
		return fermeture;
	}


	public void setFermeture(boolean fermeture) {
		this.fermeture = fermeture;
	}
	
}
