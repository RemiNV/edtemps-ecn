package org.ecn.edtemps.models.identifie;

import java.util.Date;
import java.util.List;

import org.ecn.edtemps.models.Evenement;

/**
 * Classe modèle d'un événement identifié = défini dans la base de données
 * 
 * @author Maxime TERRADE
 */
public class EvenementIdentifie extends Evenement {

	/** Identifiant de l'evenement dans la base de données */
	protected int id;
	
	
	/** Constructeur par défaut */
	public EvenementIdentifie() {
		super();
	}
	
	/** Constructeur avec tous les attributs */
	public EvenementIdentifie(String nom, Date dateDebut, Date dateFin,
			List<Integer> idCalendriers, SalleIdentifie salle,
			List<String> intervenants, int id) {
		super(nom, dateDebut, dateFin, idCalendriers, salle, intervenants);
		this.id=id;
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
