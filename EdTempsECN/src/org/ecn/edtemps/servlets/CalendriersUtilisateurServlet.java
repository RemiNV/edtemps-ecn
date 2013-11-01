package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.util.List;

import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/**
 * Servlet de récupération des calendriers dont l'utilisateur est propriétaire
 * @author Remi
 *
 */
public class CalendriersUtilisateurServlet extends RequiresConnectionServlet {

	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération des calendriers dont l'utilisateur est propriétaire
			List<CalendrierIdentifie> calendriers = calendrierGestion.listerCalendriersUtilisateur(userId);
			
			// Création de la réponse
			data = JSONUtils.getJsonArray(calendriers);
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Calendriers de l'utilisateur récupérés", data));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des calendriers de l'utilisateur d'ID " + userId, e);
		}

		bdd.close();
	}
}
