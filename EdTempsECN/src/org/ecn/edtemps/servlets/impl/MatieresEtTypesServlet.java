package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;


/**
 * Servlet permettant la récupération des matieres et types (qui qualifient un calendrier)
 * 
 * @author Maxime Terrade
 */
public class MatieresEtTypesServlet extends RequiresConnectionServlet {
	
	private static final long serialVersionUID = -1335896967484393294L;
	private static Logger logger = LogManager.getLogger(MatieresEtTypesServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		JsonValue data;
		
		try {
			CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
			
			// Récupération des matieres
			HashMap<Integer, String> mapMatieres = calendrierGestion.listerMatieres();
			
			// Récupération des types
			HashMap<Integer, String> mapTypes = calendrierGestion.listerTypesCalendrier();
			
			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("matieres", JSONUtils.getJsonStringArray(mapMatieres.values()))
					.add("types", JSONUtils.getJsonStringArray(mapTypes.values()))
					.build();
			
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Récupération des matières et types de calendriers réussie", data));
			
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de l'accès aux matieres/types", e);
		}

		bdd.close();
	}

}

