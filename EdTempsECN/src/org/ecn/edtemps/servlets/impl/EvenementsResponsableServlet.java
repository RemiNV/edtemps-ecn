package org.ecn.edtemps.servlets.impl;

import java.util.Date;
import java.util.List;

import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

/**
 * Servlet de récupération des évènements dont l'utilisateur est responsable
 * @author Remi
 *
 */
public class EvenementsResponsableServlet extends QueryWithIntervalServlet {

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		// Récupération des calendriers dont l'utilisateur est propriétaire
		List<EvenementIdentifie> evenements = evenementGestion.listerEvenementsResponsable(userId, dateDebut, dateFin, true);
		
		bdd.close();
		
		// Création de la réponse
		return JSONUtils.getJsonArray(evenements);
	}
}
