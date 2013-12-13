package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet permettant la création, la modification et la suppression d'un calendrier
 * @author Maxime Terrade
 *
 */
public class ParametresCalendrierServlet extends RequiresConnectionServlet {
	
	private static final long serialVersionUID = 3191065778966679383L;
	private static Logger logger = LogManager.getLogger(ParametresCalendrierServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String pathInfo = req.getPathInfo();
		
		// Page /calendrier/ =>  on renvoie les calendriers dont l'utilisateur est propriétaire
		if(pathInfo == null) { 
			CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
			JsonValue data;
			try {
				// Récupération de la liste des utilisateurs potentiellement proprietaires
				List<CalendrierComplet> listeCalendriers = calendrierGestion.listerCalendriersUtilisateur(userId);
				// Création de la réponse
				data = Json.createObjectBuilder()
						.add("listeCalendriers", JSONUtils.getJsonArray(listeCalendriers))
						.build();
				// Génération de la réponse
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
			} catch (EdtempsException e) {
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur d'accès à la base de données lors de la récupération des calendriers dont l'utilisateur est propriétaire", e);
			}

			bdd.close();
		}
		// Autre page
		else { 
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}
	
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String pathInfo = req.getPathInfo();
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		
		// Page /calendrier/, accessible en GET et non en POST
		if(pathInfo == null) { 
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		// Page /calendrier/creation
		else if(pathInfo.equals("/creation")) { 
			try {
				creationOuModificationCalendrier(false, userId, calendrierGestion, req);
				// Génération réponse si aucune exception
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Création calendrier réussie", null));
				logger.debug("Création calendrier réussie");
			} catch (EdtempsException e) {
				// Génération réponse si exception
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur lors de la création du calendrier", e);
			}
		}
		// Page /calendrier/modification
		else if(pathInfo.equals("/modification")) { 
			try {
				creationOuModificationCalendrier(true, userId, calendrierGestion, req);
				// Génération réponse si aucune exception
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Modification calendrier réussie", null));
				logger.debug("Modification calendrier réussie");
			} catch (EdtempsException e) {
				// Génération réponse si exception
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur lors de la modification du calendrier", e);
			}
		}
		// Page /calendrier/suppression
		else if(pathInfo.equals("/suppression")) { 
			try {
				int idCalendrierASupprimer = Integer.parseInt(req.getParameter("id"));
				calendrierGestion.supprimerCalendrier(idCalendrierASupprimer, true);
				// Génération réponse si aucune exception
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Suppression calendrier réussie", null));
				logger.debug("Suppression calendrier réussie");
			} catch (EdtempsException e) {
				// Génération réponse si exception
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur lors de la suppression du calendrier", e);
			}
		}
		// Page /calendrier/nePlusEtreProprietaire
		else if(pathInfo.equals("/nePlusEtreProprietaire")) { 
			try {
				int idCalendrier = Integer.parseInt(req.getParameter("idCalendrier"));
				calendrierGestion.nePlusEtreProprietaire(idCalendrier, userId);
				// Génération réponse si aucune exception
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Action NePlusEtreProprietaire réussie", null));
				logger.debug("Action NePlusEtreProprietaire réussie");
			} catch (EdtempsException e) {
				// Génération réponse si exception
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur lors de la requete 'NePlusEtreProprietaire'", e);
			}
		}
		// Autre page, non prise en charge
		else { 
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		bdd.close();
	}
	
	/** Fonction de création d'un calendrier
	 *  
	 * @param casModifier
	 * @param userId
	 * @param calendrierGestion
	 * @param req
	 * @throws EdtempsException
	 * 
	 */
	private void creationOuModificationCalendrier(boolean casModifier, int userId, CalendrierGestion calendrierGestion, HttpServletRequest req) throws EdtempsException {
		
		// Récupération Nom du calendrier
		String nom = req.getParameter("nom");
		
		// Récupération Matiere du calendrier
		String matiere = req.getParameter("matiere");

		// Récupération Type du calendrier
		String type = req.getParameter("type");
		
		// Récupération des ID des propriétaires 
		String stringIdProprietaires = req.getParameter("idProprietaires");
		ArrayList<Integer> idProprietaires = JSONUtils.getIntegerArrayList(
			     Json.createReader(new StringReader(stringIdProprietaires)).readArray());
		
		// Récupération des ID des groupes parents (via parsing manuel du JSON)
		String stringIdGroupesParents = req.getParameter("idGroupesParents");
		ArrayList<Integer> idGroupesParents = JSONUtils.getIntegerArrayList(
			     Json.createReader(new StringReader(stringIdGroupesParents)).readArray());
		// Cas de MODIFICATION d'un calendrier
		if (casModifier) {
			int id = Integer.parseInt(req.getParameter("id"));
			// Création d'un calendrier identifié, contenant les informations récupérées
			CalendrierIdentifie cal = new CalendrierIdentifie(nom, type, matiere, idProprietaires, id, userId);  //ici, l'attribut "créateur" contient l'id du propriétaire effectuant la modification
			// Modification du calendrier = modification de la ligne correspondante dans la BDD
			calendrierGestion.modifierCalendrier(cal, idGroupesParents);
		}
		// Cas de CREATION d'un calendrier
		else {
			// Création d'un calendrier contenant les informations récupérées
			Calendrier cal = new Calendrier(nom, type, matiere, idProprietaires, userId);
			// Création du calendrier = ajout du calendrier dans la BDD
			calendrierGestion.sauverCalendrier(cal, idGroupesParents);
		}
	}

}

