package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.JSONUtils.EvenementClient;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.CalendrierGestion.DroitsCalendriers;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de gestion des événements
 * 
 * @author Joffrey
 */
public class EvenementServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 85479515540354619L;
	private static Logger logger = LogManager.getLogger(EvenementServlet.class.getName());
	
	
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		
		try {
			switch(pathInfo) {
			case "/ajouter":
				doAjouterModifier(userId, bdd, req, resp, false);
				break;
			case "/modifier":
				doAjouterModifier(userId, bdd, req, resp, true);
				break;
			case "/supprimer":
				doSupprimer(userId, bdd, req, resp);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				bdd.close();
				return;
			}
		}
		catch(EdtempsException e) {
			logger.error("Erreur lors de l'ajout/modification/suppression d'un évènement ; requête " + pathInfo, e);
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			bdd.close();
		}
	}
	
	protected void doSupprimer(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, EdtempsException {
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		String strIdEvenement = req.getParameter("idEvenement");
		if(strIdEvenement == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idEvenement non fourni pour une suppression d'événement");
		}
		
		int idEvenement = Integer.parseInt(strIdEvenement);
		EvenementIdentifie evenement = evenementGestion.getEvenement(idEvenement);
		
		if(!UtilisateurGestion.getUserIds(evenement.getResponsables()).contains(userId)) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Tentative de supprimer un événement sans être propriétaire");
		}
		
		evenementGestion.supprimerEvenement(idEvenement, true);
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evénement supprimé", null));
		bdd.close();
	}
	
	protected void doAjouterModifier(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp, boolean estModif) throws ServletException, IOException, 
		EdtempsException {
		
		// Récupération des infos de l'objet évènement
		String strEvenement = req.getParameter("evenement");
		
		if(strEvenement == null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet événement manquant");
		}
		
		try {
			EvenementClient paramsEvenement = JSONUtils.getEvenementClient(strEvenement);
			
			EvenementGestion evenementGestion = new EvenementGestion(bdd);

			bdd.startTransaction();
			
			// Récupération de l'ancien évènement (pour les modifications)
			EvenementIdentifie oldEven = null;
			if(estModif) {
				if(paramsEvenement.idEvenement == null) {
					throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "ID d'événement non précisé pour modification");
				}
				oldEven = evenementGestion.getEvenement(paramsEvenement.idEvenement);
				
				if(oldEven == null) {
					logger.warn("ID d'évènement inexistant fourni pour l'ajout d'un évènement");
					throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Aucun événement correspondant à l'ID fourni");
				}
			}
			
			// Vérification des droits de l'utilisateur sur les calendriers
			CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
			if(paramsEvenement.idCalendriers == null && !estModif) {
				throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Vous devez préciser des calendriers pour ajouter un événement");
			}
			
			// Droits sur les calendriers de l'événement avant modification (pas de modification d'un calendrier qui n'appartient pas à l'utilisateur)
			List<Integer> idCalendriersDroitsAncien = estModif ? oldEven.getIdCalendriers() : paramsEvenement.idCalendriers;
			DroitsCalendriers droitsCalendriersAncien = calendrierGestion.getDroitsCalendriers(userId, idCalendriersDroitsAncien);
			
			// Droits sur les calendriers de l'événement après modification (détermination du statut "cours" de l'événement)
			DroitsCalendriers droitsCalendriersNouveau;
			if(estModif && paramsEvenement.idCalendriers != null) {
				droitsCalendriersNouveau = calendrierGestion.getDroitsCalendriers(userId, paramsEvenement.idCalendriers);
			}
			else {
				droitsCalendriersNouveau = droitsCalendriersAncien; // Pas de modification des calendriers donc des droits
			}
			
			// TODO : autoriser un administrateur à faire ceci
			// Si modification des calendriers : doit être propriétaire des anciens et des nouveaux (aussi pour création de l'événement)
			if(paramsEvenement.idCalendriers != null && (!droitsCalendriersNouveau.estProprietaire || !droitsCalendriersAncien.estProprietaire)) {
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Vous n'êtes pas propriétaire de tous les calendriers de l'évènement");
			}
			
			// Le créateur de l'événement doit être responsable
			// TODO : autoriser ceci pour utilisateur avec les droits suffisants
			Integer idCreateur = estModif ? oldEven.getIdCreateur() : userId;
			if(paramsEvenement.idResponsables != null && idCreateur != null && !paramsEvenement.idResponsables.contains(idCreateur)) {
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Le créateur de l'événement doit être un des propriétaires");
			}
			
			// Libération des salles déjà occupées pour un cours
			// TODO : avertir le propriétaire d'un évènement quand la salle qu'il a renseignée est libérée par un nouvel évènement
			if(paramsEvenement.idEvenementsSallesALiberer != null && !paramsEvenement.idEvenementsSallesALiberer.isEmpty()) {
				
				if(!droitsCalendriersNouveau.contientCours) {
					bdd.rollback();
					throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Le calendrier n'est pas un calendrier de cours : impossible de prendre une salle déjà occupée");
				}
				
				evenementGestion.supprimerSallesEvenementsNonCours(paramsEvenement.idSalles, paramsEvenement.idEvenementsSallesALiberer);
			}
			
			if(estModif) { // Requête /evenement/modifier
				doModifierEvenement(userId, bdd, resp, paramsEvenement, oldEven);
			}
			else { // Requête /evenement/ajouter
				doAjouterEvenement(userId, bdd, resp, paramsEvenement);
			}
			
			bdd.commit();
			bdd.close();
		}
		catch(JsonException | ClassCastException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Erreur de lecture de l'objet JSON fourni", e);
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
	protected void doAjouterEvenement(int userId, BddGestion bdd, HttpServletResponse resp, EvenementClient params) throws EdtempsException, IOException {
		
		// Vérification des paramètres
		if(params.nom == null || StringUtils.isBlank(params.nom) || params.dateDebut == null || params.dateFin == null || params.idCalendriers == null || params.idSalles == null 
				|| params.idIntervenants == null || params.idResponsables == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet evenenement incomplet : paramètres manquants", null));
			logger.warn("Objet d'évènement fourni avec des paramètres manquants lors de l'ajout");
			return;
		}
		
		// Vérification que l'utilisateur n'a pas atteint son quota
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		evenementGestion.sauverEvenement(params.nom, params.dateDebut, params.dateFin, params.idCalendriers, userId, params.idSalles, 
				params.idIntervenants, params.idResponsables, false);

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
	protected void doModifierEvenement(int userId, BddGestion bdd, HttpServletResponse resp, 
			EvenementClient params, EvenementIdentifie oldEven) throws IOException, EdtempsException {
		
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		ArrayList<Integer> oldIdsResponsables = UtilisateurGestion.getUserIds(oldEven.getResponsables());
		
		// TODO : autoriser un administrateur à faire ceci
		// Vérification que l'utilisateur est autorisé à modifier cet évènement
		if(!oldIdsResponsables.contains(userId)) {
			bdd.rollback();
			throw new EdtempsException(ResultCode.IDENTIFICATION_ERROR, "Vous n'êtes pas responsable de cet évènement");
		}
		
		// Génération des nouveaux paramètres
		String nvNom = params.nom == null ? oldEven.getNom() : params.nom;
		Date nvDateDebut = params.dateDebut == null ? oldEven.getDateDebut() : params.dateDebut;
		Date nvDateFin = params.dateFin == null ? oldEven.getDateFin() : params.dateFin;
		List<Integer> nvIdCalendriers = params.idCalendriers == null ? oldEven.getIdCalendriers() : params.idCalendriers;
		ArrayList<Integer> nvIdSalles = params.idSalles == null ? SalleGestion.getIdSalles(oldEven.getSalles()) : params.idSalles;
		ArrayList<Integer> nvIdIntervenants = params.idIntervenants == null ? UtilisateurGestion.getUserIds(oldEven.getIntervenants()) : params.idIntervenants;
		ArrayList<Integer> nvIdResponsables = params.idResponsables == null ? UtilisateurGestion.getUserIds(oldEven.getResponsables()) : params.idResponsables;
		
		evenementGestion.modifierEvenement(oldEven.getId(), nvNom, nvDateDebut, nvDateFin, nvIdCalendriers, nvIdSalles, nvIdIntervenants, nvIdResponsables, false);
		
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evènement modifié", null));
	}
}
