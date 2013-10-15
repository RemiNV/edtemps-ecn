package org.ecn.edtemps.models.identifie;

import org.ecn.edtemps.models.Groupe;

/**
 * Classe modèle d'un groupe de participants identifié
 * 
 * @author Joffrey
 */
public class GroupeIdentifie extends Groupe {

	/** Identifiant de la salle */
	protected int id;

	/**
	 * Constructeur vide
	 */
	public GroupeIdentifie() {
		super();
	}

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

