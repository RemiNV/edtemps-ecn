package org.ecn.edtemps.models.identifie;

import java.util.Date;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.models.JourFerie;

/**
 * Classe modèle identifiée d'un jour férié
 * 
 * @author Joffrey
 */
public class JourFerieIdentifie extends JourFerie implements JSONAble {

	/** Identifiant du jour férié */
	protected int id;

	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param id Identifiant du jour férié
	 * @param libelle Libellé du jour férié
	 * @param date Date du jour férié
	 */
	public JourFerieIdentifie(int id, String libelle, Date date) {
		super(libelle, date);
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
