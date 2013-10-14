package org.ecn.edtemps.models;

/**
 * Classe d'objet de matériel
 * 
 * @author Joffrey
 */
public class Materiel {

	/** Nom du matériel */
	protected String nom;

	/** Quantité du matériel dans la salle */
	protected int quantite;

	/**
	 * Constructeur vide
	 */
	public Materiel() {

	}

	/**
	 * @return nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Affecte une valeur à l'attribut quantite
	 * 
	 * @param quantite
	 */
	public int getQuantite() {
		return quantite;
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
	 * @return quantite
	 */
	public void setQuantite(int quantite) {
		this.quantite = quantite;
	}

}
