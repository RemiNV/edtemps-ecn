package org.ecn.edtemps.models;

import java.sql.Time;

/**
 * Classe modèle d'un créneau horaire
 * @author joffrey
 *
 */
public class Creneau {

	protected String libelle;
	protected Time debut;
	protected Time fin;
	
	/**
	 * Constructeur de base
	 * 
	 * @param libelle Libelle affiché sur les boutons des créneaux
	 * @param debut Horaire de début du créneau
	 * @param fin Horaire de fin du créneau
	 */
	public Creneau(String libelle, Time debut, Time fin) {
		this.libelle = libelle;
		this.debut = debut;
		this.fin = fin;
	}

	public String getLibelle() {
		return libelle;
	}
	public Time getDebut() {
		return debut;
	}
	public Time getFin() {
		return fin;
	}
	
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public void setDebut(Time debut) {
		this.debut = debut;
	}
	public void setFin(Time fin) {
		this.fin = fin;
	}
	
}
