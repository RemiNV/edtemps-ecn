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

public class CreateICalServlet extends RequiresConnectionServlet {

	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Génération d'un nouveau token iCal pour l'utilisateur
		UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
		
		try {
			String newToken = utilisateurGestion.creerTokenIcal(userId);
			
			JsonObject res = Json.createObjectBuilder().add("token", newToken).build();
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Token iCal généré", res));
		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
		}
		
		bdd.close();
	}
}
