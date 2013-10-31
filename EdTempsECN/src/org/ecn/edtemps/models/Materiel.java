/**
 * Project: EdTempsECN
 * Creation date: 25 oct. 2013
 * Author: Audrey
 */
package org.ecn.edtemps.models;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;

/**
 * @author Audrey
 *
 */
public class Materiel implements JSONAble {

	/** identifiant du matériel*/
	protected int id;
	

	/** Libellé du matériel*/
	protected String nom;
	
	/** Quantité de matériel disponible dans une salle*/
	protected int quantite;
	
	/** 
	 * Constructeur avec les paramètres obligatoires
	 * @param id identifiant du matériel, nom dénomination du matériel
	 */
	public Materiel (int id, String nom){
		this.id = id;
		this.nom = nom;
		quantite = 0;
	}
	
	/** 
	 * Constructeur avec tous les paramètres
	 * @param id identifiant du matériel, nom dénomination du matériel, quantité
	 */
	public Materiel (int id, String nom, int quantite){
		this.id = id;
		this.nom = nom;
		this.quantite = quantite;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @return the quantite
	 */
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
	
}
