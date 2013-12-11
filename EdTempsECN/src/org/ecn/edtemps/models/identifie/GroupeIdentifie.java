package org.ecn.edtemps.models.identifie;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.Groupe;

/**
 * Classe modèle d'un groupe de participants identifié
 * 
 * @author Joffrey
 */
public class GroupeIdentifie extends Groupe implements JSONAble {

	/** Identifiant du groupe */
	protected int id;

	/** Identifiant du groupe parent temporaire : en attente de validation du groupe parent */
	protected int parentIdTmp;

	/** Identifiant du créateur du groupe */
	protected int idCreateur;

	
	/**
	 * Constructeur utilisant les informations indispensables
	 * @param id Identifiant du groupe
	 * @param idProprietaires Liste des identifiants des propriétaires
	 * @param nom Nom du groupe
	 * @param rattachementAutorise Vrai si le rattachement à ce groupe est autorisé
	 * @param estCours Vrai si c'est un groupe de cours
	 * @param estCalendrierUnique Vrai si c'est un groupe unique rattaché à un calendrier
	 * @param idCreateur Identifiant du créateur
	 */
	public GroupeIdentifie(int id, String nom, List<Integer> idProprietaires, boolean rattachementAutorise, boolean estCours, boolean estCalendrierUnique, int idCreateur) {
		super(nom, idProprietaires, rattachementAutorise, estCours, estCalendrierUnique);
		this.id = id;
		this.idCreateur = idCreateur;
	}
	
	
	/**
	 * Autre constructeur, uniquement avec "nom", "parendId", "id" et "estCalendrierUnique"
	 * @param id Identifiant du groupe
	 * @param nom Nom du groupe
	 * @param parentId Identifiant du groupe parent
	 * @param estCalendrierUnique Vrai si c'est un groupe unique rattaché à un calendrier
	 */
	public GroupeIdentifie(int id, String nom, int parentId, boolean estCalendrierUnique) {
		super(nom, parentId, estCalendrierUnique);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getParentIdTmp() {
		return parentIdTmp;
	}

	public void setParentIdTmp(int parentIdTmp) {
		this.parentIdTmp = parentIdTmp;
	}

	public int getIdCreateur() {
		return idCreateur;
	}

	public void setIdCreateur(int idCreateur) {
		this.idCreateur = idCreateur;
	}
	
	protected JsonObjectBuilder makeJsonObjectBuilder() {
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("id", id)
				.add("nom", nom)
				.add("parentId", parentId)
				.add("parentIdTmp", parentIdTmp)
				.add("createur", idCreateur)
				.add("rattachementAutorise", rattachementAutorise)
				.add("estCours", estCours)
				.add("estCalendrierUnique", estCalendrierUnique);
		
		
		if(idProprietaires != null) {
			builder.add("proprietaires", JSONUtils.getJsonIntArray(idProprietaires));
		}
		else {
			builder.addNull("proprietaires");
		}
		
		if(idCalendriers != null) {
			builder.add("calendriers", JSONUtils.getJsonIntArray(idCalendriers));
		}
		else {
			builder.addNull("calendriers");
		}
		
		return builder;
	}

	@Override
	public final JsonValue toJson() {
		return makeJsonObjectBuilder().build();
	}

}
