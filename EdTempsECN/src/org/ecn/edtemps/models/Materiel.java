package org.ecn.edtemps.models;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;

/**
 * Classe modèle d'un type matériel
 * 
 * @author Audrey
 */
public class Materiel implements JSONAble {

	/** Identifiant du matériel*/
	protected int id;

	/** Libellé du matériel*/
	protected String nom;
	
	/** Quantité de matériel disponible dans une salle*/
	protected int quantite;
	
	/** 
	 * Constructeur avec les paramètres obligatoires
	 * @param id Identifiant du matériel
	 * @param nom Dénomination du matériel
	 */
	public Materiel (int id, String nom){
		this.id = id;
		this.nom = nom;
		quantite = 0;
	}
	
	/** 
	 * Constructeur avec tous les paramètres
	 * @param id Identifiant du matériel
	 * @param nom Dénomination du matériel
	 * @param quantite Quantité du matériel (utile pour indiquer la quantité d'un matériel dans une salle)
	 */
	public Materiel (int id, String nom, int quantite){
		this.id = id;
		this.nom = nom;
		this.quantite = quantite;
	}

	public int getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public int getQuantite() {
		return quantite;
	}

	@Override
	public JsonValue toJson() {
		
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", this.id)
				.add("nom", this.nom)
				.add("quantite", this.quantite);
		
		return builder.build();

	}
	
	/**
	 * Lit un objet Materiel à partir de sa représentation JSON
	 * @param object Représentation JSON du matériel (attributs id, nom, quantite)
	 * @return Matériel créé
	 * @throws ClassCastException Si un attribut est présent mais invalide
	 */
	public static Materiel inflateFromJson(JsonObject object) throws ClassCastException {
		JsonNumber jsonId = object.getJsonNumber("id");
		String nom = object.getString("nom", "");
		JsonNumber jsonQuantite = object.getJsonNumber("quantite");
		
		return new Materiel(jsonId != null ? jsonId.intValue() : 0, nom, jsonQuantite != null ? jsonQuantite.intValue() : 0);
	}
	
}
