package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet permettant la création, la modification et la suppression d'un calendrier
 * @author Maxime Terrade
 *
 */
public class ParametresCalendrierServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(ParametresCalendrierServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String pathInfo = req.getPathInfo();
		
		if(pathInfo == null) { // Page /calendrier/ =>  on renvoie les calendriers dont l'utilisateur est propriétaire
			
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
		else { // Autre page
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}
	
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String pathInfo = req.getPathInfo();
		
		if(pathInfo == null) { // Page /calendrier/, accessible en GET
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		else if(pathInfo.equals("/creation")) { // page /calendrier/creation
			try {
				creationCalendrier(userId, bdd, req);
				// Génération réponse si aucune exception
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Création calendrier réussie", null));
				logger.debug("Création calendrier réussie");
			} catch (EdtempsException e) {
				// Génération réponse si exception
				resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
				logger.error("Erreur lors de la création du calendrier", e);
			}
		}
		else { // Autre page
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}
	
	/** Fonction de création d'un calendrier 
	 * 
	 * @throws EdtempsException 
	 */
	private void creationCalendrier(int userId, BddGestion bdd, HttpServletRequest req) throws EdtempsException {
		
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		
		// Récupération Nom du calendrier
		String nom = req.getParameter("nom");
		
		// Récupération Matiere du calendrier
		String matiere = req.getParameter("matiere");

		// Récupération Type du calendrier
		String type = req.getParameter("type");
		
		// Récupération des ID des propriétaires (via parsing manuel du JSON)
		ArrayList<Integer> idProprietaires = new ArrayList<Integer>();
		String stringIdProprietaires = req.getParameter("idProprietaires");
		//Parsing manuel de la chaine JSON. Ex de chaine : "["1","2","33"]"
		stringIdProprietaires = stringIdProprietaires.replace("[", "");
		stringIdProprietaires = stringIdProprietaires.replace("]", "");
		stringIdProprietaires = stringIdProprietaires.replace("\"", "");
		String[] tableauIdProprietaires = stringIdProprietaires.split(",");
		//Ajout des id à la liste des propriétaires
		for (String s : tableauIdProprietaires) {
		  idProprietaires.add(Integer.parseInt(s)); 
		}
		
		// Création d'un calendrier contenant les informations récupérées
		Calendrier cal = new Calendrier(nom, type, matiere, idProprietaires);
		
		// Création du calendrier = ajout du calendrier dans la BDD
		calendrierGestion.sauverCalendrier(cal);

		bdd.close();
	}

}

