package org.ecn.edtemps.models;

import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;

public class TestRepetitionEvenement implements JSONAble {
	
	/** Numéro de l'événement. Plusieurs événements peuvent avoir le même numéro (mais un seul de ceux-ci n'aura pas de problème) */
	protected int num;
	protected Date dateDebut;
	protected Date dateFin;
	
	/** Liste des problèmes : est vide si "OK" */
	protected ArrayList<Probleme> problemes;
	
	public static class Probleme implements JSONAble {
		protected ProblemeStatus status;
		protected String message;
		
		public Probleme(ProblemeStatus status, String message) {
			this.status = status;
			this.message = message;
		}
		
		public ProblemeStatus getStatus() {
			return status;
		}
		
		public String getMessage() {
			return message;
		}
		
		@Override
		public JsonValue toJson() {
			return Json.createObjectBuilder()
					.add("status", status.getInt())
					.add("message", message)
					.build();
		}
	}
	
	public static enum ProblemeStatus {
		SALLE_OCCUPEE_COURS(1),
		SALLE_OCCUPEE_NON_COURS(2),
		PUBLIC_OCCUPE(3),
		JOUR_BLOQUE(4);
		
		protected int status;
		
		ProblemeStatus(int status) {
			this.status = status;
		}
		
		public int getInt() {
			return status;
		}
	}
	
	public TestRepetitionEvenement(int num, Date dateDebut, Date dateFin, ArrayList<Probleme> problemes) {
		this.num = num;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.problemes = problemes;
	}
	
	
	@Override
	public JsonValue toJson() {
		return Json.createObjectBuilder()
				.add("num", num)
				.add("debut", dateDebut.getTime())
				.add("fin", dateFin.getTime())
				.add("problemes", JSONUtils.getJsonArray(problemes))
				.build();
	}

}
