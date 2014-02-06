package org.ecn.edtemps.models;

import java.util.Date;
import java.util.List;

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
	
	/** Liste des noms des responsables de l'événement */
	protected List<UtilisateurIdentifie> responsables;
	
	/** ID de l'utilisateur qui a créé l'événement ; peut être null si il a été supprimé */
	protected Integer idCreateur;
	
	/** Constructeur avec uniquement attributs indispensables
	 * @param nom Nom de l'événement
	 * @param dateDebut Date de début
	 * @param dateFin Date de fin
	 * @param idCalendriers Identifiants des calendriers liés
	 * @param idCreateur Identifiant du créateur
	 */
	public Evenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, Integer idCreateur) {
		this.nom = nom;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.idCalendriers = idCalendriers;
		this.idCreateur = idCreateur;
	}


	/** Constructeur avec tous les attributs
	 * @param nom Nom de l'événement
	 * @param dateDebut Date de début
	 * @param dateFin Date de fin
	 * @param idCalendriers Identifiants des calendriers liés
	 * @param idCreateur Identifiant du créateur
	 * @param salles Liste des salles de l'événement
	 * @param intervenants Liste des intervenants 
	 * @param responsables Liste des responsables
	 */
	public Evenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, Integer idCreateur, List<SalleIdentifie> salles,
			List<UtilisateurIdentifie> intervenants, List<UtilisateurIdentifie> responsables) {
		this(nom, dateDebut, dateFin, idCalendriers, idCreateur);
		this.salles = salles;
		this.intervenants = intervenants;
		this.responsables = responsables;
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
	
	public Integer getIdCreateur() {
		return this.idCreateur;
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
	
	public List<UtilisateurIdentifie> getResponsables() {
		return responsables;
	}

	public void setResponsables(List<UtilisateurIdentifie> responsables) {
		this.responsables = responsables;
	}
	
	@Override
	public String toString() {
		return nom;
	}
}
