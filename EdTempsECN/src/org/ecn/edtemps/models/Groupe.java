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
	 * Autre constructeur, uniquement avec "nom" et "parendId"
	 * @param nom
	 * @param parentId
	 */
	public Groupe(String nom, int parentId) {
		this.nom = nom;
		this.parentId = parentId;
	}

	public List<Integer> getIdCalendriers() {
		return idCalendriers;
	}

	public List<Integer> getIdProprietaires() {
		return idProprietaires;
	}

	public String getNom() {
		return nom;
	}

	public int getParentId() {
		return parentId;
	}

	public boolean getRattachementAutorise() {
		return rattachementAutorise;
	}

	public void setIdCalendriers(List<Integer> idCalendriers) {
		this.idCalendriers = idCalendriers;
	}

	public void setIdProprietaires(List<Integer> idProprietaires) {
		this.idProprietaires = idProprietaires;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

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
