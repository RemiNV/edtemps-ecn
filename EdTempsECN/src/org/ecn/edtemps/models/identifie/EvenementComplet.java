package org.ecn.edtemps.models.identifie;

import java.util.Date;
import java.util.List;

import javax.json.JsonObjectBuilder;

import org.ecn.edtemps.json.JSONUtils;

/**
 * Classe pour un événement identifié muni de champs supplémentaire pour certaines utilisations
 * 
 * @author Remi
 */
public class EvenementComplet extends EvenementIdentifie {

	/** Liste des matières de l'événement */
	protected List<String> matieres;

	/** Liste des types de l'événement */
	protected List<String> types;

	/**
	 * Constructeur
	 * @param nom Nom de l'événement
	 * @param dateDebut date de début de l'événement
	 * @param dateFin date de fin de l'événement
	 * @param idCalendriers identifiants des calendriers liés
	 * @param idCreateur identifiant de l'utilisateur créateur de l'événement
	 * @param salles salles occupées par l'événement
	 * @param intervenants utilisateurs intervenants dans l'événement
	 * @param responsables utilisateurs responsables de l'événement
	 * @param id identifiant de l'événement (base de données)
	 * @param matieres liste des matières de l'événement
	 * @param types liste des types de l'événement
	 */
	public EvenementComplet(String nom, Date dateDebut, Date dateFin,
			List<Integer> idCalendriers, Integer idCreateur, List<SalleIdentifie> salles,
			List<UtilisateurIdentifie> intervenants,
			List<UtilisateurIdentifie> responsables, int id, List<String> matieres, List<String> types) {
		super(nom, dateDebut, dateFin, idCalendriers, idCreateur, salles, intervenants,
				responsables, id);
		
		this.matieres = matieres;
		this.types = types;
	}


	public List<String> getMatieres() {
		return matieres;
	}
	
	public void setMatieres(List<String> matieres) {
		this.matieres = matieres;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}
	
	@Override
	protected JsonObjectBuilder makeJsonObjectBuilder() {
		
		// Ajout d'attributs au builder pour compléter l'objet JSON
		JsonObjectBuilder builder = super.makeJsonObjectBuilder();
		
		builder.add("matieres", JSONUtils.getJsonStringArray(matieres));
		builder.add("types", JSONUtils.getJsonStringArray(types));
		
		return builder;
	}

}
