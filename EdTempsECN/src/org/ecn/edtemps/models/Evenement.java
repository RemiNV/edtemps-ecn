package org.ecn.edtemps.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

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
	protected List<SalleIdentifie> salles;
	
	/** Liste des noms des intervenants de l'événement */
	protected List<UtilisateurIdentifie> intervenants;
	
	/** Liste des noms des responsables de l'�v�nement */
	protected List<UtilisateurIdentifie> responsables;
	
	/** Liste de mat�riel n�cessaire sous forme de map avec l'identifiant du mat�riel et la quantit�*/
	protected Map<Integer, Integer> materiels;
	
	/** Constructeur avec uniquement attributs indispensables */
	public Evenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers) {
		this.nom = nom;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.idCalendriers = idCalendriers;
	}


	/** Constructeur avec tous les attributs */
	public Evenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<SalleIdentifie> salles,
			List<UtilisateurIdentifie> intervenants, List<UtilisateurIdentifie> responsables, Map<Integer, Integer> materiels) {
		this(nom, dateDebut, dateFin, idCalendriers);
		this.salles = salles;
		this.intervenants = intervenants;
		this.responsables = responsables;
		this.materiels = materiels;
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

	public List<SalleIdentifie> getSalles() {
		return salles;
	}

	public void setSalles(List<SalleIdentifie> salles) {
		this.salles = salles;
	}

	public List<UtilisateurIdentifie> getIntervenants() {
		return intervenants;
	}

	public void setIntervenants(List<UtilisateurIdentifie> intervenants) {
		this.intervenants = intervenants;
	}
	
	/**
	 * @return the responsables
	 */
	public List<UtilisateurIdentifie> getResponsables() {
		return responsables;
	}


	/**
	 * @param responsables the responsables to set
	 */
	public void setResponsables(List<UtilisateurIdentifie> responsables) {
		this.responsables = responsables;
	}


	/**
	 * @return the materiels
	 */
	public Map<Integer, Integer> getMateriels() {
		return materiels;
	}


	/**
	 * @param materiels the materiels to set
	 */
	public void setMateriels(Map<Integer, Integer> materiels) {
		this.materiels = materiels;
	}
}
