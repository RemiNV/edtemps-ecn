package org.ecn.edtemps.models.identifie;

import java.util.HashMap;

import org.ecn.edtemps.models.Salle;

/**
 * Classe modèle d'une salle identifiée
 * 
 * @author Joffrey
 */
public class SalleIdentifie extends Salle {

	public SalleIdentifie(int id, String nom) {
		super(nom);
		
		this.id = id;
	}
	
	public SalleIdentifie(int id, String batiment, String nom, int capacite, int niveau, int numero, HashMap<Integer, Integer> materiels) {
		super(batiment, nom, capacite, niveau, numero, materiels);
		
		this.id = id;
	}

	/** Identifiant de la salle */
	protected int id;

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Affecte une valeur à l'attribut id
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

}
