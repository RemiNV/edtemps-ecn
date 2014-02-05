package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.JourFerieGestion;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des jours fériés
 * 
 * @author Joffrey
 */
public class JoursFeriesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2647012858867960542L;
	private static Logger logger = LogManager.getLogger(JoursFeriesServlet.class.getName());
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getJoursFeries":
				doGetJoursFeries(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des jours fériés ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	

	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/ajouter":
				doAjouter(userId, bdd, req, resp);
				break;
			case "/modifier":
				doModifier(userId, bdd, req, resp);
				break;
			case "/supprimer":
				doSupprimer(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression d'un jour férié ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	
	/**
	 * Récupération de tous les jours fériés sur une période donnée
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doGetJoursFeries(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");

		// Exécute la requête de récupération avec le gestionnaire
		JourFerieGestion gestionnaireJoursFeries = new JourFerieGestion(bdd);
		List<JourFerieIdentifie> resultat = gestionnaireJoursFeries.getJoursFeries(debut, fin);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listeJoursFeries", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Liste des jours fériés récupérés", data));
	}
	
	
	/**
	 * Supprimer un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doSupprimer(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String strIdJourFerie = req.getParameter("idJourFerie");
		if(StringUtils.isBlank(strIdJourFerie)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idJourFerie non fourni pour une suppression de jour férié");
		}
		int idJourFerie = Integer.parseInt(strIdJourFerie);

		// Exécute la requête de suppression avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.supprimerJourFerie(idJourFerie, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié supprimé", null));
	}
	
	
	/**
	 * Ajouter un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doAjouter(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String libelle = req.getParameter("libelle");
		Date date = this.getDateInRequest(req, "date");
		
		// Exécute la requête d'ajout avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.sauverJourFerie(libelle, date, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié ajouté", null));

	}


	/**
	 * Modifier un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doModifier(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String libelle = req.getParameter("libelle");
		Date date = this.getDateInRequest(req, "date");
		String strIdJourFerie = req.getParameter("idJourFerie");
		if(strIdJourFerie == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idJourFerie non fourni pour une suppression de jour férié");
		}
		int idJourFerie = Integer.parseInt(strIdJourFerie);
		
		JourFerieIdentifie jour = new JourFerieIdentifie(idJourFerie, libelle, date);
		
		// Exécute la requête de modification avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.modifierJourFerie(jour, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié ajouté", null));

	}

}
