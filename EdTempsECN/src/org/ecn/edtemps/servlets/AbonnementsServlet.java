package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

public class AbonnementsServlet extends RequiresConnectionServlet {

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		boolean paramsOk = true;
		String message;
		ResultCode resultCode;
		JsonValue data;
		
		// Récupération des paramètres
		String strTimestampDebut = req.getParameter("debut");
		String strTimestampFin = req.getParameter("fin");
		
		if(StringUtils.isBlank(strTimestampDebut) || StringUtils.isBlank(strTimestampFin)) {
			paramsOk = false;
			message = "Paramètres début et/ou fin absent(s)";
			resultCode = ResultCode.WRONG_PARAMETERS_FOR_REQUEST;
		}


		try {
			try {
				long timestampDebut = Long.parseLong(strTimestampDebut);
				long timestampFin = Long.parseLong(strTimestampFin);
				
				Date dateDebut = new Date(timestampDebut);
				Date dateFin = new Date(timestampFin);
				
				bdd.startTransaction();
				
				GroupeGestion.makeTempTableListeGroupesAbonnement(bdd, userId); // Création de la table temporaire d'abonnements pour cette transaction
				
				ArrayList<GroupeIdentifie> abonnementsGroupes = groupeGestion.listerGroupesAbonnement(userId, false, true);
				ArrayList<CalendrierIdentifie> abonnementsCalendriers = calendrierGestion.listerCalendriersUtilisateur(userId, false, true);
				ArrayList<EvenementIdentifie> abonnementsEvenements = evenementGestion.listerEvenementsUtilisateur(userId, dateDebut, dateFin, false, true);
				
				bdd.commit(); // Suppression de la table temporaire
				
				// Création de la réponse
				data = Json.createObjectBuilder()
						.add("evenements", JSONUtils.getJsonArray(abonnementsEvenements))
						.add("calendriers", JSONUtils.getJsonArray(abonnementsCalendriers))
						.add("groupes", JSONUtils.getJsonArray(abonnementsGroupes))
						.build();
				
				resultCode = ResultCode.SUCCESS;
				message = "Abonnements récupérés";
				
			} catch (SQLException e) {
				throw new DatabaseException(e);
			}
			
		} catch (DatabaseException e) {
			resultCode = e.getResultCode();
			message = e.getMessage();
			data = null;
		} catch(NumberFormatException e) {
			paramsOk = false;
			resultCode = ResultCode.WRONG_PARAMETERS_FOR_REQUEST;
			message = "Format des paramètres début et/ou fin incorrect";
			data = null;
		}
		
		
		resp.getWriter().write(ResponseManager.generateResponse(resultCode, message, data));
	}
}
