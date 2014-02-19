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
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

/**
 * Servlet pour la gestion des utilisateurs
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurUtilisateurServlet extends HttpServlet {

	private static final long serialVersionUID = 9004489480096956740L;
	private static Logger logger = LogManager.getLogger(AdministrateurUtilisateurServlet.class.getName());
	
	/**
	 * Servlet pour la gestion des utilisateurs
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/modifiertype") && !pathInfo.equals("/desactiver") && !pathInfo.equals("/activer")) {
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
				case "/modifiertype":
					doModifierType(req, resp);
					break;
				case "/activer":
					doChangerStatut(true, req, resp);
					break;
				case "/desactiver":
					doChangerStatut(false, req, resp);
					break;
			}
		} catch (NumberFormatException e) {
			logger.error("Erreur de cast des paramètres", e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (EdtempsException e) {
			logger.error("Erreur du gestionnaire", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	
	
	/**
	 * Modifier le type d'un utilisateur
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doModifierType(HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {
		logger.info("Modification d'un type d'un utilisateur");

		// Récupération des valeurs du formulaire
		Integer userId = req.getParameter("modifier_utilisateur_id")!="" ? Integer.valueOf(req.getParameter("modifier_utilisateur_id")) : null;
		if (userId == null) {
			logger.error("Identifiant de l'utilisateur erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		String[] listeTypes = req.getParameterValues("modifier_utilisateur_types");

		// Récupération des droits
		List<Integer> types = new ArrayList<Integer>();
		if (listeTypes!=null) {
			List<String> listeTypesStr = Arrays.asList(listeTypes);
			for (String idType : listeTypesStr) {
				types.add(Integer.valueOf(idType));
			}
		}
		
		// Exécute la requête de modification avec le manager
		BddGestion bdd = new BddGestion();
		UtilisateurGestion gestionnaireUtilisateurs = new UtilisateurGestion(bdd);
		gestionnaireUtilisateurs.modifierTypeUtilisateur(userId, types);
		bdd.close();

		// Redirige vers la page de liste des utilisateurs
		resp.sendRedirect(req.getContextPath()+"/admin/utilisateurs/index.jsp");
	}


	/**
	 * Activer/Désactiver un utilisateur
	 * @param valide VRAI si l'utilisateur doit être activé
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doChangerStatut(boolean valide, HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {
		logger.info("Activation/Désactivation un utilisateur");

		// Récupération des valeurs du formulaire
		Integer id = req.getParameter("id")!="" ? Integer.valueOf(req.getParameter("id")) : null;
		if (id == null) {
			logger.error("Identifiant de l'utilisateur erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

		// Exécute la requête de modification avec le manager
		BddGestion bdd = new BddGestion();
		UtilisateurGestion gestionnaireUtilisateurs = new UtilisateurGestion(bdd);
		gestionnaireUtilisateurs.desactiverUtilisateur(id, valide);
		bdd.close();

		// Redirige vers la page de liste des utilisateurs
		resp.sendRedirect(req.getContextPath()+"/admin/utilisateurs/index.jsp");
	}

}
