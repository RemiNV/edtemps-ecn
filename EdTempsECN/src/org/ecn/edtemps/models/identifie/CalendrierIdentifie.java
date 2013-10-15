package org.ecn.edtemps.models.identifie;

import org.ecn.edtemps.models.Calendrier;

public class CalendrierIdentifie extends Calendrier {

	/** Identifiant du calendrier dans la base de données */
	protected int id;

	
	/** Constructeur par défaut */
	public CalendrierIdentifie() {
		super();
	}
	
	/** Constructeur avec tous les attributs */
	public CalendrierIdentifie(String nom, String type, String matiere, int id) {
		super(nom, type, matiere);
		this.id = id;
	}

	/**
	 * Getter de l'ID
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Setter de l'ID
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

}
