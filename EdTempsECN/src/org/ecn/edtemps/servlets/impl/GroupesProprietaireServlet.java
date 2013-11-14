package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour récupérer la liste des groupes dont un utilisateur est propriétaire (fait parti des propriétaire)
 * 
 * @author Joffrey Terrade
 */
public class GroupesProprietaireServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 8063278276185502702L;
	private static Logger logger = LogManager.getLogger(GroupesProprietaireServlet.class.getName());

	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		JsonValue data;
		
		try {
			// Récupération de la liste des groupes desquels l'utilisateur est propriétaire
			List<GroupeIdentifie> listeGroupes = groupeGestion.listerGroupesProprietaire(userId);
			
			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("listeGroupes", JSONUtils.getJsonArray(listeGroupes))
					.build();
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération de la liste des groupes desquels l'utilisateur est propriétaire", e);
		}

		bdd.close();
	}

}
