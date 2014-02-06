package org.ecn.edtemps.models;

import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;

public class TestRepetitionEvenement implements JSONAble {
	
	protected String num;
	protected Date dateDebut;
	protected Date dateFin;
	
	/** Liste des résultats : peut contenir plusieurs éléments (problèmes) si n'est pas "OK" */
	protected ArrayList<Probleme> resultats;
	
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
	
	public TestRepetitionEvenement(String num, Date dateDebut, Date dateFin, ArrayList<Probleme> resultats) {
		this.num = num;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.resultats = resultats;
	}
	
	
	@Override
	public JsonValue toJson() {
		return Json.createObjectBuilder()
				.add("num", num)
				.add("debut", dateDebut.getTime())
				.add("fin", dateFin.getTime())
				.add("resultats", JSONUtils.getJsonArray(resultats))
				.build();
	}

}
