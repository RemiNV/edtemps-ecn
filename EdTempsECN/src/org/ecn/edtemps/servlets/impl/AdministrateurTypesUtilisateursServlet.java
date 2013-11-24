package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;

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
		if (!pathInfo.equals("/ajouter")) {
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
		
		// Redirige vers la page de liste
		resp.sendRedirect(req.getContextPath()+"/admin/typesutilisateurs/index.jsp");
	}
	
	
}
