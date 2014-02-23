package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

/**
 * Servlet de déconnexion
 * 
 * @author Joffrey
 */
public class DisconnectServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 5457172146861925749L;
	private static Logger logger = LogManager.getLogger(DisconnectServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
			utilisateurGestion.seDeconnecter(userId);
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Utilisateur déconnecté", null));
			bdd.close();
		} catch (DatabaseException e) {
			bdd.close();
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.DATABASE_ERROR, e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la déconnexion d'un utilisateur", e);
		}
	}
}
