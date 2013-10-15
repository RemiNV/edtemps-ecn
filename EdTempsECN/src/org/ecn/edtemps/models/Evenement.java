package org.ecn.edtemps.models;

import java.util.Date;
import java.util.List;

import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Classe modèle d'un événement
 * 
 * @author Maxime TERRADE
 */
public class Evenement {
	
	/** Nom de l'évènement */
	protected String nom;
	
	/** Date&Heure de début de l'événement */
	protected Date dateDebut;

	/** Date&Heure de fin de l'événement */
	protected Date dateFin;
	
	/** Listes des id des calendriers associés */
	protected List<Integer> idCalendriers;
	
	/** Salle associée à l'événement */
	protected SalleIdentifie salle;
	
	/** Liste des noms des intervenants de l'événement */
	protected List<String> intervenants;

	
	/** Constructeur par défaut */
	public Evenement() {
		
	}
	
	
	/** Constructeur avec tous les attributs */
	public Evenement(String nom, Date dateDebut, Date dateFin,
			List<Integer> idCalendriers, SalleIdentifie salle,
			List<String> intervenants) {
		this.nom = nom;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.idCalendriers = idCalendriers;
		this.salle = salle;
		this.intervenants = intervenants;
	}

	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public List<Integer> getIdCalendriers() {
		return idCalendriers;
	}

	public void setIdCalendriers(List<Integer> idCalendriers) {
		this.idCalendriers = idCalendriers;
	}

	public SalleIdentifie getSalle() {
		return salle;
	}

	public void setSalle(SalleIdentifie salle) {
		this.salle = salle;
	}

	public List<String> getIntervenants() {
		return intervenants;
	}

	public void setIntervenants(List<String> intervenants) {
		this.intervenants = intervenants;
	}
	
}
