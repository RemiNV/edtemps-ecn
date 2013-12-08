package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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
 * Servlet pour récupérer la liste des groupes parents potentiels (auquels des groupes peuvent être rattachés)
 * 
 * @author Joffrey Terrade
 */
public class GroupesParentsPotentielsServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -5759790330513965044L;
	private static Logger logger = LogManager.getLogger(GroupesParentsPotentielsServlet.class.getName());

	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		JsonValue data;
		
		Integer idGroupeIgnorerEnfants = null;
		String strGroupeIgnorerEnfants = req.getParameter("idGroupeIgnorerEnfants");
		if(strGroupeIgnorerEnfants != null) {
			try {
				idGroupeIgnorerEnfants = Integer.parseInt(strGroupeIgnorerEnfants);
			}
			catch(NumberFormatException e) {
				resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, 
						"Format du paramètre idGroupeIgnorerEnfants invalide (entier attendu)", null));
			}
		}
		
		try {
			// Récupération de la liste des groupes potentiellement parents
			List<GroupeIdentifie> listeGroupesNonTriee = groupeGestion.listerGroupes(true, true, idGroupeIgnorerEnfants);

			// Remonte dans la liste les groupes dont l'utilisateur est propriétaire
			List<GroupeIdentifie> listeGroupes = new ArrayList<GroupeIdentifie>(); 
			if (CollectionUtils.isNotEmpty(listeGroupesNonTriee)) {
				for (GroupeIdentifie grp : listeGroupesNonTriee) {
					if (grp.getIdProprietaires().contains(userId)) {
						// Si l'utilisateur est dans la liste des propiétaires, on l'ajoute au début de la liste
						listeGroupes.add(0, grp);
					} else {
						// Sinon, on le range à la suite de la liste
						listeGroupes.add(grp);
					}
				}
			}
			
			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("listeGroupes", JSONUtils.getJsonArray(listeGroupes))
					.build();
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération de la liste des parents potentiels", e);
		}

		bdd.close();
	}

}
