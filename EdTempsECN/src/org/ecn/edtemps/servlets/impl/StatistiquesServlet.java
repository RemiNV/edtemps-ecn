package org.ecn.edtemps.servlets.impl;

import java.util.Date;

import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.StatistiquesGestion;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

/**
 * Servlet de récupération des statistiques de cours déjà planifiés pour une matière
 * @author Remi
 *
 */
public class StatistiquesServlet extends QueryWithIntervalServlet {

	private static final long serialVersionUID = -6372857165562624586L;

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut,
			Date dateFin, HttpServletRequest req) throws EdtempsException {
		// Récupération de la matière
		String matiere = req.getParameter("matiere");
		
		StatistiquesGestion statistiquesGestion = new StatistiquesGestion(bdd);
		
		return statistiquesGestion.getStatistiques(userId, dateDebut, dateFin, matiere).toJson();
	}

}
