package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.TestRepetitionEvenement;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class RepeterEvenementServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(RepeterEvenementServlet.class.getName());

	private static final long serialVersionUID = -3058678054940040170L;

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Requête GET /previsualiser supportée uniquement
		if(!req.getPathInfo().equals("/previsualiser")) {
			super.doGetAfterLogin(userId, bdd, req, resp);
			return;
		}
		
		try {
			// Récupération des paramètres
			int idEvenement = getIntParam(req, "idEvenement");
			int nbRepetitions = getIntParam(req, "nbRepetitions");
			int periode = getIntParam(req, "periode");
			
			EvenementGestion evenementGestion = new EvenementGestion(bdd);
			
			ArrayList<TestRepetitionEvenement> res = evenementGestion.testRepetitionEvenement(idEvenement, nbRepetitions, periode, true);
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Répétition calculée", 
					JSONUtils.getJsonArray(res)));
		}
		catch(EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors de la prévisualisation de répétition d'événement", e);
		}
		
		
		bdd.close();
	}
	
	protected static int getIntParam(HttpServletRequest req, String nom) throws EdtempsException {
		String strParam = req.getParameter(nom);
		try {
			return Integer.parseInt(strParam);
		}
		catch(NumberFormatException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, e);
		}
	}
}
