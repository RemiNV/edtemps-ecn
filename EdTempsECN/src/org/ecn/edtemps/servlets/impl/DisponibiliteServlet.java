package org.ecn.edtemps.servlets.impl;

import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

/**
 * Servlet de vérification de disponibilité d'une salle
 * 
 * Paramètres de la requête : 
 * - dates correspondant à QueryWithIntervalServlet
 * - idSalle
 * @author Remi
 *
 */
public class DisponibiliteServlet extends QueryWithIntervalServlet {

	private static final long serialVersionUID = 3574476580715701446L;

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut,
			Date dateFin, HttpServletRequest req) throws EdtempsException {
		
		String strIdSalle = req.getParameter("idSalle");
		
		if(strIdSalle == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idSalle manquant");
		}
		
		int idSalle;
		
		try {
			idSalle = Integer.parseInt(strIdSalle);
		}
		catch(NumberFormatException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "ID de salle incorrect", e);
		}
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		bdd.startTransaction();
		
		List<EvenementIdentifie> evenementsCours = evenementGestion.listerEvenementsSalleCoursOuPas(idSalle, dateDebut, dateFin, true, false);
		List<EvenementIdentifie> evenementsNonCours = evenementGestion.listerEvenementsSalleCoursOuPas(idSalle, dateDebut, dateFin, false, false);
		
		bdd.commit();
		
		bdd.close();
		
		JsonObject res = Json.createObjectBuilder()
				.add("disponibleCours", evenementsCours.size() == 0)
				.add("disponibleNonCours", evenementsCours.size() == 00 && evenementsNonCours.size() == 0)
				.add("evenementsCours", JSONUtils.getJsonArray(evenementsCours))
				.add("evenementsNonCours", JSONUtils.getJsonArray(evenementsNonCours))
				.build();
		
		return res;
	}

}
