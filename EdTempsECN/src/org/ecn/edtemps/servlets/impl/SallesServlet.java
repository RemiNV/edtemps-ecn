package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

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
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de listing de toutes les salles
 * @author Remi
 *
 */
public class SallesServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(SallesServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		SalleGestion salleGestion = new SalleGestion(bdd);
		
		try {
			List<SalleIdentifie> salles = salleGestion.listerToutesSalles();
			bdd.close();
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Liste des salles récupérée", JSONUtils.getJsonArray(salles)));
		}
		catch(DatabaseException e) {
			logger.error("Erreur lors du listing des salles", e);
			bdd.close();
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
		}
	}
}
