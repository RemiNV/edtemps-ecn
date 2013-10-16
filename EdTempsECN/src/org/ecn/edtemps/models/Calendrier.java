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

	/** 
	 * Getter de l'attribut nom
	 * 
	 * @return
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Setter de l'attribut nom
	 * 
	 * @param nom
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * Getter de l'attribut type
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter de l'attribut type
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter de l'attribut matiere
	 * 
	 * @return
	 */
	public String getMatiere() {
		return matiere;
	}
	
	/**
	 * Setter de l'attribut matiere
	 * 
	 * @param matiere
	 */
	public void setMatiere(String matiere) {
		this.matiere = matiere;
	}
	
	
	/**
	 * Getter de la liste des proprietaires
	 * 
	 * @return
	 */
	public List<Integer> getIdProprietaires() {
		return idProprietaires;
	}

	/**
	 * Setter de la liste des proprietaires
	 * 
	 * @param idProprietaires
	 */
	public void setIdProprietaires(List<Integer> idProprietaires) {
		this.idProprietaires = idProprietaires;
	}
	
}
