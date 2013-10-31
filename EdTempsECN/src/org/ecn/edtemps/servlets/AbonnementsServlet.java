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

public class AbonnementsServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(AbonnementsServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Récupération des paramètres
		String strTimestampDebut = req.getParameter("debut");
		String strTimestampFin = req.getParameter("fin");
		
		if(StringUtils.isBlank(strTimestampDebut) || StringUtils.isBlank(strTimestampFin)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètres début et/ou fin absent(s)", null));
			logger.info("Requête effectuée avec des paramètres début et/ou fin absent(s)");
			return;
		}
		
		
		Date dateDebut;
		Date dateFin;
		try {
			long timestampDebut = Long.parseLong(strTimestampDebut);
			long timestampFin = Long.parseLong(strTimestampFin);
			
			dateDebut = new Date(timestampDebut);
			dateFin = new Date(timestampFin);
		}
		catch(NumberFormatException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format des paramètres début et/ou fin incorrect", null));
			logger.info("Requête effectuée avec des paramètres début et/ou fin non numériques.");
			return;
		}
		

		String pathInfo = req.getPathInfo();
		
		if(pathInfo == null) { // Page /abonnements/
			doGetResumeAbonnements(userId, bdd, dateDebut, dateFin, req, resp);
		}
		else if(pathInfo.equals("/evenements")) { // Récupération uniquement des évènements, page /abonnements/evenements
			doGetEvenementsAbonnements(userId, bdd, dateDebut, dateFin, req, resp);
		}
		else { // Autre page
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		bdd.close();
	}
	
	/**
	 * Traite une requête d'obtention des évènements d'abonnement (à une période définie)
	 */
	private void doGetEvenementsAbonnements(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		try {
			ArrayList<EvenementIdentifie> abonnementsEvenements = evenementGestion.listerEvenementsUtilisateur(userId, dateDebut, dateFin, true, false);
			
			JsonArray res = JSONUtils.getJsonArray(abonnementsEvenements);
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Abonnements aux évènements récupérés", res));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.info("Erreur de base de données lors du listing des évènements des abonnements", e);
		}
		
	}
	
	/**
	 * Traite une requête d'abonnements demandant toutes les informations : 
	 * non seulement les évènements, mais aussi les calendriers et groupes
	 */
	private void doGetResumeAbonnements(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		String message;
		ResultCode resultCode;
		JsonValue data;

		try {
			try {
				bdd.startTransaction();
				
				GroupeGestion.makeTempTableListeGroupesAbonnement(bdd, userId); // Création de la table temporaire d'abonnements pour cette transaction
				
				ArrayList<GroupeIdentifie> abonnementsGroupes = groupeGestion.listerGroupesAbonnement(userId, false, true);
				ArrayList<CalendrierIdentifie> abonnementsCalendriers = calendrierGestion.listerCalendriersAbonnementsUtilisateur(userId, false, true);
				ArrayList<EvenementIdentifie> abonnementsEvenements = evenementGestion.listerEvenementsUtilisateur(userId, dateDebut, dateFin, false, true);
				
				bdd.commit(); // Suppression de la table temporaire
				
				// Création de la réponse
				data = Json.createObjectBuilder()
						.add("evenements", JSONUtils.getJsonArray(abonnementsEvenements))
						.add("calendriers", JSONUtils.getJsonArray(abonnementsCalendriers))
						.add("groupes", JSONUtils.getJsonArray(abonnementsGroupes))
						.build();
				
				resultCode = ResultCode.SUCCESS;
				message = "Abonnements récupérés";
				
			} catch (SQLException e) {
				throw new DatabaseException(e);
			}
			
		} catch (DatabaseException e) {
			resultCode = e.getResultCode();
			message = e.getMessage();
			data = null;
			logger.error("Erreur d'accès à la base de données lors du listing du résumé des abonnements", e);
		}
		
		
		resp.getWriter().write(ResponseManager.generateResponse(resultCode, message, data));
	}
}
