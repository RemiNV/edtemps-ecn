package org.ecn.edtemps.models;

import java.util.ArrayList;

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

	// TODO : cet attribut ne sert pas dans la plupart des cas d'utilisation, mais demande une requête BDD en plus.
	// TODO : définir un objet Salle "light" pour la plupart des requêtes ?
	/** Matériel (id, nom, quantité) qui équipe la salle */
	protected ArrayList<Materiel> materiels;

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
		this.materiels = new ArrayList<Materiel>();
	}

	/**
	 * Constructeur avec tous les paramètres
	 */
	public Salle(String batiment, String nom, int capacite, int niveau,
			int numero, ArrayList<Materiel> materiels) {
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
	public ArrayList<Materiel> getMateriels() {
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
	public void setMateriels(ArrayList<Materiel> materiels) {
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

	@Override
	public String toString() {
		return this.getNom();
	}

}
