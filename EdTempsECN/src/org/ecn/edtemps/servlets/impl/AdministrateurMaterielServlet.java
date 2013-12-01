package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.MaterielGestion;

/**
 * Servlet pour la gestion des types de matériel
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurMaterielServlet extends HttpServlet {

	private static final long serialVersionUID = 1980494688232908447L;
	private static Logger logger = LogManager.getLogger(AdministrateurMaterielServlet.class.getName());
	
	
	/**
	 * Servlet pour la gestion des types de matériel
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/supprimer") ) {
			logger.error("Les seules méthodes acceptées sont l'ajout et la suppression");
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
				case "/supprimer":
					doSupprimer(req, resp);
					break;
			}
		} catch (NumberFormatException e) {
			logger.error("Erreur de cast des paramètres");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (EdtempsException e) {
			logger.error("Erreur du gestionnaire de matériel");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	
	
	/**
	 * Supprimer un type matériel
	 * @param req Requête
	 * @param resp Réponse
	 * @throws DatabaseException 
	 * @throws IOException 
	 */
	public void doSupprimer(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.error("Suppression d'un type matériel");

		// Récupération de l'identifiant du type de matériel à supprimer
		Integer id = req.getParameter("supprimer_materiel_id")!="" ? Integer.valueOf(req.getParameter("supprimer_materiel_id")) : null;
		if (id == null) {
			logger.error("Identifiant du type de matériel erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute la requête de suppression avec le manager
		BddGestion bdd = new BddGestion();
		MaterielGestion gestionnaireMateriel = new MaterielGestion(bdd);
		gestionnaireMateriel.supprimerMateriel(id);
		bdd.close();
		
		// Redirige vers la page de liste des types de matériel
		resp.sendRedirect(req.getContextPath()+"/admin/materiel/index.jsp");
	}
	

	/**
	 * Ajouter un type matériel
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doAjouter(HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {
		logger.error("Ajout d'un type de matériel");

		// Récupération du nom
		String nom = req.getParameter("ajouter_materiel_nom");

		// Exécute la requête d'ajout avec le manager
		BddGestion bdd = new BddGestion();
		MaterielGestion gestionnaireMateriel = new MaterielGestion(bdd);
		gestionnaireMateriel.sauverMateriel(nom);
		bdd.close();

		// Redirige vers la page de liste des types de matériel
		resp.sendRedirect(req.getContextPath()+"/admin/materiel/index.jsp");
	}
	
}
