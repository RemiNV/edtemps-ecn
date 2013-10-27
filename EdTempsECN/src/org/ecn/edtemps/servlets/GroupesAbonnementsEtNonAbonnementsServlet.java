package org.ecn.edtemps.servlets;

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
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

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
			ArrayList<GroupeIdentifie> abonnementsGroupes = groupeGestion.listerGroupesAbonnement(userId, true, false);
			ArrayList<GroupeIdentifie> nonAbonnementsGroupes = new ArrayList<GroupeIdentifie>();
			
			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("groupesAbonnements", JSONUtils.getJsonArray(abonnementsGroupes))
					.add("groupesNonAbonnements", JSONUtils.getJsonArray(nonAbonnementsGroupes))
					.build();
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Abonnements aux évènements récupérés", data));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des groupes", e);
		}

		bdd.close();
	}

}
