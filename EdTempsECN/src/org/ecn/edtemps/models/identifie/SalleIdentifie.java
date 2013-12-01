package org.ecn.edtemps.models.identifie;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.Materiel;
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
	
	public SalleIdentifie(int id, String batiment, String nom, int capacite, int niveau, int numero, ArrayList<Materiel> materiels) {
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
	
	protected JsonObjectBuilder makeJsonObjectBuilder() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("capacite", capacite)
				.add("niveau", niveau)
				.add("numero", numero);
		
		if(batiment == null) {
			builder.addNull("batiment");
		}
		else {
			builder.add("batiment", batiment);
		}
		
		// Ajout des matériels
		JsonArrayBuilder materielsArrayBuilder = Json.createArrayBuilder();
		for(Materiel materiel : materiels) {
			materielsArrayBuilder.add(Json.createObjectBuilder()
					.add("id", materiel.getId())
					.add("nom", materiel.getNom())
					.add("quantite", materiel.getQuantite())
					.build());
		}
		builder.add("materiels", materielsArrayBuilder.build());
		
		return builder;
	}

	@Override
	public final JsonValue toJson() {
		return makeJsonObjectBuilder().build();
	}

}
