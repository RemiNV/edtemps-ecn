package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;

import javax.json.JsonException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.JSONUtils.EvenementClientRepetition;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CalendrierGestion;
import org.ecn.edtemps.managers.CalendrierGestion.DroitsCalendriers;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.models.TestRepetitionEvenement;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class RepeterEvenementServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(RepeterEvenementServlet.class.getName());

	private static final long serialVersionUID = -3058678054940040170L;

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Requête GET /previsualiser supportée uniquement
		if(!req.getPathInfo().equals("/previsualiser")) {
			super.doGetAfterLogin(userId, bdd, req, resp);
			bdd.close();
			return;
		}
		
		try {
			// Récupération des paramètres
			int idEvenement = getIntParam(req, "idEvenement");
			int nbRepetitions = getIntParam(req, "nbRepetitions");
			int periode = getIntParam(req, "periode");
			
			EvenementGestion evenementGestion = new EvenementGestion(bdd);
			
			ArrayList<TestRepetitionEvenement> res = evenementGestion.testRepetitionEvenement(idEvenement, nbRepetitions, periode, true);
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Répétition calculée", 
					JSONUtils.getJsonArray(res)));
		}
		catch(EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors de la prévisualisation de répétition d'événement", e);
		}
		
		
		bdd.close();
	}
	
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Requête POST /executer supportée uniquement
		if(!req.getPathInfo().equals("/executer")) {
			super.doPostAfterLogin(userId, bdd, req, resp);
			bdd.close();
			return;
		}
		
		String strEvenements = req.getParameter("evenements");
		
		if(strEvenements == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre \"evenements\" manquant", null));
			bdd.close();
			return;
		}
		
		try {
			// Toute la procédure est effectuée dans une transaction
			bdd.startTransaction();
			EvenementGestion evenementGestion = new EvenementGestion(bdd);
			
			// Récupération des événements à répéter
			ArrayList<EvenementClientRepetition> evenements = JSONUtils.getEvenementsClientRepetition(strEvenements);
			
			int idEvenementRepetition = getIntParam(req, "idEvenementRepetition");
			EvenementIdentifie evenementRepetition = evenementGestion.getEvenement(idEvenementRepetition);
			
			// Vérification que l'utilisateur est propriétaire de l'événement
			// TODO : écrire !
			
			int idCalendrier = getIntParam(req, "idCalendrier");
			ArrayList<Integer> lstIdCalendrier = new ArrayList<Integer>(1);
			lstIdCalendrier.add(idCalendrier);
			
			// Vérification des droits de l'utilisateur sur le calendrier
			CalendrierGestion calendrierGestion = new CalendrierGestion(bdd);
			DroitsCalendriers droitsCalendrier = calendrierGestion.getDroitsCalendriers(userId, lstIdCalendrier);
			
			if(!droitsCalendrier.estProprietaire) {
				bdd.rollback();
				throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Vous n'êtes pas propriétaire du calendrier");
			}
			
			// Ajout de chacun des événements
			for(EvenementClientRepetition evenement : evenements) {
				
				// Libération des salles occupées par un non-cours
				if(evenement.idEvenementsSallesALiberer != null && !evenement.idEvenementsSallesALiberer.isEmpty()) {
					
					if(!droitsCalendrier.contientCours) {
						bdd.rollback();
						throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Le calendrier n'est pas un calendrier de cours : impossible de prendre une salle déjà occupée");
					}
					
					evenementGestion.supprimerSallesEvenementsNonCours(evenement.idSalles, evenement.idEvenementsSallesALiberer);
				}
				
				// Utilisation des salles de l'événement d'origine si pas de changement précisé
				ArrayList<Integer> salles = evenement.idSalles == null ? SalleGestion.getIdSalles(evenementRepetition.getSalles()) : evenement.idSalles; 
				
				evenementGestion.sauverEvenement(evenementRepetition.getNom(), evenement.dateDebut, evenement.dateFin, 
						lstIdCalendrier, userId, salles, UtilisateurGestion.getUserIds(evenementRepetition.getIntervenants()), 
								UtilisateurGestion.getUserIds(evenementRepetition.getResponsables()), false);
			}
			
			bdd.commit();
			
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Evénements ajoutés", null));
		}
		catch(JsonException | ClassCastException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format du paramètre \"evenements\" incorrect", null));
			logger.warn("Paramètres invalides pour la requête de répétition", e);
		}
		catch(EdtempsException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur lors d'une requête de répétition", e);
		}
		
		bdd.close();
	}
	
	protected static int getIntParam(HttpServletRequest req, String nom) throws EdtempsException {
		String strParam = req.getParameter(nom);
		try {
			return Integer.parseInt(strParam);
		}
		catch(NumberFormatException e) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre " + nom + " incorrect ou absent", e);
		}
	}
}
