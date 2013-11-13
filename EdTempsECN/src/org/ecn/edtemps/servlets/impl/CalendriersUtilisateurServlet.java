package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de récupération des calendriers dont l'utilisateur est propriétaire
 * 
 * @author Remi
 */
public class CalendriersUtilisateurServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 775452333566274544L;
	private static Logger logger = LogManager.getLogger(CalendriersUtilisateurServlet.class.getName());
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération des calendriers dont l'utilisateur est propriétaire
			List<CalendrierComplet> calendriers = calendrierGestion.listerCalendriersUtilisateur(userId);
			
			// Création de la réponse
			data = JSONUtils.getJsonArray(calendriers);
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Calendriers de l'utilisateur récupérés", data));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des calendriers de l'utilisateur d'ID " + userId, e);
		}

		bdd.close();
	}
}
