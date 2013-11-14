package org.ecn.edtemps.models;

import java.util.List;

/**
 * Classe modèle d'un calendrier
 * 
 * @author Maxime TERRADE
 */
public class Calendrier {
	
	/** Nom du calendrier */
	protected String nom;

	/** Type du calendrier. Ex: "CM", "TD", "TP", ... */
	protected String type;

	/** Matière associée au calendrier */
	protected String matiere;

	/** Proprietaires du calendrier */
	protected List<Integer> idProprietaires;

	/** 
	 * Constructeur avec tous les attributs
	 * 
	 * @param nom Nom du calendrier
	 * @param type Type du calendrier
	 * @param matiere Matière du calendrier
	 * @param idProprietaires Propriétaires du calendrier
	 */
	public Calendrier(String nom, String type, String matiere, List<Integer> idProprietaires) {
		this.nom = nom;
		this.type = type;
		this.matiere = matiere;
		this.idProprietaires = idProprietaires;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMatiere() {
		return matiere;
	}
	
	public void setMatiere(String matiere) {
		this.matiere = matiere;
	}
	
	public List<Integer> getIdProprietaires() {
		return idProprietaires;
	}

	public void setIdProprietaires(List<Integer> idProprietaires) {
		this.idProprietaires = idProprietaires;
	}
	
}
