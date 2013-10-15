package org.ecn.edtemps.models;

/**
 * Classe modèle d'un groupe de participants
 * 
 * @author Joffrey
 */
public class Groupe {

	/** Nom du groupe de participants */
	protected String nom;

	/** Identifiant du groupe parent */
	protected int parentId;

	/** Est-ce que le groupe peut avoir un rattachement */
	protected boolean rattachementAutorise;

	/**
	 * @return nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @return parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @return rattachementAutorise
	 */
	public boolean getRattachementAutorise() {
		return rattachementAutorise;
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
	 * Affecte une valeur à l'attribut parentId
	 * 
	 * @param parentId
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * Affecte une valeur à l'attribut rattachementAutorise
	 * 
	 * @param rattachementAutorise
	 */
	public void setRattachementAutorise(boolean rattachementAutorise) {
		this.rattachementAutorise = rattachementAutorise;
	}

}
