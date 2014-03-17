package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.JourFerieGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.managers.UtilisateurGestion.ActionsEdtemps;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des jours fériés
 * 
 * @author Joffrey
 */
public class JoursFeriesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2647012858867960542L;
	private static Logger logger = LogManager.getLogger(JoursFeriesServlet.class.getName());
	
	private static class PreJourFerie {
		public Integer idJour;
		public String libelle;
		public Date date;
	}
	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getJoursFeries":
				doGetJoursFeries(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des jours fériés ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	

	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			// Vérifie que l'utilisateur est autorisé à gérer les jours fériés
			UtilisateurGestion userGestion = new UtilisateurGestion(bdd);
			if (!userGestion.aDroit(ActionsEdtemps.GERER_JOURS_BLOQUES, userId)) {
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Utilisateur non autorisé à gérer les jours fériés");
			}

			switch(pathInfo) {
			case "/ajouter":
				doAjouter(userId, bdd, req, resp);
				break;
			case "/modifier":
				doModifier(userId, bdd, req, resp);
				break;
			case "/supprimer":
				doSupprimer(userId, bdd, req, resp);
				break;
			case "/ajoutautomatique":
				doAjouterAuto(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression d'un jour férié ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	
	/**
	 * Récupérer tous les jours fériés sur une période donnée
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doGetJoursFeries(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");
		
		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}

		// Exécute la requête de récupération avec le gestionnaire
		JourFerieGestion gestionnaireJoursFeries = new JourFerieGestion(bdd);
		List<JourFerieIdentifie> resultat = gestionnaireJoursFeries.getJoursFeries(debut, fin);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listeJoursFeries", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Liste des jours fériés récupérés", data));
	}
	
	
	/**
	 * Supprimer un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doSupprimer(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String strIdJourFerie = req.getParameter("idJourFerie");
		if(StringUtils.isBlank(strIdJourFerie)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idJourFerie non fourni pour une suppression de jour férié");
		}
		int idJourFerie = Integer.parseInt(strIdJourFerie);

		// Exécute la requête de suppression avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.supprimerJourFerie(idJourFerie, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié supprimé", null));
	}
	
	
	/**
	 * Ajouter un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doAjouter(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres dans la requête
		PreJourFerie param = recupererInformation(req);

		// Exécute la requête d'ajout avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.sauverJourFerie(param.libelle, param.date, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié ajouté", null));

	}


	/**
	 * Modifier un jour férié
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doModifier(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres dans la requête
		PreJourFerie param = recupererInformation(req);
		if (param.idJour == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet jour incomplet");
		}
		JourFerieIdentifie jour = new JourFerieIdentifie(param.idJour, param.libelle, param.date);
		
		// Exécute la requête de modification avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		jourFerieGestion.modifierJourFerie(jour, userId);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Jour férié modifié", null));

	}
	
	
	/**
	 * Ajouter automatiquement tous les jours fériés
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doAjouterAuto(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String strAnnee = req.getParameter("annee");
		if(strAnnee == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre annee non fourni pour un ajout automatique des jours fériés");
		}
		int annee = Integer.parseInt(strAnnee);
		
		// Exécute la requête d'ajout automatique avec le gestionnaire
		JourFerieGestion jourFerieGestion = new JourFerieGestion(bdd);
		Map<String, Boolean> resultat = jourFerieGestion.ajoutAutomatiqueJoursFeries(annee, userId);
		bdd.close();
		
		// Découpe le résultat en deux listes : les jours ajoutés, et ceux qui sont déjà présents en base
		List<String> listeAjoutes = new ArrayList<String>();
		List<String> listeNonAjoutes = new ArrayList<String>();
		
		for (Entry<String, Boolean> jour : resultat.entrySet()) {
			if (jour.getValue()) {
				listeAjoutes.add(jour.getKey());
			} else {
				listeNonAjoutes.add(jour.getKey());
			}
		}
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listeAjoutes", JSONUtils.getJsonStringArray(listeAjoutes))
				.add("listeNonAjoutes", JSONUtils.getJsonStringArray(listeNonAjoutes))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));

	}


	/**
	 * Générer un objet de type PreJourFerie qui contient tous les champs intéressants à partir d'une requpete HTTP
	 * 
	 * @param req La requête à traiter
	 * @return l'objet PreJourFerie
	 * @throws EdtempsException
	 */
	protected PreJourFerie recupererInformation(HttpServletRequest req) throws EdtempsException {

		PreJourFerie res = new PreJourFerie();
		
		String strPeriode = req.getParameter("jour");
		if(StringUtils.isBlank(strPeriode)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet jour manquant");
		}
		
		JsonReader reader = Json.createReader(new StringReader(strPeriode));
		JsonObject jsonObject = reader.readObject();
		JsonNumber jsonId = getJsonNumberOrNull(jsonObject, "idJourFerie"); 
		JsonNumber jsonDebut = getJsonNumberOrNull(jsonObject, "date"); 

		res.libelle = jsonObject.containsKey("libelle") && !jsonObject.isNull("libelle") ? jsonObject.getString("libelle") : null;
		res.idJour = jsonId == null ? null : new Integer(jsonId.intValue());
		res.date = jsonDebut == null ? null : new Date(jsonDebut.longValue());

		if (res.date==null || StringUtils.isBlank(res.libelle)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet jour incomplet");
		}
		
		return res;
	}

	protected JsonNumber getJsonNumberOrNull(JsonObject object, String key) {
		return object.containsKey(key) && !object.isNull(key) ? object.getJsonNumber(key) : null;
	}

	
}
