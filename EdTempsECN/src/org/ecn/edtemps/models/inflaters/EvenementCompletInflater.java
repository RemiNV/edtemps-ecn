package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.EvenementComplet;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

/**
 * Génération d'une salle complète à partir d'une ligne de base de données
 * 
 * @author Remi
 */
public class EvenementCompletInflater extends AbsEvenementInflater<EvenementComplet> {

	@Override
	public EvenementComplet inflate(String nom, Date dateDebut, Date dateFin, ArrayList<Integer> idCalendriers, 
			ArrayList<SalleIdentifie> salles, ArrayList<UtilisateurIdentifie> intervenants, 
			ArrayList<UtilisateurIdentifie> responsables, int id, ResultSet reponse, BddGestion bdd) throws DatabaseException {
		
		// Récupération des types et matières (calendriers)
		ResultSet reponseCalendriers = bdd.executeRequest("SELECT matiere.matiere_nom, typecalendrier.typecal_libelle " +
				"FROM edt.calendrier " +
				"INNER JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id " +
				"INNER JOIN edt.typecalendrier ON typecalendrier.typecal_id=calendrier.typecal_id " +
				"INNER JOIN edt.evenementappartient ON evenementappartient.cal_id=calendrier.cal_id " +
				"AND evenementappartient.eve_id=" + id);
		
		ArrayList<String> matieres = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		
		try {
			while(reponseCalendriers.next()) {
				matieres.add(reponseCalendriers.getString("matiere_nom"));
				types.add(reponseCalendriers.getString("typecal_libelle"));
			}
			
			reponseCalendriers.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		return new EvenementComplet(nom, dateDebut, dateFin, idCalendriers, salles, intervenants, responsables, 
				id, matieres, types);
	}

}
