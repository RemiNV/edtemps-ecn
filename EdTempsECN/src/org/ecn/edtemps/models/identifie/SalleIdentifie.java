package org.ecn.edtemps.models.identifie;

import org.ecn.edtemps.models.Salle;

public class SalleIdentifie extends Salle {

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
