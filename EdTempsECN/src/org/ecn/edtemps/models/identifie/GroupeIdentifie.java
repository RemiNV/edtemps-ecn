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
	 * Constructeur utilisant les informations indispensables
	 * @param id
	 * @param nom
	 * @param rattachementAutorise
	 * @param estCours
	 * @param estCalendrierUnique
	 */
	public GroupeIdentifie(int id, String nom, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique) {
		super(nom, rattachementAutorise, estCours, estCalendrierUnique);
		this.id = id;
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
