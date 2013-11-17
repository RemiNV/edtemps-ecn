package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.models.identifie.GroupeComplet;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet pour la gestion des groupes de participants
 * 
 * @author Joffrey Terrade
 */
public class GroupeParticipantsServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = -61618228879909663L;
	private static Logger logger = LogManager.getLogger(GroupeParticipantsServlet.class.getName());

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
		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier") && !pathInfo.equals("/supprimer") && !pathInfo.equals("/get") ) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}

		try {

			// Renvoies vers les différentes fonctionnalités
			switch (pathInfo) {
				case "/ajouter":
					doAjouterGroupeParticipants(userId, bdd, req, resp);
					break;
				case "/modifier":
					doModifierGroupeParticipants(userId, bdd, req, resp);
					break;
				case "/supprimer":
					doSupprimerGroupeParticipants(bdd, req, resp);
					break;
				case "/get":
					doGetGroupeParticipants(bdd, req, resp);
					break;
			}

			// Ferme l'accès à la base de données
			bdd.close();
		
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON 'groupe de participants' incorrect", null));
			bdd.close();
		} catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression/récupération d'un groupe de participants", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}

	}

	
	/**
	 * Ajouter un groupe de participants
	 * 
	 * @param userId Identifiant de l'utilisateur qui fait la demande d'ajout
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doAjouterGroupeParticipants(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

		// Récupération des infos de l'objet groupe
		String strGroupe = req.getParameter("groupe");
		if (strGroupe == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe manquant", null));
			bdd.close();
			return;
		}

		JsonReader reader = Json.createReader(new StringReader(strGroupe));
		JsonObject jsonGroupe = reader.readObject();
		
		// Récupération des informations sur le groupe
		String nom = jsonGroupe.getString("nom");
		Integer idGroupeParent = Integer.valueOf(jsonGroupe.getString("idGroupeParent"));
		if (idGroupeParent==-1) {
			idGroupeParent=null;
		}
		Boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
		Boolean estCours = jsonGroupe.getBoolean("estCours");
		JsonArray jsonIdProprietaires = jsonGroupe.getJsonArray("proprietaires");
		List<Integer> listeIdProprietaires = (jsonIdProprietaires == null) ? null : JSONUtils.getIntegerArrayList(jsonIdProprietaires);
		
		// Vérification que l'objet est bien complet
		if (StringUtils.isBlank(nom) || rattachementAutorise == null || estCours == null || CollectionUtils.isEmpty(listeIdProprietaires)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe incomplet : paramètres manquants", null));
			bdd.close();
			return;
		}

		// Ajout
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		groupeGestion.sauverGroupe(nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires, userId);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe ajouté", null));
	}

	
	/**
	 * Modifier un groupe de participants
	 * 
	 * @param userId Identifiant de l'utilisateur qui fait la demande de modification
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doModifierGroupeParticipants(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

		// Récupération des infos de l'objet groupe
		String strGroupe = req.getParameter("groupe");
		if (strGroupe == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe manquant", null));
			bdd.close();
			return;
		}

		JsonReader reader = Json.createReader(new StringReader(strGroupe));
		JsonObject jsonGroupe = reader.readObject();
		
		// Récupération des informations sur le groupe
		int idGroupe = jsonGroupe.getInt("id");
		String nom = jsonGroupe.getString("nom");
		Integer idGroupeParent = Integer.valueOf(jsonGroupe.getString("idGroupeParent"));
		if (idGroupeParent==-1) {
			idGroupeParent=null;
		}
		Boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
		Boolean estCours = jsonGroupe.getBoolean("estCours");
		JsonArray jsonIdProprietaires = jsonGroupe.getJsonArray("proprietaires");
		List<Integer> listeIdProprietaires = (jsonIdProprietaires == null) ? null : JSONUtils.getIntegerArrayList(jsonIdProprietaires);
		
		// Vérification que l'objet est bien complet
		if (StringUtils.isBlank(nom) || rattachementAutorise == null || estCours == null || CollectionUtils.isEmpty(listeIdProprietaires)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe incomplet : paramètres manquants", null));
			bdd.close();
			return;
		}

		// Modification
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		groupeGestion.modifierGroupe(idGroupe, nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires, userId);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe ajouté", null));
	}

	
	/**
	 * Supprimer un groupe de participants
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doSupprimerGroupeParticipants(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		groupeGestion.supprimerGroupe(Integer.valueOf(req.getParameter("id")));
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe supprimé", null));
	}

	/**
	 * Récupérer un groupe de participants
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doGetGroupeParticipants(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		GroupeComplet groupe = groupeGestion.getGroupeComplet(Integer.valueOf(req.getParameter("id")));
		JsonValue data = Json.createObjectBuilder().add("groupe", groupe.toJson()).build();
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe récupéré", data));
	}

}
