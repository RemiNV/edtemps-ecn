package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.AdministrateurGestion;
import org.ecn.edtemps.managers.BddGestion;

/**
 * Servlet pour la gestion des types d'utilisateurs
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurTypesUtilisateursServlet extends HttpServlet {

	private static final long serialVersionUID = 2760466483465073439L;
	private static Logger logger = LogManager.getLogger(AdministrateurTypesUtilisateursServlet.class.getName());
	
	/**
	 * Servlet pour la gestion des types d'utilisateurs
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifierDroits") && !pathInfo.equals("/supprimer")) {
			logger.error("Méthode non acceptée pour cette servlet");
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		HttpSession session = req.getSession(true);

		// Vérifie que l'utilisateur est bien connecté
		if (session.getAttribute("connect")!="OK") {
			logger.error("L'utilisateur n'est pas connecté");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		try {
			switch (pathInfo) {
				case "/ajouter":
					doAjouter(req, resp);
					break;
				case "/modifierDroits":
					doModifierDroits(req, resp);
					break;
				case "/supprimer":
					doSupprimer(req, resp);
					break;
			}
		} catch (NumberFormatException e) {
			logger.error("Erreur de cast des paramètres");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (EdtempsException e) {
			logger.error("Erreur du gestionnaire");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	

	/**
	 * Ajouter un type d'utilisateur
	 * @param req Requête
	 * @param resp Réponse
	 * @throws EdtempsException 
	 * @throws IOException 
	 */
	public void doAjouter(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.error("Ajouter un type d'utilisateur");

		// Récupération de l'identifiant de la salle à supprimer dans la requête
		String nom = req.getParameter("ajouter_type_utilisateurs_nom");

		// Ajoute le type d'utilisateurs
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateurs = new AdministrateurGestion(bdd);
		gestionnaireAdministrateurs.ajouterTypeUtilisateurs(nom);
		
		// Redirige vers la page de liste
		resp.sendRedirect(req.getContextPath()+"/admin/typesutilisateurs/index.jsp");
	}

	
	/**
	 * Modifier les droits d'un type d'utilisateur
	 * @param req Requête
	 * @param resp Réponse
	 * @throws EdtempsException 
	 * @throws IOException 
	 */
	public void doModifierDroits(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.error("Modifier les droits d'un type d'utilisateur");
		
		// Récupération des données du formulaire
		int id = Integer.valueOf(req.getParameter("modifier_type_utilisateurs_form_id"));
		String nom = req.getParameter("modifier_type_utilisateurs_form_nom");
		String[] listeDroits = req.getParameterValues("modifier_type_utilisateurs_form_droits");

		// Récupération des droits
		List<Integer> droits = new ArrayList<Integer>();
		if (listeDroits!=null) {
			List<String> droitsStr = Arrays.asList(listeDroits);
			for (String droit : droitsStr) {
				droits.add(Integer.valueOf(droit));
			}
		}
		
		// Supprime le type d'utilisateurs
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateurs = new AdministrateurGestion(bdd);
		gestionnaireAdministrateurs.modifierDroitsTypeUtilisateurs(id, droits, nom);

		// Redirige vers la page de liste
		resp.sendRedirect(req.getContextPath()+"/admin/typesutilisateurs/index.jsp");
	}
	

	/**
	 * Supprimer un type d'utilisateurs
	 * @param req Requête
	 * @param resp Réponse
	 * @throws EdtempsException 
	 * @throws IOException 
	 */
	public void doSupprimer(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.error("Supprimer un type d'utilisateur");

		// Récupération de l'identifiant du type à supprimer
		int id = Integer.valueOf(req.getParameter("supprimer_types_utilisateurs_id"));
		
		// Supprime le type d'utilisateurs
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateurs = new AdministrateurGestion(bdd);
		gestionnaireAdministrateurs.supprimerTypeUtilisateurs(id);
		
		// Redirige vers la page de liste
		resp.sendRedirect(req.getContextPath()+"/admin/typesutilisateurs/index.jsp");
	}
	
}
