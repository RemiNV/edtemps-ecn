package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.models.Statistiques;
import org.ecn.edtemps.models.Statistiques.StatistiquesGroupe;

public class StatistiquesGestion {

	protected BddGestion _bdd;
	
	public StatistiquesGestion(BddGestion bdd) {
		_bdd = bdd;
	}
	
	/**
	 * Récupération d'une statistiques d'une matière pour les calendriers d'un utilisateur
	 * @param idUtilisateur Utilisateur dont les calendriers sont à examiner
	 * @param dateDebut Date de début de la fenêtre de recherche
	 * @param dateFin Date de fin de la fenêtre de recherche
	 * @param matiere Matière dont les statistiques sont à calculer
	 * @return Statistiques calculées
	 * @throws DatabaseException 
	 */
	public Statistiques getStatistiques(int idUtilisateur, Date dateDebut, Date dateFin, String matiere) throws DatabaseException {
		Statistiques res = new Statistiques(matiere);
		
		// On convertit l'intervalle datefin-datedebut en secondes
		String request = "SELECT extract('epoch' from SUM(evenement.eve_datefin - evenement.eve_datedebut)) AS intervalle_cours, typecalendrier.typecal_id, " +
				"typecalendrier.typecal_libelle, cap.groupeparticipant_id, MAX(heurescours.heurescours_quantite) AS heurescours_quantite from edt.calendrier " +
				"INNER JOIN edt.typecalendrier ON calendrier.typecal_id=typecalendrier.typecal_id " +
				"INNER JOIN edt.calendrierappartientgroupe cap ON calendrier.cal_id=cap.cal_id " +
				"INNER JOIN edt.groupeparticipant ON groupeparticipant.groupeparticipant_id=cap.groupeparticipant_id AND NOT groupeparticipant.groupeparticipant_estcalendrierunique " +
				"INNER JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id AND matiere.matiere_nom = ? " +
				"INNER JOIN edt.proprietairecalendrier ON proprietairecalendrier.cal_id=calendrier.cal_id AND proprietairecalendrier.utilisateur_id=" + idUtilisateur +
				" LEFT JOIN edt.heurescours ON heurescours.matiere_id=calendrier.matiere_id AND heurescours.typecal_id=typecalendrier.typecal_id " +
				"LEFT JOIN edt.evenementappartient ON evenementappartient.cal_id=calendrier.cal_id " +
				"LEFT JOIN edt.evenement ON evenement.eve_id=evenementappartient.eve_id AND evenement.eve_datefin > ? AND evenement.eve_datedebut <= ? " +
				"GROUP BY typecalendrier.typecal_id, typecalendrier.typecal_libelle, cap.groupeparticipant_id " +
				"ORDER BY typecalendrier.typecal_id";
		
		try {
			PreparedStatement statement = _bdd.getConnection().prepareStatement(request);
			
			statement.setString(1, matiere);
			statement.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
			statement.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
			
			ResultSet resultSet = statement.executeQuery();
			
			int typeCoursCourant = -1;
			String strTypeCoursCourant = null;
			HashMap<Integer, StatistiquesGroupe> nextMap = new HashMap<Integer, StatistiquesGroupe>();
			while(resultSet.next()) {
				
				int typeCours = resultSet.getInt("typecal_id");
				if(typeCoursCourant == -1) { // Première occurrence
					strTypeCoursCourant = resultSet.getString("typecal_libelle");
					typeCoursCourant = typeCours;
				}

				// Les typecours identiques doivent être à la suite : nouveau type de cours
				if(typeCours != typeCoursCourant) {
					res.setStatistiquesTypeCours(strTypeCoursCourant, nextMap);
					
					typeCoursCourant = typeCours;
					strTypeCoursCourant = resultSet.getString("typecal_libelle");
					nextMap = new HashMap<Integer, StatistiquesGroupe>();
				}
				
				nextMap.put(resultSet.getInt("groupeparticipant_id"), new StatistiquesGroupe(resultSet.getInt("intervalle_cours"), resultSet.getInt("heurescours_quantite") * 3600));
			}
			
			resultSet.close();
			
			if(strTypeCoursCourant != null) { // Ajout du dernier type de cours
				res.setStatistiquesTypeCours(strTypeCoursCourant, nextMap);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
		
		
		return res;
	}
}
