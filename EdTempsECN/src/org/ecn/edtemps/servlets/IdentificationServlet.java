package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.UtilisateurGestion;

public class IdentificationServlet extends HttpServlet {

	
	/**
	 * Connection d'un utilisateur
	 * Format JSON des réponses en cas de succès (objet data) : 
	 * (token: 'tokendeconnexion')
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Retour sous forme JSON
		resp.setContentType("application/json");
		
		// Identifiants passés en paramètre
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		
		String reponse;
		
		if(username == null || password == null) { // Paramètres incomplets
			reponse = ResponseManager.generateResponse(ResultCode.WRONG_ARGUMENTS_FOR_REQUEST, "Nom d'utilisateur ou mot de passe manquant", null);
		}
		else {
			
			ResultCode result;
			
			try {
				String token = UtilisateurGestion.seConnecter(username, password);
				result = ResultCode.SUCCESS;
				
				JsonObject data = Json.createObjectBuilder()
						.add("token", token)
						.build();
				
				reponse = ResponseManager.generateResponse(result, "", data);
			} catch (EdtempsException e) {
				reponse = ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null);
			}
		}
		
		resp.getWriter().print(reponse);
	}
}
