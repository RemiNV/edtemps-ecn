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

	/** Identifiant du groupe parent. 0 si aucun parent */
	protected int parentId;

	/** Est-ce que le groupe peut avoir un rattachement */
	protected boolean rattachementAutorise;
	
	/** Le groupe contient des calendriers de cours */
	protected boolean estCours;
	
	/** Le groupe est un groupe unique d'un calendrier (il n'a pas de propriétaire dans ce cas, à part celui du calendrier) */
	protected boolean estCalendrierUnique;
	
	/**
	 * Constructeur utilisant les informations indispensables
 	 * @param nom
	 * @param rattachementAutorise
	 * @param estCours
	 * @param estCalendrierUnique
	 */
	public Groupe(String nom, List<Integer> idProprietaires, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique) {
		this.nom = nom;
		this.idProprietaires = idProprietaires;
		this.rattachementAutorise = rattachementAutorise;
		this.estCours = estCours;
		this.estCalendrierUnique = estCalendrierUnique;
	}

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
	 * Récupération du parent du groupe
	 * @return parentId ID du parent, 0 si aucun parent
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
	 * @param parentId ID du parent, 0 si aucun
	 */
	public void setParentId(Integer parentId) {
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

	public boolean estCours() {
		return estCours;
	}

	public void setEstCours(boolean estCours) {
		this.estCours = estCours;
	}

	public boolean estCalendrierUnique() {
		return estCalendrierUnique;
	}

	public void setEstCalendrierUnique(boolean estCalendrierUnique) {
		this.estCalendrierUnique = estCalendrierUnique;
	}

}
