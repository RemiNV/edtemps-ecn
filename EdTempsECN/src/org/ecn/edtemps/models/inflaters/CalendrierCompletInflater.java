package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/**
 * Génération d'un calendrier complet à partir d'une ligne de base de données
 * 
 * @author Joffrey
 */
public class CalendrierCompletInflater extends AbsCalendrierInflater<CalendrierComplet> {

	@Override
	protected CalendrierComplet inflate(int id, String nom, String type, String matiere, List<Integer> idProprietaires, ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException {
		
		CalendrierIdentifie calendrier = new CalendrierIdentifie(nom, type, matiere, idProprietaires, id);
		
		boolean estCours = reponse.getBoolean("estcours");
		
		// Récupération des parents du calendrier (temporaires ou validés)
		ResultSet rs_idGroupesParents = bdd.executeRequest("SELECT cag.groupeparticipant_id, cag.groupeparticipant_id_tmp"
				+ " FROM edt.calendrierappartientgroupe cag"
				+ " LEFT JOIN edt.groupeparticipant gp ON gp.groupeparticipant_id = cag.groupeparticipant_id"
				+ " WHERE cal_id=" + calendrier.getId()
				+ " AND (gp.groupeparticipant_estCalendrierUnique = FALSE OR gp.groupeparticipant_id IS NULL)"
		);
		List<Integer> idGroupesParents = new ArrayList<Integer>();
		List<Integer> idGroupesParentsTmp = new ArrayList<Integer>();
	    while (rs_idGroupesParents.next()) {
	    	idGroupesParents.add(rs_idGroupesParents.getInt("groupeparticipant_id"));
	    	idGroupesParentsTmp.add(rs_idGroupesParents.getInt("groupeparticipant_id_tmp"));
	    }

	    // Création du calendrier complet
		CalendrierComplet res = new CalendrierComplet(calendrier, estCours, idGroupesParents);
		res.setIdGroupesParentsTmp(idGroupesParentsTmp);
		
		return res;
	}
	
}
