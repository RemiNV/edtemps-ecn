package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

/**
 * Servlet de base à étendre pour toutes les requêtes nécessitant l'identification de l'utilisateur
 * @author Remi
 *
 */
public abstract class RequiresConnectionServlet extends HttpServlet {

	public static enum SupportedMethods {
		GET,
		POST;
	}
	
	private void doMethod(SupportedMethods method, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		// Vérification de la présence du token
		String token = req.getParameter("token");
		
		ResultCode result;
		String message;
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		
		if(StringUtils.isBlank(token)) {
			result = ResultCode.IDENTIFICATION_ERROR;
			message = "Token de connexion non fourni";
		}
		else {
			try {
				UtilisateurGestion utilisateurGestion = new UtilisateurGestion(new BddGestion());
				int userId = utilisateurGestion.verifierConnexion(token);
				
				// Succès de la connexion
				switch(method) {
				case GET:
					doGetAfterLogin(userId, req, resp);
					break;
				case POST:
					doPostAfterLogin(userId, req, resp);
					break;
				}
				return;
				
			} catch (IdentificationException | DatabaseException e) {
				result = e.getResultCode();
				message = e.getMessage();
			}
		}
		
		// Erreur quelconque
		resp.getWriter().write(ResponseManager.generateResponse(result, message, null));
	}
	
	// Surclasser la méthode doPostAfterLogin à la place de celle-ci
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		doMethod(SupportedMethods.POST, req, resp);
	}
	
	// Surclasser la méthode doGetAFterLogin à la place de celle-ci
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		doMethod(SupportedMethods.GET, req, resp);
	}
	
	/**
	 * Gestion des requêtes POST après vérification du login. Surclasser cette méthode à la place de doPost()
	 * @param userId ID de l'utilisateur qui effectue la requête
	 * @param req HttpServletRequest fourni par doGet
	 * @param resp HttpServletResponse fourni par doPost, initialisé avec un ContentType application/json et charset utf-8
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPostAfterLogin(int userId, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}
	
	/**
	 * Gestion des requêtes GET après vérification du login. Surclasser cette méthode à la place de doGet()
	 * @param userId ID de l'utilisateur qui effectue la requête
	 * @param req HttpServletRequest fourni par doGet
	 * @param resp HttpServletResponse fourni par doPost, initialisé avec un ContentType application/json et charset utf-8
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGetAfterLogin(int userId, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}
}
