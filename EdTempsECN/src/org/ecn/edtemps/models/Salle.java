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

	public String getBatiment() {
		return batiment;
	}

	public int getCapacite() {
		return capacite;
	}

	public ArrayList<Materiel> getMateriels() {
		return materiels;
	}

	public int getNiveau() {
		return niveau;
	}

	public String getNom() {
		return nom;
	}

	public int getNumero() {
		return numero;
	}

	public void setBatiment(String batiment) {
		this.batiment = batiment;
	}

	public void setCapacite(int capacite) {
		this.capacite = capacite;
	}

	public void setMateriels(ArrayList<Materiel> materiels) {
		this.materiels = materiels;
	}

	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	@Override
	public String toString() {
		return this.getNom();
	}

}
