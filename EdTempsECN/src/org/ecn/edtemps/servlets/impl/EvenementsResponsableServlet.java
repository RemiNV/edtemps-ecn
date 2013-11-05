package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de récupération des évènements dont l'utilisateur est responsable
 * @author Remi
 *
 */
public class EvenementsResponsableServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(EvenementsResponsableServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		JsonValue data;
		
		String strTimestampDebut = req.getParameter("debut");
		String strTimestampFin = req.getParameter("fin");
		
		if(StringUtils.isBlank(strTimestampDebut) || StringUtils.isBlank(strTimestampFin)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètres début et/ou fin absent(s)", null));
			logger.info("Requête effectuée avec des paramètres début et/ou fin absent(s)");
			return;
		}
		
		
		Date dateDebut;
		Date dateFin;
		try {
			long timestampDebut = Long.parseLong(strTimestampDebut);
			long timestampFin = Long.parseLong(strTimestampFin);
			
			dateDebut = new Date(timestampDebut);
			dateFin = new Date(timestampFin);
		}
		catch(NumberFormatException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format des paramètres début et/ou fin incorrect", null));
			logger.info("Requête effectuée avec des paramètres début et/ou fin non numériques.");
			return;
		}
		
		try {
			// Récupération des calendriers dont l'utilisateur est propriétaire
			List<EvenementIdentifie> evenements = evenementGestion.listerEvenementsResponsable(userId, dateDebut, dateFin, true);
			
			// Création de la réponse
			data = JSONUtils.getJsonArray(evenements);
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Calendriers de l'utilisateur récupérés", data));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des évènements de l'utilisateur d'ID " + userId, e);
		}

		bdd.close();
	}
}
