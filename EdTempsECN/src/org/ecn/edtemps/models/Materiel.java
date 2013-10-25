/**
 * Project: EdTempsECN
 * Creation date: 25 oct. 2013
 * Author: Audrey
 */
package org.ecn.edtemps.models;

/**
 * @author Audrey
 *
 */
public class Materiel {

	/** identifiant du matériel*/
	protected int id;
	

	/** Libellé du matériel*/
	protected String nom;
	
	/** Quantité de matériel disponible dans une salle, si matériel requis, quantité fixé à -1*/
	protected int quantite;
	
	/** 
	 * Constructeur avec les paramètres obligatoires
	 * @param id identifiant du matériel, nom dénomination du matériel
	 */
	public Materiel (int id, String nom){
		this.id = id;
		this.nom = nom;
		quantite = -1;
	}
	
	/** 
	 * Constructeur avec tous les paramètres
	 * @param id identifiant du matériel, nom dénomination du matériel, quantité
	 */
	public Materiel (int id, String nom, int quantite){
		this.id = id;
		this.nom = nom;
		this.quantite = quantite;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @return the quantite
	 */
	public int getQuantite() {
		return quantite;
	}
	
}
