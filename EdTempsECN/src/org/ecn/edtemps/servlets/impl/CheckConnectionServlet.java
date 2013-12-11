package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de vérification de l'état de la connexion
 * 
 * @author Remi
 */
public class CheckConnectionServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 3604157101345479831L;
	private static Logger logger = LogManager.getLogger(CheckConnectionServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Si cette méthode est appelée, c'est que l'utilisateur a été identifié avec succès
		
		try {
			// Récupération des droits de l'utilisateur
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
			List<Integer> listeDroits = utilisateurGestion.getListeActionsAutorisees(userId);
			
			JsonObject data = Json.createObjectBuilder()
					.add("id", userId)
					.add("actionsAutorisees", JSONUtils.getJsonIntArray(listeDroits))
					.build();
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Identifiants valides.", data));
			logger.debug("Identifiants soumis à vérification par le client valides");
			bdd.close();
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors de la vérification de la connexion d'un utilisateur", e);
			bdd.close();
		}
	}
}
