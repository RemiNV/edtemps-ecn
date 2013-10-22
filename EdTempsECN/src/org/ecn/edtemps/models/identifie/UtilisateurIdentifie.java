package org.ecn.edtemps.models.identifie;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.Utilisateur;

public class UtilisateurIdentifie extends Utilisateur implements JSONAble {

	public UtilisateurIdentifie(int id, String nom, String prenom, String email) {
		super(nom, prenom, email);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	protected int id;

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("prenom", prenom);
		
		if(email != null) {
			builder.add("email", email);
		}
		else {
			builder.addNull("email");
		}
				
		return builder.build();
	}
}
