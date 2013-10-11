package org.ecn.edtemps.models;

/**
 * Classe d'objet de salle
 * 
 * @author Joffrey
 */
public class Salle {

	/** Nom du bâtiment */
	protected String batiment;

	/** Capacité de la salle */
	protected int capacite;

	/** Niveau de la salle */
	protected int niveau;

	/** Numéro de la salle */
	protected int numero;

	/**
	 * Constructeur vide
	 */
	public Salle() {

	}

	/**
	 * Constructeur avec tous les paramètres
	 */
	public Salle(String batiment, int capacite, int niveau, int numero) {
		this.batiment = batiment;
		this.capacite = capacite;
		this.niveau = niveau;
		this.numero = numero;
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
	 * @return niveau
	 */
	public int getNiveau() {
		return niveau;
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
	 * Affecte une valeur à l'attribut niveau
	 * 
	 * @param niveau
	 */
	public void setNiveau(int niveau) {
		this.niveau = niveau;
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
