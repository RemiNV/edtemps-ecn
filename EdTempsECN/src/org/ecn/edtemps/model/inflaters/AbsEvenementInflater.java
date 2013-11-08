package org.ecn.edtemps.model.inflaters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.EvenementComplet;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

public abstract class AbsEvenementInflater<T extends EvenementIdentifie> {

	
	public T inflateEvenement(ResultSet reponse, BddGestion bdd) throws DatabaseException {
		try {
			
			// Récupération des IDs des calendriers
			PreparedStatement requetePreparee;
			
			int id = reponse.getInt("eve_id");
			String nom = reponse.getString("eve_nom");
			Date dateDebut = reponse.getTimestamp("eve_datedebut");
			Date dateFin = reponse.getTimestamp("eve_datefin");
			
			requetePreparee = bdd.getConnection().prepareStatement(
					"SELECT cal_id "
					+ "FROM edt.evenementappartient "
					+ "WHERE eve_id=" + id);
			
			ArrayList<Integer> idCalendriers = bdd.recupererIds(requetePreparee, "cal_id");
			
			// Récupération des salles
			SalleGestion salleGestion = new SalleGestion(bdd);
			ArrayList<SalleIdentifie> salles = salleGestion.getSallesEvenement(id);
			
			// Récupération des intervenants
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
			ArrayList<UtilisateurIdentifie> intervenants = utilisateurGestion.getIntervenantsEvenement(id);
			
			// Récupération des responsables
			ArrayList<UtilisateurIdentifie> responsables = utilisateurGestion.getResponsablesEvenement(id);
			
			// Matériel
			ResultSet reponseMateriel = bdd.executeRequest(
					"SELECT materiel.materiel_id AS id, materiel.materiel_nom AS nom, necessitemateriel.necessitemateriel_quantite AS quantite " +
					"FROM edt.materiel INNER JOIN edt.necessitemateriel ON necessitemateriel.materiel_id = materiel.materiel_id "
					+ "AND necessitemateriel.eve_id=" + id);
			
			ArrayList<Materiel> materiels = new ArrayList<Materiel>();
			while(reponseMateriel.next()) {
				materiels.add(new Materiel(reponseMateriel.getInt("id"), reponseMateriel.getString("nom"), reponseMateriel.getInt("quantite")));
			}
			reponseMateriel.close();
			
			return inflate(nom, dateDebut, dateFin, idCalendriers, salles, intervenants, responsables, materiels, id, reponse, bdd);
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	protected abstract T inflate(String nom, Date dateDebut, Date dateFin, ArrayList<Integer> idCalendriers, ArrayList<SalleIdentifie> salles, ArrayList<UtilisateurIdentifie> intervenants, 
			ArrayList<UtilisateurIdentifie> responsables, ArrayList<Materiel> materiels, int id, ResultSet reponse, BddGestion bdd) throws DatabaseException;

}
