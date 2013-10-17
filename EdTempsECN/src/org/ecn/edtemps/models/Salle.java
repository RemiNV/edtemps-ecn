package org.ecn.edtemps.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe modèle d'une salle
 * 
 * @author Joffrey
 */
public class Salle {

	/** Nom du bâtiment */
	protected String batiment;

	/** Capacité de la salle */
	protected int capacite;

	/** Map du matériel qui équipe la salle avec la quantité */
	protected Map<Integer, Integer> materiels;

	/** Niveau de la salle */
	protected int niveau;

	/** Nom de la salle */
	protected String nom;

	/** Numéro de la salle */
	protected int numero;
	
	/**
	 * Constructeur avec les paramètres obligatoires
	 */
	public Salle(String nom) {
		this.nom = nom;
		this.materiels = new HashMap<Integer, Integer>();
	}

	/**
	 * Constructeur avec tous les paramètres
	 */
	public Salle(String batiment, String nom, int capacite, int niveau,
			int numero, Map<Integer, Integer> materiels) {
		this.batiment = batiment;
		this.capacite = capacite;
		this.niveau = niveau;
		this.numero = numero;
		this.nom = nom;
		this.materiels = materiels;
	}

	/**
	 * @return batiment
	 */
	public String getBatiment() {
		return batiment;
	}

	/**
	 * @return capacite
	 */
	public int getCapacite() {
		return capacite;
	}

	/**
	 * @return materiels
	 */
	public Map<Integer, Integer> getMateriels() {
		return materiels;
	}

	/**
	 * @return niveau
	 */
	public int getNiveau() {
		return niveau;
	}

	/**
	 * @return nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @return numero
	 */
	public int getNumero() {
		return numero;
	}

	/**
	 * Affecte une valeur à l'attribut batiment
	 * 
	 * @param batiment
	 */
	public void setBatiment(String batiment) {
		this.batiment = batiment;
	}

	/**
	 * Affecte une valeur à l'attribut capacite
	 * 
	 * @param capacite
	 */
	public void setCapacite(int capacite) {
		this.capacite = capacite;
	}

	/**
	 * Affecte une valeur à l'attribut materiels
	 * 
	 * @param materiels
	 */
	public void setMateriels(Map<Integer, Integer> materiels) {
		this.materiels = materiels;
	}

	/**
	 * Affecte une valeur à l'attribut niveau
	 * 
	 * @param niveau
	 */
	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}

	/**
	 * Affecte une valeur à l'attribut nom
	 * 
	 * @param nom
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * Affecte une valeur à l'attribut numero
	 * 
	 * @param numero
	 */
	public void setNumero(int numero) {
		this.numero = numero;
	}

}
