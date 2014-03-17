package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
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
import org.ecn.edtemps.managers.PeriodeBloqueeGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.managers.UtilisateurGestion.ActionsEdtemps;
import org.ecn.edtemps.models.identifie.PeriodeBloqueeIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des périodes bloquées
 * 
 * @author Joffrey
 */
public class PeriodesBloqueesServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -7109999235327735066L;
	private static Logger logger = LogManager.getLogger(PeriodesBloqueesServlet.class.getName());
	
	private static class PrePeriodeBloquee {
		public String libelle;
		public Date dateDebut;
		public Date dateFin;
		public ArrayList<Integer> listeIdGroupes;
		public Integer idPeriodeBloquee;
		public Boolean vacances;
		public Boolean fermeture;
	}

	
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/getperiodesbloquees":
				doGetPeriodesBloquees(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de gestion des périodes bloquées ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	

	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			// Vérifie que l'utilisateur est autorisé à gérer les périodes bloquées
			UtilisateurGestion userGestion = new UtilisateurGestion(bdd);
			if (!userGestion.aDroit(ActionsEdtemps.GERER_JOURS_BLOQUES, userId)) {
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Utilisateur non autorisé à gérer les périodes bloquées");
			}
			
			switch(pathInfo) {
			case "/ajouter":
				doAjouter(bdd, req, resp);
				break;
			case "/modifier":
				doModifier(bdd, req, resp);
				break;
			case "/supprimer":
				doSupprimer(bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression d'une période bloquée ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	
	/**
	 * Récupérer toutes les périodes bloquées sur une période donnée
	 * @param userId
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doGetPeriodesBloquees(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres
		Date debut = this.getDateInRequest(req, "debut");
		Date fin = this.getDateInRequest(req, "fin");

		// Quelques vérifications sur les dates
		if (debut==null || fin==null || debut.after(fin)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST);
		}
		
		// Exécute la requête de récupération avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		List<PeriodeBloqueeIdentifie> resultat = gestionnaire.getPeriodesBloquees(debut, fin);
		bdd.close();
		
		// Création de la réponse
		JsonValue data = Json.createObjectBuilder()
				.add("listePeriodesBloquees", JSONUtils.getJsonArray(resultat))
				.build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "", data));
	}


	/**
	 * Supprimer une période bloquée
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doSupprimer(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres
		String strId = req.getParameter("idPeriodeBloquee");
		if(StringUtils.isBlank(strId)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idPeriodeBloquee non fourni pour une suppression de période bloquée");
		}
		int idPeriodeBloquee = Integer.parseInt(strId);

		// Exécute la requête de suppression avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.supprimerPeriodeBloquee(idPeriodeBloquee);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée supprimée", null));
	}
	

	/**
	 * Ajouter une période bloquée
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doAjouter(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		
		// Récupère les paramètres dans la requête
		PrePeriodeBloquee param = recupererInformation(req);

		// Exécute la requête d'ajout avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.sauverPeriodeBloquee(param.libelle, param.dateDebut, param.dateFin, param.vacances, param.fermeture, param.listeIdGroupes);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée ajoutée", null));

	}


	/**
	 * Modifier un jour férié
	 * @param bdd
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doModifier(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {

		// Récupère les paramètres dans la requête
		PrePeriodeBloquee param = recupererInformation(req);
		if (param.idPeriodeBloquee == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet periode incomplet");
		}

		// Exécute la requête de modification avec le gestionnaire
		PeriodeBloqueeGestion gestionnaire = new PeriodeBloqueeGestion(bdd);
		gestionnaire.modifierPeriodeBloquee(param.idPeriodeBloquee, param.libelle, param.dateDebut, param.dateFin, param.vacances, param.fermeture, param.listeIdGroupes);
		bdd.close();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Période bloquée modifiée", null));

	}
	
	
	/**
	 * Générer un objet de type PrePeriodeBloquee qui contient tous les champs intéressants à partir d'une requpete HTTP
	 * 
	 * @param req La requête à traiter
	 * @return l'objet PrePeriodeBloquee
	 * @throws EdtempsException
	 */
	protected PrePeriodeBloquee recupererInformation(HttpServletRequest req) throws EdtempsException {

		PrePeriodeBloquee res = new PrePeriodeBloquee();
		
		String strPeriode = req.getParameter("periode");
		if(StringUtils.isBlank(strPeriode)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet periode manquant");
		}
		
		JsonReader reader = Json.createReader(new StringReader(strPeriode));
		JsonObject jsonPeriode = reader.readObject();
		
		JsonNumber jsonId = getJsonNumberOrNull(jsonPeriode, "idPeriodeBloquee"); 
		res.idPeriodeBloquee = jsonId == null ? null : new Integer(jsonId.intValue());

		JsonNumber jsonDebut = getJsonNumberOrNull(jsonPeriode, "dateDebut"); 
		res.dateDebut = jsonDebut == null ? null : new Date(jsonDebut.longValue());

		JsonNumber jsonFin = getJsonNumberOrNull(jsonPeriode, "dateFin");
		res.dateFin = jsonFin == null ? null : new Date(jsonFin.longValue());

		JsonArray jsonIdGroupes = getJsonArrayOrNull(jsonPeriode, "listeGroupes");
		res.listeIdGroupes = jsonIdGroupes == null ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdGroupes);

		res.libelle = jsonPeriode.containsKey("libelle") && !jsonPeriode.isNull("libelle") ? jsonPeriode.getString("libelle") : null;
		res.vacances = jsonPeriode.containsKey("vacances") && !jsonPeriode.isNull("vacances") ? jsonPeriode.getBoolean("vacances") : null;
		res.fermeture = jsonPeriode.containsKey("fermeture") && !jsonPeriode.isNull("fermeture") ? jsonPeriode.getBoolean("fermeture") : null;

		if (res.dateDebut==null || res.dateFin==null || res.dateDebut.after(res.dateFin) || StringUtils.isBlank(res.libelle) || res.listeIdGroupes.isEmpty() || res.vacances==null || res.fermeture==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet periode incomplet");
		}
		
		return res;
	}

	protected JsonNumber getJsonNumberOrNull(JsonObject object, String key) {
		return object.containsKey(key) && !object.isNull(key) ? object.getJsonNumber(key) : null;
	}

	protected JsonArray getJsonArrayOrNull(JsonObject object, String key) {
		return object.containsKey(key) && !object.isNull(key) ? object.getJsonArray(key) : null;
	}
}
