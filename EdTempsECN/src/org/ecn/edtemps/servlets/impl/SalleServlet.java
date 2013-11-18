package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
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
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour la gestion des salles
 * 
 * @author Joffrey Terrade
 */
public class SalleServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2374015380431254517L;
	private static Logger logger = LogManager.getLogger(SalleServlet.class.getName());

	/**
	 * Méthode générale du servlet appelée par la requête POST
	 * Elle redirige vers les différentes fonctionnalités possibles
	 * 
	 * @param userId Identifiant de l'utilisateur qui a fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier") && !pathInfo.equals("/supprimer") && !pathInfo.equals("/get") && !pathInfo.equals("/lister") ) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}

		try {

			// Renvoies vers les différentes fonctionnalités
			switch (pathInfo) {
				case "/ajouter":
					doAjouterSalle(bdd, req, resp);
					break;
				case "/modifier":
					doModifierSalle(bdd, req, resp);
					break;
				case "/supprimer":
					doSupprimerSalle(bdd, req, resp);
					break;
				case "/get":
					doGetSalle(bdd, req, resp);
					break;
				case "/lister":
					doListerSalle(bdd, req, resp);
					break;
			}

			// Ferme l'accès à la base de données
			bdd.close();
		
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON incorrect", null));
			bdd.close();
		} catch(EdtempsException e) {
			logger.error("Erreur lors de l'exécution de la servlet des salles", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}

	}

	
	/**
	 * Ajouter une salle
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doAjouterSalle(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

	}

	
	/**
	 * Modifier une salle
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doModifierSalle(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

	}

	
	/**
	 * Supprimer une salle
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doSupprimerSalle(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

	}

	
	/**
	 * Récupérer une salle
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doGetSalle(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

	}

	
	/**
	 * Lister toutes les salles
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doListerSalle(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		
		SalleGestion gestionnaireSalles = new SalleGestion(bdd);
		
		try {
			
			// Récupération de la liste de toutes les salles
			List<SalleIdentifie> listeSalles = gestionnaireSalles.listerToutesSalles();

			// Création de la réponse
			JsonValue data = Json.createObjectBuilder().add("listeSalles", JSONUtils.getJsonArray(listeSalles)).build();
			
			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

			// Fermeture de l'accès à la base de données
			bdd.close();

		} catch (EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la récupération des salles", e);
		}
	}
	
}
