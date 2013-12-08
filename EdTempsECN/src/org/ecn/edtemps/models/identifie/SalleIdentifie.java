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

	/** Identifiant de la salle */
	protected int id;

	/**
	 * Constructeur avec uniquement les informations obligatoires
	 * @param id Identifiant de la salle
	 * @param nom Nom de la salle
	 */
	public SalleIdentifie(int id, String nom) {
		super(nom);
		this.id = id;
	}
	
	/**
	 * Constructeur complet
	 * @param id Identifiant de la salle
	 * @param batiment Bâtiment de localisation
	 * @param nom Nom de la salle
	 * @param capacite Capacité en nombre de places
	 * @param niveau Niveau de la salle dans le bâtiment
	 * @param numero Numéro dans l'étage
	 * @param materiels Liste des matéries contenus
	 */
	public SalleIdentifie(int id, String batiment, String nom, int capacite, int niveau, int numero, ArrayList<Materiel> materiels) {
		super(batiment, nom, capacite, niveau, numero, materiels);
		this.id = id;
	}

	public int getId() {
		return id;
	}

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
		} else {
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
