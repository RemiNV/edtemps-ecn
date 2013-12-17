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
public class GroupeIdentifieInflater extends AbsGroupeInflater<GroupeIdentifie> {

	@Override
	protected GroupeIdentifie inflate(int id, String nom, int idParent, int idParentTmp, boolean rattachementAutorise,
			boolean estCours, boolean estCalendrierUnique, int idCreateur, ResultSet reponse, BddGestion bdd)
					throws DatabaseException, SQLException {
		
		// Récupérer la liste des identifiants des propriétaires */
		ArrayList<Integer> idProprietaires = bdd.recupererIds(bdd.getConnection().prepareStatement("SELECT * FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+id), 
				"utilisateur_id");
		
		GroupeIdentifie groupeRecupere = new GroupeIdentifie(id, nom, idProprietaires, rattachementAutorise, estCours, estCalendrierUnique, idCreateur);
		groupeRecupere.setParentId(idParent); // Eventuellement 0
		groupeRecupere.setParentIdTmp(idParentTmp);

		// Récupérer la liste des identifiants des calendriers */
		ArrayList<Integer> idCalendriers = bdd.recupererIds(bdd.getConnection().prepareStatement("SELECT cal_id FROM edt.calendrierappartientgroupe WHERE groupeparticipant_id="+id), "cal_id");
		groupeRecupere.setIdCalendriers(idCalendriers);
		
		return groupeRecupere;
		
	}


}
