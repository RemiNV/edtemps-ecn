package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

public class AbonnementsServlet extends QueryWithIntervalServlet {
	
	/**
	 * Traite une requête d'obtention des évènements d'abonnement (à une période définie)
	 */
	private JsonValue getEvenementsAbonnements(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		ArrayList<EvenementIdentifie> abonnementsEvenements = evenementGestion.listerEvenementsUtilisateur(userId, dateDebut, dateFin, true, false);
		
		return JSONUtils.getJsonArray(abonnementsEvenements);
	}
	
	/**
	 * Traite une requête d'abonnements demandant toutes les informations : 
	 * non seulement les évènements, mais aussi les calendriers et groupes
	 */
	private JsonValue getResumeAbonnements(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		EvenementGestion evenementGestion = new EvenementGestion(bdd);

		try {
			bdd.startTransaction();
			
			GroupeGestion.makeTempTableListeGroupesAbonnement(bdd, userId); // Création de la table temporaire d'abonnements pour cette transaction
			
			ArrayList<GroupeIdentifie> abonnementsGroupes = groupeGestion.listerGroupesAbonnement(userId, false, true);
			ArrayList<CalendrierIdentifie> abonnementsCalendriers = calendrierGestion.listerCalendriersAbonnementsUtilisateur(userId, false, true);
			ArrayList<EvenementIdentifie> abonnementsEvenements = evenementGestion.listerEvenementsUtilisateur(userId, dateDebut, dateFin, false, true);
			
			bdd.commit(); // Suppression de la table temporaire
			
			// Création de la réponse
			return Json.createObjectBuilder()
					.add("evenements", JSONUtils.getJsonArray(abonnementsEvenements))
					.add("calendriers", JSONUtils.getJsonArray(abonnementsCalendriers))
					.add("groupes", JSONUtils.getJsonArray(abonnementsGroupes))
					.build();
			
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		String pathInfo = req.getPathInfo();
		
		JsonValue res;
		
		if(pathInfo == null) { // Page /abonnements/
			res = getResumeAbonnements(userId, bdd, dateDebut, dateFin, req);
		}
		else if(pathInfo.equals("/evenements")) { // Récupération uniquement des évènements, page /abonnements/evenements
			res = getEvenementsAbonnements(userId, bdd, dateDebut, dateFin, req);
		}
		else { // Autre page
			res = null;
		}
		
		bdd.close();
		
		return res;
	}
}
