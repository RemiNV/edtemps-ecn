package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class EvenementServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(EvenementServlet.class.getName());
	
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String pathInfo = req.getPathInfo();
		
		if(!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}
		
		// Récupération des infos de l'objet évènement
		String strEvenement = req.getParameter("evenement");
		
		if(strEvenement == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet évènement manquant", null));
			bdd.close();
			return;
		}
		
		JsonReader reader = Json.createReader(new StringReader(strEvenement));
		JsonObject jsonEvenement;
		
		try {
			jsonEvenement = reader.readObject();
			
			String nom = jsonEvenement.getString("nom", null);
			JsonNumber jsonDebut = jsonEvenement.getJsonNumber("dateDebut");
			JsonNumber jsonFin = jsonEvenement.getJsonNumber("dateFin");
			JsonArray jsonIdCalendriers = jsonEvenement.getJsonArray("calendriers");
			JsonArray jsonIdSalles = jsonEvenement.getJsonArray("salles");
			JsonArray jsonIdIntervenants = jsonEvenement.getJsonArray("intervenants");
			JsonArray jsonIdResponsables = jsonEvenement.getJsonArray("responsables");
			
			Date dateDebut = jsonDebut == null ? null : new Date(jsonDebut.longValue());
			Date dateFin = jsonFin == null ? null : new Date(jsonFin.longValue());
			ArrayList<Integer> idCalendriers = jsonIdCalendriers == null ? null : JSONUtils.getIntegerArrayList(jsonIdCalendriers);
			ArrayList<Integer> idSalles = jsonIdSalles == null ? null : JSONUtils.getIntegerArrayList(jsonIdSalles);
			ArrayList<Integer> idIntervenants = jsonIdIntervenants == null ? null : JSONUtils.getIntegerArrayList(jsonIdIntervenants);
			ArrayList<Integer> idResponsables = jsonIdResponsables == null ? null : JSONUtils.getIntegerArrayList(jsonIdResponsables);
			
			if(pathInfo.equals("/ajouter")) { // Requête /evenement/ajouter
				doAjouterEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables);
			}
			else if(pathInfo.equals("/modifier")) { // Requête /evenement/modifier
				
				JsonNumber jsonIdEvenement = jsonEvenement.getJsonNumber("id");
				if(jsonIdEvenement == null) {
					resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, 
							"Objet evenenement incomplet : paramètre id manquant pour requête de modification", null));
					bdd.close();
					return;
				}
				
				int idEvenement = jsonIdEvenement.intValue();
				
				doModifierEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, idEvenement);
			}
			
			bdd.close();
		}
		catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON evenement incorrect", null));
			bdd.close();
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification d'un évènement", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	
	protected void doAjouterEvenement(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Date dateDebut, Date dateFin, 
			ArrayList<Integer> idCalendriers, ArrayList<Integer> idSalles, ArrayList<Integer> idIntervenants, 
			ArrayList<Integer> idResponsables) throws EdtempsException, IOException {
		
		// Vérification des paramètres
		if(nom == null || StringUtils.isBlank(nom) || dateDebut == null || dateFin == null || idCalendriers == null || idSalles == null 
				|| idIntervenants == null || idResponsables == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet evenenement incomplet : paramètres manquants", null));
			logger.warn("Objet d'évènement fourni avec des paramètres manquants lors de l'ajout");
			return;
		}
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		evenementGestion.sauverEvenement(nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables);

		// Succès
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evènement ajouté", null));
	}
	
	
	protected void doModifierEvenement(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Date dateDebut, Date dateFin, 
			ArrayList<Integer> idCalendriers, ArrayList<Integer> idSalles, ArrayList<Integer> idIntervenants, 
			ArrayList<Integer> idResponsables, int idEvenement) throws IOException, EdtempsException {
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		bdd.startTransaction();
		
		// Récupération de l'ancien évènement
		EvenementIdentifie even = evenementGestion.getEvenement(idEvenement);
		
		if(even == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Evènement d'ID " + idEvenement + " inexistant", null));
			logger.warn("ID d'évènement inexistant fourni pour l'ajout d'un évènement");
			return;
		}
		
		// Génération des nouveaux paramètres
		String nvNom = nom == null ? even.getNom() : nom;
		Date nvDateDebut = dateDebut == null ? even.getDateDebut() : dateDebut;
		Date nvDateFin = dateFin == null ? even.getDateFin() : dateFin;
		List<Integer> nvIdCalendriers = idCalendriers == null ? even.getIdCalendriers() : idCalendriers;
		ArrayList<Integer> nvIdSalles = idSalles == null ? getIdSalles(even.getSalles()) : idSalles;
		ArrayList<Integer> nvIdIntervenants = idIntervenants == null ? getUserIds(even.getIntervenants()) : idIntervenants;
		ArrayList<Integer> nvIdResponsables = idResponsables == null ? getUserIds(even.getResponsables()) : idResponsables;
		
		evenementGestion.modifierEvenement(idEvenement, nvNom, nvDateDebut, nvDateFin, nvIdCalendriers, nvIdSalles, nvIdIntervenants, nvIdResponsables, false);
		
		bdd.commit();
		
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evènement modifié", null));
	}
	
	protected static ArrayList<Integer> getIdSalles(List<SalleIdentifie> salles) {
		ArrayList<Integer> res = new ArrayList<Integer>(salles.size());
		
		for(SalleIdentifie s : salles) {
			res.add(s.getId());
		}
		
		return res;
	}
	
	protected static ArrayList<Integer> getUserIds(List<UtilisateurIdentifie> utilisateurs) {
		ArrayList<Integer> res = new ArrayList<Integer>(utilisateurs.size());
		
		for(UtilisateurIdentifie u : utilisateurs) {
			res.add(u.getId());
		}
		
		return res;
	}
}
