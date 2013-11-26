package org.ecn.edtemps.servlets.impl;

import java.util.ArrayList;
import java.util.Date;

import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.identifie.EvenementComplet;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

public class EvenementsGroupeServlet extends QueryWithIntervalServlet {

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		
		String strIdGroupe = req.getParameter("idGroupe");
		if(strIdGroupe == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idGroupe manquant", null);
		}
		
		try {
			int idGroupe = Integer.parseInt(strIdGroupe);
			
			EvenementGestion evenementGestion = new EvenementGestion(bdd);
			ArrayList<EvenementComplet> evenements = evenementGestion.listerEvenementsGroupe(idGroupe, dateDebut, dateFin, true);
			
			return JSONUtils.getJsonArray(evenements);
		}
		catch(NumberFormatException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idGroupe non numérique fourni", null);
		}
	}
}
