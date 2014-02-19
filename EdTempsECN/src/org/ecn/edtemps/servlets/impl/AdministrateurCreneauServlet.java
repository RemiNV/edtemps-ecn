package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.sql.Time;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.CreneauGestion;
import org.ecn.edtemps.models.Creneau;
import org.ecn.edtemps.models.identifie.CreneauIdentifie;

/**
 * Servlet pour la gestion des créneaux
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurCreneauServlet extends HttpServlet {

	private static final long serialVersionUID = -3540266400253594679L;
	private static Logger logger = LogManager.getLogger(AdministrateurCreneauServlet.class.getName());
	
	/**
	 * Servlet pour la gestion des créneaux
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/ajoutermodifier") && !pathInfo.equals("/supprimer") ) {
			logger.error("Les seules méthodes acceptées sont l'ajoutmodification et la suppression");
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
				case "/ajoutermodifier":
					doAjouterModifier(req, resp);
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
			logger.error("Erreur du gestionnaire de créneaux");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	
	
	/**
	 * Supprimer un créneau
	 * @param req Requête
	 * @param resp Réponse
	 * @throws EdtempsException 
	 * @throws IOException 
	 */
	public void doSupprimer(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.info("Suppression d'un créneau");

		// Récupération de l'identifiant
		String strIdCreneau = req.getParameter("idCreneau");
		Integer idCreneau = StringUtils.isNotBlank(strIdCreneau) ? Integer.valueOf(strIdCreneau) : null;
		if (idCreneau == null) {
			logger.error("Identifiant du créneau erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute la requête de suppression
		BddGestion bdd = new BddGestion();
		CreneauGestion gestionnaire = new CreneauGestion(bdd);
		gestionnaire.supprimer(idCreneau);
		bdd.close();
		
		// Redirige vers la page listing
		resp.sendRedirect(req.getContextPath()+"/admin/general.jsp");
	}
	

	/**
	 * Ajouter/Modifier un créneau
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doAjouterModifier(HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {

		// Récupération des valeurs du formulaire
		Integer idCreneau = StringUtils.isNotBlank(req.getParameter("idCreneau")) ? Integer.valueOf(req.getParameter("idCreneau")) : null;
		String libelleCreneau = req.getParameter("libelleCreneau");
		Time debutCreneau = StringUtils.isNotBlank(req.getParameter("debutCreneau")) ? new Time(Long.valueOf(req.getParameter("debutCreneau"))) : null;
		Time finCreneau = StringUtils.isNotBlank(req.getParameter("finCreneau")) ? new Time(Long.valueOf(req.getParameter("finCreneau"))) : null;
		if (debutCreneau == null || finCreneau == null) {
			logger.error("Les horaires de début et de fin ne sont pas corrects");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute l'ajout ou la modification
		BddGestion bdd = new BddGestion();
		CreneauGestion gestionnaire = new CreneauGestion(bdd);
		if (idCreneau == null) {
			gestionnaire.creer(new Creneau(libelleCreneau, debutCreneau, finCreneau));
			logger.info("Ajout d'un créneau");
		} else {
			gestionnaire.modifier(new CreneauIdentifie(idCreneau, libelleCreneau, debutCreneau, finCreneau));
			logger.info("Modification d'un créneau");
		}
		bdd.close();

		// Redirige vers la page listing
		resp.sendRedirect(req.getContextPath()+"/admin/general.jsp");
	}

}
