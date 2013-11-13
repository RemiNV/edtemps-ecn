package org.ecn.edtemps.servlets.impl;

import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

/**
 * Servlet pour s'abonner à un groupe de participants
 * 
 * @author Joffrey
 */
public class AbonnementsServlet extends QueryWithIntervalServlet {
	
	private static final long serialVersionUID = -4716249286511792115L;

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
