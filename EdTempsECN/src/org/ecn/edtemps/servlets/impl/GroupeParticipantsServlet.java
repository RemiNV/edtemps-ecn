package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
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

		String pathInfo = req.getPathInfo();

		try {
			// Renvoie vers les différentes fonctionnalités
			switch (pathInfo) {
				case "/ajouter":
					doAjouterGroupeParticipants(userId, bdd, req, resp);
					break;
				case "/modifier":
					doModifierGroupeParticipants(userId, bdd, req, resp);
					break;
				case "/supprimer":
					doSupprimerGroupeParticipants(userId, bdd, req, resp);
					break;
				case "/nePlusEtreProprietaire":
					doSupprimerProprietaire(userId, bdd, req, resp);
					break;
				default: // Fonctionnalité non supportée
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

			// Ferme l'accès à la base de données
			bdd.close();
		
		} catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON 'groupe de participants' incorrect", null));
			bdd.close();
		} catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression d'un groupe de participants", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	/**
	 * Méthode générale du servlet appelée par la requête GET
	 * Elle redirige vers les différentes fonctionnalités possibles
	 * 
	 * @param userId Identifiant de l'utilisateur qui a fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param req Requête
	 * @param resp Réponse pour le client
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String pathInfo = req.getPathInfo();

		try {
			// Renvoie vers les différentes fonctionnalités
			switch (pathInfo) {
				case "/get":
					doGetGroupeParticipants(bdd, req, resp);
					break;
				case "/lister":
					doListerGroupesParticipants(bdd, req, resp);
					break;
				default: // Fonctionnalité non supportée
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

			// Ferme l'accès à la base de données
			bdd.close();
		
		} catch(EdtempsException e) {
			logger.error("Erreur lors de l'opération de récupération/listing d'un groupe de participants", e);
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
		if (idGroupeParent != null && idGroupeParent.equals(-1)) {
			idGroupeParent=null;
		}
		Boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
		Boolean estCours = jsonGroupe.getBoolean("estCours");
		JsonArray jsonIdProprietaires = jsonGroupe.getJsonArray("proprietaires");
		List<Integer> listeIdProprietaires = (jsonIdProprietaires == null) ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdProprietaires);
		
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
		if (idGroupeParent != null && idGroupeParent.equals(-1)) {
			idGroupeParent=null;
		}
		Boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
		Boolean estCours = jsonGroupe.getBoolean("estCours");
		JsonArray jsonIdProprietaires = jsonGroupe.getJsonArray("proprietaires");
		List<Integer> listeIdProprietaires = (jsonIdProprietaires == null) ? null : JSONUtils.getIntegerArrayListSansDoublons(jsonIdProprietaires);
		
		// Vérifie que l'utilisateur est propriétaire du groupe
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		GroupeIdentifie groupe = groupeGestion.getGroupe(idGroupe);
		if (!groupe.getIdProprietaires().contains(userId)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Tentative de modifier un groupe sans être propriétaire");
		}

		// Vérification que l'objet est bien complet
		if (StringUtils.isBlank(nom) || rattachementAutorise == null || estCours == null || CollectionUtils.isEmpty(listeIdProprietaires)) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe incomplet : paramètres manquants", null));
			bdd.close();
			return;
		}

		// Modification
		groupeGestion.modifierGroupe(idGroupe, nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires, userId);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe ajouté", null));
	}

	
	/**
	 * Supprimer un groupe de participants
	 * 
	 * @param userId Identifiant de l'utilisateur qui fait la demande de modification
	 * @param bdd Gestionnaire de la base de données
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doSupprimerGroupeParticipants(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		
		// Récupération de l'identifiant du groupe à supprimer
		Integer id = req.getParameter("id")!=null ? Integer.valueOf(req.getParameter("id")) : null;
		if (id==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Tentative de supprimer un groupe avec un identifiant incorrect");
		}
		
		// Vérifie que l'utilisateur est propriétaire du groupe
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		GroupeIdentifie groupe = groupeGestion.getGroupe(id);
		if (!groupe.getIdProprietaires().contains(userId)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Tentative de supprimer un groupe sans être propriétaire");
		}

		// Supprime le groupe
		groupeGestion.supprimerGroupe(id, true);
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
	
	
	/**
	 * Listing de tous les groupes de participants
	 * 
	 * @param bdd Gestionnaire de la base de données
	 * @param req Requête
	 * @param resp Réponse à compléter
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doListerGroupesParticipants(BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		ArrayList<GroupeIdentifie> groupes = groupeGestion.listerGroupes(true, false);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupes récupérés", JSONUtils.getJsonArray(groupes)));
	}
	

	/**
	 * Supprimer l'utilisateur courant de la liste des propriétaires d'un groupe de participants
	 * 
	 * @param userId Identifiant de l'utilisateur qui a fait la requête
	 * @param bdd Gestionnaire de la base de données
	 * @param req Requête
	 * @param resp Réponse à compléter
	 * 
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doSupprimerProprietaire(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {

		// Récupération des valeurs
		String idGroupeStr = req.getParameter("groupeId");
		int idGroupe;
		if (StringUtils.isBlank(idGroupeStr)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Cette requête requiert un identifiant de groupe");
		} else {
			idGroupe = Integer.valueOf(idGroupeStr);
		}

		// Suppression du propriétaire
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		groupeGestion.supprimerProprietaire(userId, idGroupe);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Propriétaire supprimé de la liste des propriétaires du groupe", null));
	}

}
