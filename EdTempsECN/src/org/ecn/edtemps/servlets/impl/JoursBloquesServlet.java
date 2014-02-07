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
import org.ecn.edtemps.managers.JourBloqueGestion;
import org.ecn.edtemps.models.identifie.JourBloqueIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des jours bloqués
 * 
 * @author Joffrey
 */
public class JoursBloquesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -7109999235327735066L;
	private static Logger logger = LogManager.getLogger(JoursBloquesServlet.class.getName());
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getJoursBloques":
				doGetJoursBloques(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des jours bloqués ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	protected void doGetJoursBloques(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");
		Boolean vacances = req.getParameter("vacances")=="" ? null : Boolean.valueOf(req.getParameter("vacances"));

		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
		
		// Exécute la requête de récupération avec le gestionnaire
		JourBloqueGestion gestionnaireJoursBloques = new JourBloqueGestion(bdd);
		List<JourBloqueIdentifie> resultat = gestionnaireJoursBloques.getJoursBloques(debut, fin, vacances);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listeJoursBloques", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
	}

}
