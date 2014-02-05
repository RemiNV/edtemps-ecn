package org.ecn.edtemps.models;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.HashMap;
import java.util.Map.Entry;

import org.ecn.edtemps.json.JSONAble;

public class Statistiques implements JSONAble {

	protected String matiere;
	
	/**
	 * Clé : type de cours
	 * Pour chaque type de cours : mapping ID du groupe -> statistiques
	 */
	protected HashMap<String, HashMap<Integer, StatistiquesGroupe>> stats;
	
	/**
	 * Création d'un objet statistiques pour une matière
	 * @param matiere La matière
	 */
	public Statistiques(String matiere) {
		this.matiere = matiere;
		this.stats = new HashMap<String, HashMap<Integer, StatistiquesGroupe>>();
	}
	
	/**
	 * Définit les statistiques d'un type de cours pour la matière
	 * @param typeCours Type de cours
	 * @param statsTypeCours Mapping idGroupe->StatistiquesGroupe pour chaque groupe
	 */
	public void setStatistiquesTypeCours(String typeCours, HashMap<Integer, StatistiquesGroupe> statsTypeCours) {
		this.stats.put(typeCours, statsTypeCours);
	}
	
	/**
	 * Récupération des statistiques d'un type de cours pour la matière
	 * @param typeCours Type de cours dont les statistiques sont à récupérer
	 * @return Mapping idGroupe->StatistiquesGroupe pour ce type de cours
	 */
	public HashMap<Integer, StatistiquesGroupe> getStatistiques(String typeCours) {
		return stats.get(typeCours);
	}
	
	public String getMatiere() {
		return matiere;
	}

	/**
	 * Statistiques d'un groupe pour un type de cours
	 * @author Remi
	 */
	public static class StatistiquesGroupe {
		/** Temps de cours déjà prévu (en secondes) */
		protected int actuel;
		/** Temps de cours prévu (en secondes) */
		protected int prevu;
		
		public StatistiquesGroupe(int actuel, int prevu) {
			this.actuel = actuel;
			this.prevu = prevu;
		}
		
		public float getActuel() {
			return actuel;
		}
		
		public float getPrevu() {
			return prevu;
		}
	}
	
	
	
	@Override
	public JsonValue toJson() {
		
		JsonObjectBuilder resBuilder = Json.createObjectBuilder();
		
		// Génération du JSON des statistiques
		JsonObjectBuilder statsBuilder = Json.createObjectBuilder();
		for(Entry<String, HashMap<Integer, StatistiquesGroupe>> statEntry : stats.entrySet()) { // Pour chaque type de cours
			JsonObjectBuilder objectTypeCoursBuilder = Json.createObjectBuilder();
			for(Entry<Integer, StatistiquesGroupe> typeCoursEntry : statEntry.getValue().entrySet()) { // Pour chaque groupe
				JsonObjectBuilder objectStatistiquesBuilder = Json.createObjectBuilder()
						.add("actuel", typeCoursEntry.getValue().getActuel())
						.add("prevu", typeCoursEntry.getValue().getPrevu());
				
				objectTypeCoursBuilder.add(typeCoursEntry.getKey().toString(), objectStatistiquesBuilder);
			}
			
			statsBuilder.add(statEntry.getKey(), objectTypeCoursBuilder);
		}
		
		resBuilder.add("stats", statsBuilder);
		
		// Ajout des autres attributs
		resBuilder.add("matiere", matiere);
		
		return resBuilder.build();
	}
	
}
