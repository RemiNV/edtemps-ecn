package org.ecn.edtemps.models;

import java.util.Date;

/**
 * Classe modèle d'un créneau horaire
 * @author joffrey
 *
 */
public class Creneau {

	protected String libelle;
	protected Date debut;
	protected Date fin;
	
	/**
	 * Constructeur de base
	 * 
	 * @param libelle Libelle affiché sur les boutons des créneaux
	 * @param debut Horaire de début du créneau
	 * @param fin Horaire de fin du créneau
	 */
	public Creneau(String libelle, Date debut, Date fin) {
		this.libelle = libelle;
		this.debut = debut;
		this.fin = fin;
	}

	public String getLibelle() {
		return libelle;
	}
	public Date getDebut() {
		return debut;
	}
	public Date getFin() {
		return fin;
	}
	
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public void setDebut(Date debut) {
		this.debut = debut;
	}
	public void setFin(Date fin) {
		this.fin = fin;
	}
	
}
