package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.AdministrateurGestion;
import org.ecn.edtemps.managers.BddGestion;

/**
 * Servlet pour la connexion en grand administrateur
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurServlet extends HttpServlet {

	private static final long serialVersionUID = 5303879641965806292L;
	private static Logger logger = LogManager.getLogger(AdministrateurServlet.class.getName());

	/**
	 * Servlet pour la connexion et la déconnexion en mode grand administrateur
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/connexion") && !pathInfo.equals("/ajouter") && !pathInfo.equals("/supprimer")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		HttpSession session = req.getSession(true);

		try {
			switch (pathInfo) {
				case "/connexion":
					doConnexion(req, resp, session);
					break;
				case "/ajouter":
					doAjouter(req, resp, session);
					break;
				case "/supprimer":
					doSupprimer(req, resp, session);
					break;
			}
		} catch (IOException | ServletException e) {
			logger.error("Erreur lors de l'écriture de la réponse");
			session.setAttribute("connect", "KO");
			resp.sendRedirect("../admin/login.jsp");
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			logger.error("Erreur lors du cryptage du mot de passe");
			session.setAttribute("connect", "KO");
			resp.sendRedirect("../admin/login.jsp");
		} catch (DatabaseException e) {
			logger.error("Erreur liée à la base de données");
			session.setAttribute("connect", "KO");
			resp.sendRedirect("../admin/login.jsp");
		}
		
	}
	
	
	/**
	 * Connecter mode Administrateur
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @param session Session HTTP
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws ServletException 
	 */
	protected void doConnexion(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws IOException, InvalidKeyException, NoSuchAlgorithmException, DatabaseException, ServletException {
		
		// Récupération des identifiants passés en paramètre
		String login = req.getParameter("login");
		String password = req.getParameter("password");
		logger.debug("Tentative de connexion à l'espace d'administration avec le login : " + login);
		
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateur = new AdministrateurGestion(bdd);
		
		if (gestionnaireAdministrateur.seConnecter(login, password)) {
			logger.error("Connexion réussie");
			session.setAttribute("connect", "OK");
			session.setAttribute("login", login);
			resp.sendRedirect("../admin/general.jsp");
		} else {
			logger.error("Echec de connexion : identifiant ou mot de passe erroné.");
			session.setAttribute("connect", "KO");
			session.setAttribute("login", "");
			resp.sendRedirect("../admin/login.jsp");
		}

	}
	
	
	/**
	 * Ajouter un administrateur 
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @param session Session HTTP
	 * @throws DatabaseException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	protected void doAjouter(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws DatabaseException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		logger.info("Ajout d'un administrateur");

		// Récupération du login et du mot de passe
		String login = req.getParameter("ajouter_administrateur_login");
		String password = req.getParameter("ajouter_administrateur_password");

		// Si le login ou mot de passe est vide, une erreur est générée
		if (StringUtils.isBlank(login) || StringUtils.isBlank(password)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute la requête d'ajout avec le manager
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateur = new AdministrateurGestion(bdd);
		gestionnaireAdministrateur.ajouterAdministrateur(login, password);
		bdd.close();

		// Redirige vers la page de liste des administrateurs
		resp.sendRedirect(req.getContextPath()+"/admin/administrateurs/index.jsp");
	}
	

	/**
	 * Supprimer un administrateur 
	 * @param resp Réponse à compléter
	 * @param requete Requête
	 * @param session Session HTTP
	 * @throws DatabaseException 
	 * @throws IOException 
	 */
	protected void doSupprimer(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws DatabaseException, IOException {
		logger.info("Suppression d'un administrateur");

		// Récupération de l'identifiant de l'administrateur à supprimer
		Integer id = req.getParameter("supprimer_administrateur_id")!="" ? Integer.valueOf(req.getParameter("supprimer_administrateur_id")) : null;
		if (id == null) {
			logger.error("Identifiant de l'administrateur erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute la requête de suppression avec le manager
		BddGestion bdd = new BddGestion();
		AdministrateurGestion gestionnaireAdministrateur = new AdministrateurGestion(bdd);
		gestionnaireAdministrateur.supprimerAdministrateur(id);
		bdd.close();
		
		// Redirige vers la page de liste des types de matériel
		resp.sendRedirect(req.getContextPath()+"/admin/administrateurs/index.jsp");
	}


}
