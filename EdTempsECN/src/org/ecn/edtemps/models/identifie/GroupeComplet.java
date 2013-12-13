package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.JsonObjectBuilder;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;

/**
 * Classe modèle d'un groupe de participants avec tous les objets
 * 
 * @author Joffrey
 */
public class GroupeComplet extends GroupeIdentifie implements JSONAble {

	/** Liste des calendriers */
	protected List<CalendrierIdentifie> calendriers;

	/** Liste des propriétaires */
	protected List<UtilisateurIdentifie> proprietaires;

	/** Groupe parent */
	protected GroupeIdentifie parent;

	
	/**
	 * Constructeur complet
	 * @param id Identifiant du groupe
	 * @param nom Nom du groupe
	 * @param rattachementAutorise VRAI si le groupe accepte le rattachement
	 * @param estCours VRAI s'il est un groupe de cours
	 * @param estCalendrierUnique VRAI s'il est le groupe unique lié à un calendrier
	 * @param parentIdTmp Identifiant du groupe parent (en attendant la validation du rattachement) 
	 * @param parentId Identifiant du groupe parent
	 * @param calendriers Liste des calendriers
	 * @param proprietaires Liste des propriétaires
	 * @param parent Groupe parent (le temporaire ou le réel si la validation a été faite)
	 * @param idCreateur Identifiant du créateur du groupe de participants
	 */
	public GroupeComplet(int id, String nom, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique, int parentIdTmp, int parentId, List<CalendrierIdentifie> calendriers, List<UtilisateurIdentifie> proprietaires, GroupeIdentifie parent, int idCreateur) {
		super(id, nom, null, rattachementAutorise, estCours, estCalendrierUnique, idCreateur);
		this.setParentIdTmp(parentIdTmp);
		this.setParentId(parentId);
		this.setParent(parent);
		this.setProprietaires(proprietaires);
		this.setCalendriers(calendriers);
	}
	
	public List<CalendrierIdentifie> getCalendriers() {
		return calendriers;
	}

	public void setCalendriers(List<CalendrierIdentifie> calendriers) {
		this.calendriers = calendriers;
	}

	public List<UtilisateurIdentifie> getProprietaires() {
		return proprietaires;
	}

	public void setProprietaires(List<UtilisateurIdentifie> proprietaires) {
		this.proprietaires = proprietaires;
	}

	public GroupeIdentifie getParent() {
		return parent;
	}

	public void setParent(GroupeIdentifie parent) {
		this.parent = parent;
	}
	
	@Override
	protected JsonObjectBuilder makeJsonObjectBuilder() {
		JsonObjectBuilder builder = super.makeJsonObjectBuilder();
		
		if (parent!=null) {
			builder.add("parent", parent.toJson());
		}
		
		if(calendriers != null) {
			builder.add("proprietaires", JSONUtils.getJsonArray(proprietaires));
		}
		else {
			builder.addNull("proprietaires");
		}
		
		if(proprietaires != null) {
			builder.add("calendriers", JSONUtils.getJsonArray(calendriers));
		}
		else {
			builder.addNull("calendriers");
		}
		
		return builder;
	}
}
