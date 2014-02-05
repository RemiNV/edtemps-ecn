
package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.JsonObjectBuilder;

import org.ecn.edtemps.json.JSONUtils;

/**
 * Calendrier avec des champs en plus qui sont utiles uniquement dans certains cas.
 * Par exemple le champ estCours qui permet de savoir si le calendrier est attaché
 * à au moins un groupe de participants qui est un cours
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
	
	/** Liste des noms des groupes parents du calendrier (dans le même ordre que la liste des ID) */
	protected List<String> nomsGroupesParents;
	
	/** Le créateur complet : uniquement récupéré quand nécessaire : pas présent dans le constructeur*/
	protected UtilisateurIdentifie createur;


	/**
	 * Constructeur
	 * @param calendrier Calendrier identifié
	 * @param estCours VRAI si le calendrier est rattaché à un groupe qui est un cours
	 * @param idGroupesParents Liste des id des groupes auxquels est rattaché le calendrier (hormis le groupe unique)
	 * @param nomsGroupesParents Liste des noms des groupes auxquels est rattaché le calendrier (hormis le groupe unique), dans le même ordre que les ID
	 */
	public CalendrierComplet(CalendrierIdentifie calendrier, boolean estCours, List<Integer> idGroupesParents, List<String> nomsGroupesParents) {
		super(calendrier.getNom(), calendrier.getType(), calendrier.getMatiere(), calendrier.getIdProprietaires(), calendrier.getId(), calendrier.getIdCreateur());
		this.estCours = estCours;
		this.idGroupesParents = idGroupesParents;
		this.nomsGroupesParents = nomsGroupesParents;
	}
	
	@Override
	public JsonObjectBuilder makeObjectBuilder() {
		JsonObjectBuilder builder = super.makeObjectBuilder();
		
		if (this.createur!=null) {
			builder.add("createurComplet", this.createur.toJson());
		}
	
		return builder.add("estCours", this.estCours)
				.add("groupesParents", JSONUtils.getJsonIntArray(this.idGroupesParents))
				.add("groupesParentsTmp", JSONUtils.getJsonIntArray(this.idGroupesParentsTmp))
				.add("nomsGroupesParents", JSONUtils.getJsonStringArray(nomsGroupesParents));
		
	}

	public List<Integer> getIdGroupesParents() {
		return idGroupesParents;
	}
	
	public List<String> getNomsGroupesParents() {
		return nomsGroupesParents;
	}

	public void setIdGroupesParents(List<Integer> idGroupesParents) {
		this.idGroupesParents = idGroupesParents;
	}
	
	public UtilisateurIdentifie getCreateur() {
		return createur;
	}

	public void setCreateur(UtilisateurIdentifie createur) {
		this.createur = createur;
	}

	public List<Integer> getIdGroupesParentsTmp() {
		return idGroupesParentsTmp;
	}

	public void setIdGroupesParentsTmp(List<Integer> idGroupesParentsTmp) {
		this.idGroupesParentsTmp = idGroupesParentsTmp;
	}

}
