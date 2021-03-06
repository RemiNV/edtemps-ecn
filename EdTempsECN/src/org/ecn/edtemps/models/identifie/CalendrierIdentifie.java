package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Calendrier;

/**
 * Classe modèle d'un calendrier identifié
 * 
 * @author Maxime TERRADE
 */
public class CalendrierIdentifie extends Calendrier implements JSONAble {

	/** Identifiant du calendrier dans la base de données */
	protected int id;
	
	
	/** Constructeur avec tous les attributs
	 * @param nom Nom du calendrier
	 * @param type Type de cours du calendrier
	 * @param matiere Matière lié au calendrier
	 * @param idProprietaires Liste des indentifiants des propriétaires
	 * @param id Identifiant du calendrier
	 * @param idCreateur Identifiant du créateur
	 */
	public CalendrierIdentifie(String nom, String type, String matiere, List<Integer> idProprietaires, int id, int idCreateur) {
		super(nom, type, matiere, idProprietaires, idCreateur);
		this.id = id;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Création d'un builder Json
	 * @return builder
	 */
	protected JsonObjectBuilder makeObjectBuilder() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("createur", idCreateur)
				.add("proprietaires", JSONUtils.getJsonIntArray(idProprietaires));
		
		if(type != null) {
			builder.add("type", type);
		} else {
			builder.addNull("type");
		}
		
		if(matiere != null) {
			builder.add("matiere", matiere);
		} else {
			builder.addNull("matiere");
		}
		
		return builder;
	}

	@Override
	public final JsonValue toJson() {
		JsonObjectBuilder builder = makeObjectBuilder();
		
		return builder.build();
	}

}
