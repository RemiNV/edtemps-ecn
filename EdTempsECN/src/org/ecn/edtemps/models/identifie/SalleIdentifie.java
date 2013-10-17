package org.ecn.edtemps.models.identifie;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Salle;

/**
 * Classe modèle d'une salle identifiée
 * 
 * @author Joffrey
 */
public class SalleIdentifie extends Salle implements JSONAble {

	public SalleIdentifie(int id, String nom) {
		super(nom);
		
		this.id = id;
	}
	
	public SalleIdentifie(int id, String batiment, String nom, int capacite, int niveau, int numero, HashMap<Integer, Integer> materiels) {
		super(batiment, nom, capacite, niveau, numero, materiels);
		
		this.id = id;
	}

	/** Identifiant de la salle */
	protected int id;

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
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("batiment", batiment)
				.add("capacite", capacite)
				.add("niveau", niveau)
				.add("numero", numero);
		
		// Ajout des matériels
		JsonArrayBuilder materielsArrayBuilder = Json.createArrayBuilder();
		for(Map.Entry<Integer, Integer> materiel : materiels.entrySet()) {
			materielsArrayBuilder.add(Json.createObjectBuilder().add("id", materiel.getKey()).add("quantite", materiel.getValue()).build());
		}
		
		builder.add("materiels", materielsArrayBuilder.build());
		
		return builder.build();
	}

}
