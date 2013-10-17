package org.ecn.edtemps.models.identifie;

import org.ecn.edtemps.models.Utilisateur;

public class UtilisateurIdentifie extends Utilisateur {

	public UtilisateurIdentifie(int id, String nom, String prenom, String email) {
		super(nom, prenom, email);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	protected int id;
}
