package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
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
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.CalendrierGestion.DroitsCalendriers;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class EvenementServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(EvenementServlet.class.getName());
	
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String pathInfo = req.getPathInfo();
		
		if(!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}
		
		// Récupération des infos de l'objet évènement
		String strEvenement = req.getParameter("evenement");
		
		if(strEvenement == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet évènement manquant", null));
			bdd.close();
			return;
		}
		
		JsonReader reader = Json.createReader(new StringReader(strEvenement));
		JsonObject jsonEvenement;
		
		try {
			jsonEvenement = reader.readObject();
			
			String nom = jsonEvenement.getString("nom", null);
			JsonNumber jsonDebut = jsonEvenement.getJsonNumber("dateDebut");
			JsonNumber jsonFin = jsonEvenement.getJsonNumber("dateFin");
			JsonArray jsonIdCalendriers = jsonEvenement.getJsonArray("calendriers");
			JsonArray jsonIdSalles = jsonEvenement.getJsonArray("salles");
			JsonArray jsonIdIntervenants = jsonEvenement.getJsonArray("intervenants");
			JsonArray jsonIdResponsables = jsonEvenement.getJsonArray("responsables");
			JsonArray jsonIdEvenementsSallesALiberer = jsonEvenement.getJsonArray("evenementsSallesALiberer");
			JsonNumber jsonIdEvenement = jsonEvenement.getJsonNumber("id"); // Uniquement pour la modification
			
			Date dateDebut = jsonDebut == null ? null : new Date(jsonDebut.longValue());
			Date dateFin = jsonFin == null ? null : new Date(jsonFin.longValue());
			ArrayList<Integer> idCalendriers = jsonIdCalendriers == null ? null : JSONUtils.getIntegerArrayList(jsonIdCalendriers);
			ArrayList<Integer> idSalles = jsonIdSalles == null ? null : JSONUtils.getIntegerArrayList(jsonIdSalles);
			ArrayList<Integer> idIntervenants = jsonIdIntervenants == null ? null : JSONUtils.getIntegerArrayList(jsonIdIntervenants);
			ArrayList<Integer> idResponsables = jsonIdResponsables == null ? null : JSONUtils.getIntegerArrayList(jsonIdResponsables);
			ArrayList<Integer> idEvenementsSallesALiberer = jsonIdEvenementsSallesALiberer == null ? null : JSONUtils.getIntegerArrayList(jsonIdEvenementsSallesALiberer);
			
			EvenementGestion evenementGestion = new EvenementGestion(bdd);
			
			// Récupération de l'ancien évènement (pour les modifications)
			EvenementIdentifie oldEven = null;
			if(jsonIdEvenement != null) {
				oldEven = evenementGestion.getEvenement(jsonIdEvenement.intValue());
			}
			
			DroitsCalendriers droitsCalendriers = null;
			CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
			
			if(idCalendriers == null && oldEven == null) {
				throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Vous devez préciser des calendriers pour ajouter un évènement");
			}
			
			// Utilisation des nouveaux calendriers (ajout ou modification des calendriers) ou des anciens (si aucune modification)
			List<Integer> idCalendriersDroits = idCalendriers != null ? idCalendriers : oldEven.getIdCalendriers();
			
			// Récupération des droits des calendriers donnés
			droitsCalendriers = calendrierGestion.getDroitsCalendriers(userId, idCalendriersDroits);
			
			// TODO : autoriser un administrateur à faire ceci
			if(!droitsCalendriers.estProprietaire) {
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Vous n'êtes pas propriétaire de tous les calendriers de l'évènement");
			}
			
			bdd.startTransaction();
			
			// Libération des salles déjà occupées pour un cours
			// TODO : avertir le propriétaire d'un évènement quand la salle qu'il a renseignée est libérée par un nouvel évènement
			if(idEvenementsSallesALiberer != null && !idEvenementsSallesALiberer.isEmpty()) {
				
				if(!droitsCalendriers.contientCours) {
					bdd.rollback();
					throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Vous ne pouvez pas prendre une salle déjà occupée pour un évènement autre qu'un cours");
				}
				
				evenementGestion.supprimerSallesEvenementsNonCours(idSalles, idEvenementsSallesALiberer);
			}
			
			if(pathInfo.equals("/ajouter")) { // Requête /evenement/ajouter
				doAjouterEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables);
			}
			else if(pathInfo.equals("/modifier")) { // Requête /evenement/modifier
				
				if(oldEven == null) {
					resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, 
							"Objet evenenement incomplet : paramètre id manquant pour requête de modification", null));
					
					logger.warn("ID d'évènement inexistant fourni pour l'ajout d'un évènement");
					return;
				}
				
				doModifierEvenement(userId, bdd, resp, nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, oldEven);
			}
			
			bdd.commit();
			
			bdd.close();
		}
		catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON evenement incorrect", null));
			bdd.close();
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification d'un évènement", e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	/**
	 * Ajout d'un évènement. Doit être appelé à l'intérieur d'une transaction.
	 * 
	 * @param userId ID de l'utilisateur
	 * @param bdd Base de données à utiliser, avec une transaction commencée
	 * @param resp Servlet à utiliser pour envoyer une réponse
	 * @param nom
	 * @param dateDebut
	 * @param dateFin
	 * @param idCalendriers
	 * @param idSalles
	 * @param idIntervenants
	 * @param idResponsables
	 * @throws EdtempsException
	 * @throws IOException
	 */
	protected void doAjouterEvenement(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Date dateDebut, Date dateFin, 
			ArrayList<Integer> idCalendriers, ArrayList<Integer> idSalles, ArrayList<Integer> idIntervenants, 
			ArrayList<Integer> idResponsables) throws EdtempsException, IOException {
		
		// Vérification des paramètres
		if(nom == null || StringUtils.isBlank(nom) || dateDebut == null || dateFin == null || idCalendriers == null || idSalles == null 
				|| idIntervenants == null || idResponsables == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet evenenement incomplet : paramètres manquants", null));
			logger.warn("Objet d'évènement fourni avec des paramètres manquants lors de l'ajout");
			return;
		}
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		evenementGestion.sauverEvenement(nom, dateDebut, dateFin, idCalendriers, idSalles, idIntervenants, idResponsables, false);

		// Succès
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evènement ajouté", null));
	}
	
	/**
	 * Modification d'un évènement. Doit être appelé à l'intérieur d'une transaction.
	 * 
	 * @param userId ID de l'utilisateur
	 * @param bdd Base de données à utiliser, avec une transaction commencée
	 * @param resp Servlet à utiliser pour envoyer une réponse
	 * @param nom
	 * @param dateDebut
	 * @param dateFin
	 * @param idCalendriers
	 * @param idSalles
	 * @param idIntervenants
	 * @param idResponsables
	 * @param oldEven
	 * @throws IOException
	 * @throws EdtempsException
	 */
	protected void doModifierEvenement(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Date dateDebut, Date dateFin, 
			ArrayList<Integer> idCalendriers, ArrayList<Integer> idSalles, ArrayList<Integer> idIntervenants, 
			ArrayList<Integer> idResponsables, EvenementIdentifie oldEven) throws IOException, EdtempsException {
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		ArrayList<Integer> oldIdsResponsables = getUserIds(oldEven.getResponsables());
		
		// TODO : autoriser un administrateur à faire ceci
		// Vérification que l'utilisateur est autorisé à modifier cet évènement
		if(!oldIdsResponsables.contains(userId)) {
			bdd.rollback();
			throw new EdtempsException(ResultCode.IDENTIFICATION_ERROR, "Vous n'êtes pas responsable de cet évènement");
		}
		
		// Génération des nouveaux paramètres
		String nvNom = nom == null ? oldEven.getNom() : nom;
		Date nvDateDebut = dateDebut == null ? oldEven.getDateDebut() : dateDebut;
		Date nvDateFin = dateFin == null ? oldEven.getDateFin() : dateFin;
		List<Integer> nvIdCalendriers = idCalendriers == null ? oldEven.getIdCalendriers() : idCalendriers;
		ArrayList<Integer> nvIdSalles = idSalles == null ? getIdSalles(oldEven.getSalles()) : idSalles;
		ArrayList<Integer> nvIdIntervenants = idIntervenants == null ? getUserIds(oldEven.getIntervenants()) : idIntervenants;
		ArrayList<Integer> nvIdResponsables = idResponsables == null ? getUserIds(oldEven.getResponsables()) : idResponsables;
		
		evenementGestion.modifierEvenement(oldEven.getId(), nvNom, nvDateDebut, nvDateFin, nvIdCalendriers, nvIdSalles, nvIdIntervenants, nvIdResponsables, false);
		
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evènement modifié", null));
	}
	
	protected static ArrayList<Integer> getIdSalles(List<SalleIdentifie> salles) {
		ArrayList<Integer> res = new ArrayList<Integer>(salles.size());
		
		for(SalleIdentifie s : salles) {
			res.add(s.getId());
		}
		
		return res;
	}
	
	protected static ArrayList<Integer> getUserIds(List<UtilisateurIdentifie> utilisateurs) {
		ArrayList<Integer> res = new ArrayList<Integer>(utilisateurs.size());
		
		for(UtilisateurIdentifie u : utilisateurs) {
			res.add(u.getId());
		}
		
		return res;
	}
}
