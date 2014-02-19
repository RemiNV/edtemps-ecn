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
import org.ecn.edtemps.managers.CreneauGestion;
import org.ecn.edtemps.models.identifie.CreneauIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour récupérer la liste des créneaux
 * 
 * @author Joffrey Terrade
 */
public class CreneauServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 6949388831461317311L;
	private static Logger logger = LogManager.getLogger(CreneauServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		CreneauGestion gestionnaire = new CreneauGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération de la liste des créneaux
			List<CreneauIdentifie> listeCreneaux = gestionnaire.getCreneaux();

			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("listeCreneaux", JSONUtils.getJsonArray(listeCreneaux))
					.build();
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des créneaux", e);
		}

		bdd.close();
	}

}
