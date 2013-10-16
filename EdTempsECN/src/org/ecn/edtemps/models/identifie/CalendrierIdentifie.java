package org.ecn.edtemps.models.identifie;

import java.util.List;

import org.ecn.edtemps.models.Calendrier;

/**
 * Classe modèle d'un calendrier identifié
 * 
 * @author Maxime TERRADE
 */
public class CalendrierIdentifie extends Calendrier {

	/** Identifiant du calendrier dans la base de données */
	protected int id;
	
	/** Constructeur avec tous les attributs */
	public CalendrierIdentifie(String nom, String type, String matiere, List<Integer> idProprietaires, int id) {
		super(nom, type, matiere, idProprietaires);
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
