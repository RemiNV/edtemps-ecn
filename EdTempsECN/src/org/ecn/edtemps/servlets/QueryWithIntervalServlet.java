package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.util.Date;

import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;

/**
 * Servlet générique de récupération d'évènements
 * Demande la connexion de l'utilisateur et la présence des timestamps dateDebut et dateFin en paramètres
 * @author Remi
 *
 */
public abstract class QueryWithIntervalServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 5599328695024804780L;
	private static Logger logger = LogManager.getLogger(RequiresConnectionServlet.class.getName());
	
	@Override
	protected final void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Récupération des paramètres
		String strTimestampDebut = req.getParameter("debut");
		String strTimestampFin = req.getParameter("fin");
		
		if(StringUtils.isBlank(strTimestampDebut) || StringUtils.isBlank(strTimestampFin)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètres début et/ou fin absent(s)", null));
			logger.info("Requête effectuée avec des paramètres début et/ou fin absent(s)");
			return;
		}
		
		
		Date dateDebut = null;
		Date dateFin = null;
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
		
		// Récupération des éléments à renvoyer
		JsonValue data = null;
		try {
			data = doQuery(userId, bdd, dateDebut, dateFin, req);
		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors d'une requête nécessitant un intervalle", e);
			return;
		}
		
		if(data == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		else {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
		}
		
	}
	
	/**
	 * Récupère les valeurs qui doivent être renvoyées par la requête
	 * @param userId ID de l'utilisateur connecté et effectuant la requête
	 * @param bdd Base de données à utiliser <b>et fermer dans cette méthode</b> une fois le traitement terminé
	 * @param dateDebut Date de début de l'intervalle pour cette requête
	 * @param dateFin Date de fin de l'intervalle pour cette requête
	 * @param req Requête effectuée
	 * @return Données à renvoyer pour cette requête, ou null si une erreur 404 doit être renvoyée
	 * @throws EdtempsException
	 */
	protected abstract JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException;
	
}
