package org.ecn.edtemps.models.identifie;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONUtils;

/**
 * Calendrier avec des champs en plus qui sont utiles uniquement dans certains cas.
 * Par exemple le champ estCours qui permet de savoir si le calendrier est attaché à au moins un groupe de participants qui est un cours
 * 
 * @author Joffrey
 */
public class CalendrierComplet extends CalendrierIdentifie {

	/** Booléen pour savoir si le calendrier est rattaché à un groupe qui est un groupe de cours */
	protected boolean estCours;
	
	/**
	 * Constructeur
	 * 
	 * @param calendrier
	 * 			Calendrier identifié
	 * @param estCours
	 * 			VRAI si le calendrier est rattaché à un groupe qui est un cours
	 */
	public CalendrierComplet(CalendrierIdentifie calendrier, boolean estCours) {
		super(calendrier.getNom(), calendrier.getType(), calendrier.getMatiere(), calendrier.getIdProprietaires(), calendrier.getId());
		this.estCours = estCours;
	}
	
	@Override
	public JsonObjectBuilder makeObjectBuilder() {
		JsonObjectBuilder builder = super.makeObjectBuilder();
		
		return builder.add("estCours", this.estCours)
			.add("proprietaires", JSONUtils.getJsonIntArray(this.idProprietaires));
	}

}
