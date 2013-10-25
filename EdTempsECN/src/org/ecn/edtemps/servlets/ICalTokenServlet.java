package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

/**
 * Servlet de récupération de l'URL ICal de l'utilisateur
 * @author Remi
 *
 */
public class ICalTokenServlet extends RequiresConnectionServlet {

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		try {
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
			
			String token = utilisateurGestion.getTokenICal(userId);
			
			JsonObject res = Json.createObjectBuilder().add("token", token).build();
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "URL ICal récupérée", res));
		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
		}
		
		bdd.close();
	}
}
