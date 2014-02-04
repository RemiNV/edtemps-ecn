package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.JourFerieGestion;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des jours fériés
 * 
 * @author Joffrey
 */
public class JoursFeriesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2647012858867960542L;
	private static Logger logger = LogManager.getLogger(JoursFeriesServlet.class.getName());
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getJoursFeries":
				doGetJoursFeries(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des jours fériés ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	protected void doGetJoursFeries(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");

		// Exécute la requête de récupération avec le gestionnaire
		JourFerieGestion gestionnaireJoursFeries = new JourFerieGestion(bdd);
		List<JourFerieIdentifie> resultat = gestionnaireJoursFeries.getJoursFeries(debut, fin);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listeJoursFeries", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
	}

}
