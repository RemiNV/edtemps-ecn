package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
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
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.GroupeComplet;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour la gestion rattachement aux groupes de participants
 * 
 * @author Joffrey Terrade
 */
public class RattachementGroupeServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 6204340565926277288L;
	private static Logger logger = LogManager.getLogger(RattachementGroupeServlet.class.getName());

	/**
	 * Méthode générale du servlet appelée par la requête POST
	 * Elle redirige vers les différentes méthodes possibles
	 * @param userId Identifiant de l'utilisateur qui a fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/listermesdemandes") && !pathInfo.equals("/decidergroupe") && !pathInfo.equals("/decidercalendrier") ) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}

		try {

			// Renvoies vers les différentes fonctionnalités
			switch (pathInfo) {
				case "/listermesdemandes":
					doListerMesDemandesDeRattachement(userId, bdd, req, resp);
					break;
				case "/decidergroupe":
					doDeciderDemandeDeRattachementGroupe(userId, bdd, req, resp);
					break;
				case "/decidercalendrier":
					doDeciderDemandeDeRattachementCalendrier(userId, bdd, req, resp);
					break;
			}

			bdd.close();
		
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON incorrect", null));
			bdd.close();
		} catch(EdtempsException e) {
			logger.error("Erreur avec le servlet de rattachement à un groupe de participants (listerDemandes ou accepter ou refuser)", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		} catch(SQLException e) {
			logger.error("Erreur avec le servlet de rattachement à un groupe de participants (listerDemandes ou accepter ou refuser)", e);
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.DATABASE_ERROR, e.getMessage(), null));
			bdd.close();
		}

	}
	
	/**
	 * Lister les demandes de rattachements d'un utilisateur
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws EdtempsException
	 */
	protected void doListerMesDemandesDeRattachement(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException, EdtempsException {
		GroupeGestion gestionnaireGroupes = new GroupeGestion(bdd);
		List<GroupeComplet> listeGroupes = gestionnaireGroupes.listerDemandesDeRattachementGroupes(userId);
		List<CalendrierComplet> listeCalendriers = gestionnaireGroupes.listerDemandesDeRattachementCalendriers(userId);
		JsonObject data = Json.createObjectBuilder()
				.add("listeGroupes", JSONUtils.getJsonArray(listeGroupes))
				.add("listeCalendriers", JSONUtils.getJsonArray(listeCalendriers)).build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "La liste des groupes et la liste des calendriers en attente de rattachement sont récupérées", data));
	}
	
	/**
	 * Décider une demande de rattachement d'un groupe
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doDeciderDemandeDeRattachementGroupe(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		Boolean choix = Boolean.valueOf(req.getParameter("etat"));
		Integer groupeId = Integer.valueOf(req.getParameter("id"));
		groupeGestion.deciderRattachementGroupe(choix, groupeId);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Rattachement "+(choix ? "accepté" : "refusé"), null));
	}

	/**
	 * Décider une demande de rattachement pour un calendrier
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doDeciderDemandeDeRattachementCalendrier(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		Boolean choix = Boolean.valueOf(req.getParameter("etat"));
		Integer groupeIdParent = req.getParameter("groupeIdParent")==null ? null : Integer.valueOf(req.getParameter("groupeIdParent"));
		Integer calendrierId = req.getParameter("calendrierId")==null ? null : Integer.valueOf(req.getParameter("calendrierId"));
		if (calendrierId==null | groupeIdParent==null | choix==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Les paramètres de la requêtes ne sont pas corrects, il faut un identifiant de groupe parent, un identifiant de calendrier et un choix.");
		}
		groupeGestion.deciderRattachementCalendrier(choix, groupeIdParent, calendrierId);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Rattachement "+(choix ? "accepté" : "refusé"), null));
	}
}
