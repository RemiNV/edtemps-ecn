package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de récupération de l'URL ICal de l'utilisateur
 * @author Remi
 *
 */
public class ICalTokenServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(ICalTokenServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		try {
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
			
			String token = utilisateurGestion.getTokenICal(userId);
			
			JsonObject res = Json.createObjectBuilder().add("token", token).build();
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "URL ICal récupérée", res));
		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors de la récupération de l'URL ICal de l'utilisateur", e);
		}
		
		bdd.close();
	}
}
