package org.ecn.edtemps.models.identifie;

import org.ecn.edtemps.models.Calendrier;

/**
 * Classe modèle d'un calendrier identifié
 * 
 * @author Maxime TERRADE
 */
public class CalendrierIdentifie extends Calendrier {

	/** Identifiant du calendrier dans la base de données */
	protected int id;

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
