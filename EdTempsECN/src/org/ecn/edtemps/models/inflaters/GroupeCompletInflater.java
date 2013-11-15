package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

/**
 * Classe de création d'un GroupeIdentifie à partir de lignes de base de données.
 * 
 * @author Joffrey
 */
public class GroupeCompletInflater extends AbsGroupeInflater<GroupeIdentifie> {

	@Override
	protected GroupeIdentifie inflate(int id, String nom, int idParent, int idParentTmp, boolean rattachementAutorise,
			boolean estCours, boolean estCalendrierUnique, ResultSet reponse, BddGestion bdd)
					throws DatabaseException, SQLException {
		
		// Récupérer la liste des identifiants des propriétaires */
		ResultSet requeteProprietaires = bdd.executeRequest("SELECT * FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+id);
		
		ArrayList<Integer> idProprietaires = new ArrayList<Integer>();
		while (requeteProprietaires.next()) {
			idProprietaires.add(requeteProprietaires.getInt("utilisateur_id"));
		}
		requeteProprietaires.close();
		
		GroupeIdentifie groupeRecupere = new GroupeIdentifie(id, nom, idProprietaires, rattachementAutorise, estCours, estCalendrierUnique);
		groupeRecupere.setParentId(idParent); // Eventuellement 0
		groupeRecupere.setParentIdTmp(idParentTmp);

		// Récupérer la liste des identifiants des calendriers */
		ResultSet requeteCalendriers = bdd.executeRequest("SELECT * FROM edt.calendrierappartientgroupe WHERE groupeparticipant_id="+id);
		
		ArrayList<Integer> idCalendriers = new ArrayList<Integer>();
		while (requeteCalendriers.next()) {
			idCalendriers.add(requeteCalendriers.getInt("cal_id"));
		}
		requeteCalendriers.close();
		groupeRecupere.setIdCalendriers(idCalendriers);
		
		return groupeRecupere;
		
	}


}
