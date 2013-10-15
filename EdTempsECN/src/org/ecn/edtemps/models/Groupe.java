package org.ecn.edtemps.models;

import java.util.List;

/**
 * Classe modèle d'un groupe de participants
 * 
 * @author Joffrey
 */
public class Groupe {

	/** Liste des identifiants des calendriers */
	protected List<Integer> idCalendriers;

	/** Liste des identifiants des propriétaires */
	protected List<Integer> idProprietaires;

	/** Nom du groupe de participants */
	protected String nom;

	/** Identifiant du groupe parent */
	protected int parentId;

	/** Est-ce que le groupe peut avoir un rattachement */
	protected boolean rattachementAutorise;

	/**
	 * @return idCalendriers
	 */
	public List<Integer> getIdCalendriers() {
		return idCalendriers;
	}

	/**
	 * @return idProprietaires
	 */
	public List<Integer> getIdProprietaires() {
		return idProprietaires;
	}

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
	 * Affecte une valeur à l'attribut idCalendriers
	 * 
	 * @param idCalendriers
	 */
	public void setIdCalendriers(List<Integer> idCalendriers) {
		this.idCalendriers = idCalendriers;
	}

	/**
	 * Affecte une valeur à l'attribut idProprietaires
	 * 
	 * @param idProprietaires
	 */
	public void setIdProprietaires(List<Integer> idProprietaires) {
		this.idProprietaires = idProprietaires;
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
