package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

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
			JsonArray jsonIdCalendriers = jsonEvenement.getJsonArray("idCalendriers");
			JsonArray jsonIdSalles = jsonEvenement.getJsonArray("salles");
			JsonArray jsonIdIntervenants = jsonEvenement.getJsonArray("intervenants");
			JsonArray jsonIdResponsables = jsonEvenement.getJsonArray("responsables");
			JsonArray jsonMateriels = jsonEvenement.getJsonArray("materiels");
			
			if(nom == null || StringUtils.isBlank(nom) || jsonDebut == null || jsonFin == null || jsonIdCalendriers == null || jsonIdSalles == null 
					|| jsonIdIntervenants == null || jsonIdResponsables == null || jsonMateriels == null) {
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet evenenement incomplet : paramètres manquants", null));
				bdd.close();
				return;
			}
			
			Date dateDebut = new Date(jsonDebut.longValue());
			Date dateFin = new Date(jsonFin.longValue());
			ArrayList<Integer> idCalendriers = JSONUtils.getIntegerArrayList(jsonIdCalendriers);
			ArrayList<Integer> idSalles = JSONUtils.getIntegerArrayList(jsonIdSalles);
			ArrayList<Integer> idIntervenants = JSONUtils.getIntegerArrayList(jsonIdIntervenants);
			ArrayList<Integer> idResponsables = JSONUtils.getIntegerArrayList(jsonIdResponsables);
			
			ArrayList<Materiel> materiels = new ArrayList<Materiel>(jsonMateriels.size());
			for(JsonValue v : jsonMateriels) {
				JsonObject o = (JsonObject) v;
				materiels.add(Materiel.inflateFromJson(o));
			}
			
			if(pathInfo.equals("/ajouter")) { // Requête /evenement/ajouter
				doAjouterEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, materiels);
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
				
				doModifierEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, materiels, idEvenement);
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
			ArrayList<Integer> idResponsables, ArrayList<Materiel> materiels) throws EdtempsException {
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		evenementGestion.sauverEvenement(nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, materiels);
	}
	
	
	protected void doModifierEvenement(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Date dateDebut, Date dateFin, 
			ArrayList<Integer> idCalendriers, ArrayList<Integer> idSalles, ArrayList<Integer> idIntervenants, 
			ArrayList<Integer> idResponsables, ArrayList<Materiel> materiels, int idEvenement) throws IOException, EdtempsException {
		// TODO : remplir
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
