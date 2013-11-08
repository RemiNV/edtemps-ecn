package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifieAbonnement;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet renvoyant les groupes de participants auxquels l'utilisateur est abonné 
 * et auxquels il n'est pas abonné (dans deux tableaux différents)
 * @author Maxime Terrade
 *
 */
public class GroupesAbonnementsEtNonAbonnementsServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(GroupesAbonnementsEtNonAbonnementsServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
			
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération des groupes 
			ArrayList<GroupeIdentifieAbonnement> abonnementsGroupes = groupeGestion.listerGroupesAbonnementDirect(userId);
			ArrayList<GroupeIdentifieAbonnement> nonAbonnementsGroupes = groupeGestion.listerGroupesNonAbonnement(userId);
			
			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("groupesAbonnements", JSONUtils.getJsonArray(abonnementsGroupes))
					.add("groupesNonAbonnements", JSONUtils.getJsonArray(nonAbonnementsGroupes))
					.build();
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Abonnements aux groupes récupérés", data));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des groupes", e);
		}

		bdd.close();
	}

}
