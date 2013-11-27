
package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.JsonObjectBuilder;

import org.ecn.edtemps.json.JSONUtils;

/**
 * Calendrier avec des champs en plus qui sont utiles uniquement dans certains cas.
 * Par exemple le champ estCours qui permet de savoir si le calendrier est attaché à au moins un groupe de participants qui est un cours
 * 
 * @author Maxime TERRADE
 */
public class CalendrierComplet extends CalendrierIdentifie {

	/** Booléen pour savoir si le calendrier est rattaché à un groupe qui est un groupe de cours */
	protected boolean estCours;
	
	/** Liste contenant les id des groupes auxquels le calendrier est rattaché (hormis le groupe unique) */
	protected List<Integer> idGroupesParents;

	/** Liste contenant les id des groupes auxquels le calendrier est en attente de rattachement (hormis le groupe unique) */
	protected List<Integer> idGroupesParentsTmp;


	/**
	 * Constructeur
	 * 
	 * @param calendrier
	 * 			Calendrier identifié
	 * @param estCours
	 * 			VRAI si le calendrier est rattaché à un groupe qui est un cours
	 * @param idGroupesParticipants
	 * 			liste des id des groupes auxquels est rattaché le calendrier (hormis le groupe unique)
	 */
	public CalendrierComplet(CalendrierIdentifie calendrier, boolean estCours, List<Integer> idGroupesParents) {
		super(calendrier.getNom(), calendrier.getType(), calendrier.getMatiere(), calendrier.getIdProprietaires(), calendrier.getId());
		this.estCours = estCours;
		this.idGroupesParents = idGroupesParents;
	}
	
	@Override
	public JsonObjectBuilder makeObjectBuilder() {
		JsonObjectBuilder builder = super.makeObjectBuilder();
		
		return builder.add("estCours", this.estCours)
			.add("groupesParents", JSONUtils.getJsonIntArray(this.idGroupesParents));
	}

	public List<Integer> getIdGroupesParentsTmp() {
		return idGroupesParentsTmp;
	}

	public void setIdGroupesParentsTmp(List<Integer> idGroupesParentsTmp) {
		this.idGroupesParentsTmp = idGroupesParentsTmp;
	}

}
