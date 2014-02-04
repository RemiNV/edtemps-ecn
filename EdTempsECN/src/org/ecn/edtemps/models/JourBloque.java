package org.ecn.edtemps.models;

import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.ecn.edtemps.json.JSONAble;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

/**
 * Classe modèle d'un jour bloqué
 * 
 * @author Joffrey
 */
public class JourBloque implements JSONAble {

	/** Libellé */
	protected String libelle;
	
	/** Date de début */
	protected Date dateDebut;

	/** Date de fin */
	protected Date dateFin;
	
	/** Liste des groupes rattachés à ce jour bloqué */
	protected List<GroupeIdentifie> listeGroupes;
	
	/** VRAI si la période bloquée correspond à des vacances */
	protected boolean vacances;

	
	/** 
	 * Constructeur avec les paramètres obligatoires
	 * 
	 * @param libelle Libellé du jour bloqué
	 * @param dateDebut Date de début du jour bloqué
	 * @param dateFin Date de fin du jour bloqué
	 * @param listeGroupes Liste des groupes rattachés à ce jour bloqué
	 * @param vacances VRAI si la période bloquée correspond à des vacances
	 */
	public JourBloque (String libelle, Date dateDebut, Date dateFin, List<GroupeIdentifie> listeGroupes, boolean vacances) {
		this.libelle = libelle;
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.listeGroupes = listeGroupes;
		this.vacances = vacances;
	}
	

	@Override
	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
				.add("libelle", this.libelle)
				.add("vacances", this.vacances)
				.add("dateDebut", this.dateDebut.getTime())
				.add("dateFin", this.dateFin.getTime())
				.add("listeGroupes", JSONUtils.getJsonArray(this.listeGroupes));
		return builder.build();
	}


	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public List<GroupeIdentifie> getListeGroupes() {
		return listeGroupes;
	}

	public void setListeGroupes(List<GroupeIdentifie> listeGroupes) {
		this.listeGroupes = listeGroupes;
	}

	public boolean getVacances() {
		return vacances;
	}

	public void setVacances(boolean vacances) {
		this.vacances = vacances;
	}

}
