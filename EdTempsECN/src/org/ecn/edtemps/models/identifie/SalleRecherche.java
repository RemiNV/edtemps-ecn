package org.ecn.edtemps.models.identifie;

import java.util.ArrayList;

import javax.json.JsonObjectBuilder;

import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Materiel;

/**
 * Objet renvoyé par les recherches de salle pour indiquer les évènements déjà prévus
 * dans la salle. Utile pour les recherches de salle par les enseignants.
 * 
 * @author Remi
 *
 */
public class SalleRecherche extends SalleIdentifie {
	
	/** Liste des événements qui sont déjà prévus dans la salle */
	protected ArrayList<EvenementIdentifie> evenementsEnCours;

	
	/**
	 * Constructeur
	 * @param id Identifiant de la salle
	 * @param batiment Bâtiment de localisation
	 * @param nom Nom de la salle
	 * @param capacite Capacité en nombre de places
	 * @param niveau Niveau de la salle dans le bâtiment
	 * @param numero Numéro dans l'étage
	 * @param materiels Liste des matéries contenus
	 * @param evenementsEnCours Liste des événements déjà prévus dans la salle
	 */
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

	@Override
	protected JsonObjectBuilder makeJsonObjectBuilder() {
		JsonObjectBuilder builder = super.makeJsonObjectBuilder();
		
		if (this.evenementsEnCours != null) {
			builder.add("evenementsEnCours", JSONUtils.getJsonArray(this.evenementsEnCours));
		} else {
			builder.addNull("evenementsEnCours");
		}
		
		return builder;
	}
}
