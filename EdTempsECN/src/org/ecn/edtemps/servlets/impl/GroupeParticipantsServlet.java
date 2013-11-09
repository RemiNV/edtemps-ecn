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
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour la gestion des groupes de participants
 * 
 * @author Joffrey Terrade
 */
public class GroupeParticipantsServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -61618228879909663L;

	private static Logger logger = LogManager.getLogger(GroupeParticipantsServlet.class.getName());

	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String pathInfo = req.getPathInfo();
		
		if(!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier") && !pathInfo.equals("/supprimer")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}
		
		// Récupération des infos de l'objet groupe
		String strGroupe = req.getParameter("groupe");
		
		if(strGroupe == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe manquant", null));
			bdd.close();
			return;
		}
		
		JsonReader reader = Json.createReader(new StringReader(strGroupe));
		JsonObject jsonGroupe;
		
		try {
			jsonGroupe = reader.readObject();
			
			String nom = jsonGroupe.getString("nom");
			int idGroupeParent = jsonGroupe.getInt("idGroupeParent");
			boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
			boolean estCours = jsonGroupe.getBoolean("estCours");

			/*
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
		
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON evenement incorrect", null));
			bdd.close();
		} catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification d'un évènement", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}*/
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON evenement incorrect", null));
			bdd.close();
		}
	}


	protected void doAjouterGroupeParticipants(int userId, BddGestion bdd, HttpServletResponse resp) throws EdtempsException, IOException {
		
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		
		groupeGestion.sauverGroupe(null);
		
		// Succès
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe ajouté", null));
	}
}
