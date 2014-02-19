package org.ecn.edtemps.models.identifie;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.Creneau;

/**
 * Classe modèle d'un créneau horaire identifié en base
 * 
 * @author joffrey
 */
public class CreneauIdentifie extends Creneau implements JSONAble {

	protected int id;

	/**
	 * Constructeur
	 * 
	 * @param id Identifiant du créneau dans la base de données
	 * @param libelle Libelle affiché sur les boutons des créneaux
	 * @param debut Horaire de début du créneau
	 * @param fin Horaire de fin du créneau
	 */
	public CreneauIdentifie(int id, String libelle, Date debut, Date fin) {
		super(libelle, debut, fin);
		this.id = id;
	}

	@Override
	public final JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();

		builder.add("id", id);
		builder.add("libelle", libelle);
		builder.add("debut", debut.getTime());
		builder.add("fin", fin.getTime());
		
		return builder.build();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
}
