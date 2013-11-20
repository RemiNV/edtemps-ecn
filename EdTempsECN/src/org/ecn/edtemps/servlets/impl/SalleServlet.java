package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.SalleGestion;

/**
 * Servlet pour la gestion des salles
 * 
 * @author Joffrey Terrade
 */
public class SalleServlet extends HttpServlet {

	private static final long serialVersionUID = 2760466483465073439L;
	private static Logger logger = LogManager.getLogger(SalleServlet.class.getName());
	
	/**
	 * Servlet pour la gestion des salles
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/supprimer") && !pathInfo.equals("/modifier") ) {
			logger.error("Les seules méthodes acceptées sont la modification et la suppression");
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
				case "/supprimer":
					doSupprimer(req, resp);
					break;
			}
		} catch (NumberFormatException e) {
			logger.error("Erreur de cast des paramètres");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (DatabaseException e) {
			logger.error("Erreur avec la base de données");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	
	
	/**
	 * Supprimer une salle
	 * @param req Requête
	 * @param resp Réponse
	 * @throws DatabaseException 
	 * @throws IOException 
	 */
	public void doSupprimer(HttpServletRequest req, HttpServletResponse resp) throws DatabaseException, IOException {
		logger.error("Suppression d'une salle");

		// Récupération de l'identifiant de la salle à supprimer dans la requête
		int id = Integer.valueOf(req.getParameter("id"));
		
		// Exécute la requête de suppression avec le manager
		BddGestion bdd = new BddGestion();
		SalleGestion gestionnaireSalles = new SalleGestion(bdd);
		gestionnaireSalles.supprimerSalle(id);
		bdd.close();
		
		// Redirige vers la page de liste des salles
		resp.sendRedirect(req.getContextPath()+"/admin/salles/index.jsp");
	}
	
}
