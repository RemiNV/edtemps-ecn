package org.ecn.edtemps.servlets.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

public class ListerEvenementsServlet extends QueryWithIntervalServlet {

	private static final long serialVersionUID = -2338486413889378986L;
	
	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		
	String pathInfo = req.getPathInfo();
	EvenementGestion evenementGestion = new EvenementGestion(bdd);
	
	JsonValue res;
			
		switch(pathInfo) {
		case "/groupe":
			int idGroupe = getIntParam(req, "idGroupe");
			res = JSONUtils.getJsonArray(evenementGestion.listerEvenementsGroupe(idGroupe, dateDebut, dateFin, true));
			break;
		case "/intervenant":
			res = JSONUtils.getJsonArray(evenementGestion.listerEvenementsIntervenant(userId, dateDebut, dateFin, true));
			break;
		case "/salle":
			int idSalle = getIntParam(req, "idSalle");
			res = JSONUtils.getJsonArray(evenementGestion.listerEvenementCompletsSalle(idSalle, dateDebut, dateFin, true));
			break;
		case "/groupescalendriers":
			String strIdCalendriers = req.getParameter("idCalendriers");
			if(strIdCalendriers == null) {
				bdd.close();
				throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idCalendriers manquant");
			}
			
			try {
				JsonArray array = Json.createReader(new ByteArrayInputStream(strIdCalendriers.getBytes())).readArray();
				ArrayList<Integer> idCalendriers = JSONUtils.getIntegerArrayList(array);
				res = JSONUtils.getJsonArray(evenementGestion.listerEvenementsGroupesCalendrier(idCalendriers, dateDebut, dateFin, true));
			}
			catch(JsonException e) {
				bdd.close();
				throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format incorrect pour idCalendriers", e);
			}
			break;
		default:
			res = null;
			break;
		}
		
		bdd.close();
		return res;
	}
	
	protected int getIntParam(HttpServletRequest req, String nomParam) throws EdtempsException {
		String strParam = req.getParameter(nomParam);
		if(strParam == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre " + strParam + " manquant", null);
		}
		
		try {
			return Integer.parseInt(strParam);
		}
		catch(NumberFormatException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idGroupe non numérique fourni", null);
		}
	}

}
