package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.ICalGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;
import org.ecn.edtemps.servlets.TokenServlet;

/**
 * Servlet pour l'export ICal
 * 
 * @author Remi
 */
public class ICalServlet extends TokenServlet {

	private static final long serialVersionUID = 8276900152894529829L;
	private static Logger logger = LogManager.getLogger(ICalServlet.class.getName());
	
	
	/**
	 * L'utilisateur est identifié par son token iCal ici, pas par son token de connexion classique
	 */
	@Override
	protected int verifierToken(BddGestion bdd, String token) throws IdentificationException, DatabaseException {
		UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
		return utilisateurGestion.verifierTokenIcal(token);
	}

	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		resp.setContentType("text/calendar");
		
		ICalGestion icalGestion = new ICalGestion(bdd);
		
		
		try {
			resp.getWriter().write(icalGestion.genererICalAbonnements(userId));
		} catch (DatabaseException e) {
			logger.error("Erreur de génération du calendrier ICal", e);
			resp.getWriter().write("Erreur de génération du calendrier ICal. Code : " + e.getResultCode() + " message : " + e.getMessage());
		}
		
		bdd.close();
	}
	
}
