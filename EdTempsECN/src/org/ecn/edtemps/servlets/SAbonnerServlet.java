package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
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
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

/**
 * Servlet permettant l'abonnement d'un utilisateur à un groupeDeParcipants
 * @author Maxime Terrade
 *
 */
public class SAbonnerServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(SAbonnerServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		int idGroupe = Integer.parseInt(req.getParameter("idGroupe"));
		
		try {
			// Abonnement du l'utilisateur au groupe (= modification de la BDD)
			groupeGestion.sAbonner(userId, idGroupe, false);
			
			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Rattachement à un groupe réussi", null));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors du rattachement", e);
		}

		bdd.close();
	}

}

