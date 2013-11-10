package org.ecn.edtemps.models.identifie;

import java.util.ArrayList;

import org.ecn.edtemps.models.Materiel;

/**
 * Objet renvoyé par les recherches de salle pour indiquer les évènements déjà prévus
 * dans la salle. Utile pour les recherches de salle par les enseignants.
 * 
 * @author Remi
 *
 */
public class SalleRecherche extends SalleIdentifie {
	
	protected ArrayList<EvenementIdentifie> evenementsEnCours;

	public SalleRecherche(int id, String batiment, String nom, int capacite,
			int niveau, int numero, ArrayList<Materiel> materiels, ArrayList<EvenementIdentifie> evenementsEnCours) {
		super(id, batiment, nom, capacite, niveau, numero, materiels);

		this.evenementsEnCours = evenementsEnCours;
	}

	/**
	 * Renvoie les évènements en cours (dépend du contexte de recherche) dans une salle.
	 * <b>Peut être null si aucun évènement</b>
	 * @return Evènements en cours, ou null si aucun
	 */
	public ArrayList<EvenementIdentifie> getEvenementsEnCours() {
		return evenementsEnCours;
	}

	public void setEvenementsEnCours(ArrayList<EvenementIdentifie> evenementsEnCours) {
		this.evenementsEnCours = evenementsEnCours;
	}

}
