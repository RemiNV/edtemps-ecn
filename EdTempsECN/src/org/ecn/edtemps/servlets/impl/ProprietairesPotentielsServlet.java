package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour récupérer la liste des utilisateurs qui peuvent potentiellement être propriétaires
 * 
 * @author Joffrey Terrade
 */
public class ProprietairesPotentielsServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 5254458280034416445L;
	private static Logger logger = LogManager.getLogger(ProprietairesPotentielsServlet.class.getName());

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération de la liste des utilisateurs potentiellement proprietaires
			List<UtilisateurIdentifie> listeUtilisateurs = utilisateurGestion.getResponsablesPotentiels();

			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("listeUtilisateurs", JSONUtils.getJsonArray(listeUtilisateurs))
					.build();
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération de la liste des propriétaires potentiels", e);
		}

		bdd.close();
	}

}
