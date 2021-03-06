package org.ecn.edtemps.models.identifie;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.Utilisateur;

/**
 * Classe d'un utilisateur identifié (dans la base de données)
 * 
 * @author Joffrey
 */
public class UtilisateurIdentifie extends Utilisateur implements JSONAble {

	/** Identifiant de l'utilisateur */
	protected int id;

	/** Statut de l'utilisateur : activé ou non */
	protected boolean active;

	/** Date d'expiration du token */
	protected Date tokenExpiration;

	
	/**
	 * Constructeur
	 * @param id identifiant de l'utilisateur
	 * @param nom nom de l'utilisateur
	 * @param prenom prénom de l'utilisateur
	 * @param email email de l'utilisateur
	 */
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


	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getTokenExpiration() {
		return tokenExpiration;
	}

	public void setTokenExpiration(Date tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}

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
