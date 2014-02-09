package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
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
import org.ecn.edtemps.managers.PeriodeBloqueeGestion;
import org.ecn.edtemps.models.identifie.PeriodeBloqueeIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des périodes bloquées
 * 
 * @author Joffrey
 */
public class PeriodesBloqueesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -7109999235327735066L;
	private static Logger logger = LogManager.getLogger(PeriodesBloqueesServlet.class.getName());
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getperiodesbloquees":
				doGetPeriodesBloquees(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des périodes bloquées ; requête " + pathInfo, e);
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
			logger.error("Erreur lors de l'ajout/modification/suppression d'une période bloquée ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	
	/**
	 * Récupérer toutes les périodes bloquées sur une période donnée
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doGetPeriodesBloquees(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");
		Boolean vacances = req.getParameter("vacances")=="" ? null : Boolean.valueOf(req.getParameter("vacances"));

		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
		
		// Exécute la requête de récupération avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		List<PeriodeBloqueeIdentifie> resultat = gestionnaire.getPeriodesBloquees(debut, fin, vacances);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listePeriodesBloquees", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
	}


	/**
	 * Supprimer une période bloquée
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
		String strId = req.getParameter("idPeriodeBloquee");
		if(StringUtils.isBlank(strId)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idPeriodeBloquee non fourni pour une suppression de période bloquée");
		}
		int id = Integer.parseInt(strId);

		// Exécute la requête de suppression avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.supprimerPeriodeBloquee(id, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée supprimée", null));
	}
	

	/**
	 * Ajouter une période bloquée
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
		Date dateDebut = this.getDateInRequest(req, "dateDebut");
		Date dateFin = this.getDateInRequest(req, "dateFin");
		String strVacances = req.getParameter("vacances");
		if(StringUtils.isBlank(strVacances)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre vacances non fourni pour un ajout de période bloquée");
		}
		boolean vacances = Boolean.valueOf(strVacances);

		ArrayList<Integer> listeIdGroupe = new ArrayList<Integer>();
		
		// Quelques vérifications sur les dates
		if (dateDebut==null || dateFin==null || dateDebut.after(dateFin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
		
		// Exécute la requête d'ajout avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.sauverPeriodeBloquee(libelle, dateDebut, dateFin, vacances, listeIdGroupe, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée ajoutée", null));

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
		Date dateDebut = this.getDateInRequest(req, "dateDebut");
		Date dateFin = this.getDateInRequest(req, "dateFin");
		String strVacances = req.getParameter("vacances");
		if(StringUtils.isBlank(strVacances)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre vacances non fourni pour un ajout de période bloquée");
		}
		boolean vacances = Boolean.valueOf(strVacances);
		String strId = req.getParameter("idPeriodeBloquee");
		if(strId == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idPeriodeBloquee non fourni pour une modification de période bloquée");
		}
		int id = Integer.parseInt(strId);
		
		ArrayList<Integer> listeIdGroupe = new ArrayList<Integer>();

		// Quelques vérifications sur les dates
		if (dateDebut==null || dateFin==null || dateDebut.after(dateFin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
		
		// Exécute la requête de modification avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.modifierPeriodeBloquee(id, libelle, dateDebut, dateFin, vacances, listeIdGroupe, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée modifiée", null));

	}
}
