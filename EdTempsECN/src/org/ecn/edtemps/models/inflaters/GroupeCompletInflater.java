package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.GroupeComplet;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

/**
 * Classe de création d'un GroupeComplet à partir de lignes de base de données.
 * 
 * @author Joffrey
 */
public class GroupeCompletInflater extends AbsGroupeInflater<GroupeComplet> {

	@Override
	protected GroupeComplet inflate(int id, String nom, int idParent, int idParentTmp, boolean rattachementAutorise,
			boolean estCours, boolean estCalendrierUnique, int idCreateur, ResultSet reponse, BddGestion bdd)
					throws DatabaseException, SQLException {

		// Récupérer la liste des propriétaires
		UtilisateurGestion getionnaireUtilisateurs = new UtilisateurGestion(bdd);
		List<UtilisateurIdentifie> proprietaires = getionnaireUtilisateurs.getResponsablesGroupe(id);
		
		// Récupérer la liste des calendriers
		CalendrierGestion getionnaireCalendriers = new CalendrierGestion(bdd);
		List<CalendrierIdentifie> calendriers = getionnaireCalendriers.listerCalendriersGroupeParticipants(id);
		
		// Récupérer le groupe parent s'il y en a un
		GroupeGestion getionnaireGroupes = new GroupeGestion(bdd);
		GroupeIdentifie parent = null;
		try {
			if (idParent>0) {
				parent = getionnaireGroupes.getGroupe(idParent);
			} else if (idParentTmp>0) {
				parent = getionnaireGroupes.getGroupe(idParentTmp);
			}
		} catch (EdtempsException e) {
			throw new DatabaseException(e);
		}

		return new GroupeComplet(id, nom, rattachementAutorise, estCours, estCalendrierUnique, idParentTmp, idParent, calendriers, proprietaires, parent, idCreateur);
		
	}


}
