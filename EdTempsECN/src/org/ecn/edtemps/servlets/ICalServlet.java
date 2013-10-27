package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.ICalGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

public class ICalServlet extends TokenServlet {

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
			resp.getWriter().write("Erreur de génération du calendrier ICal. Code : " + e.getResultCode() + " message : " + e.getMessage());
			
			System.out.println("Erreur de génération de calendrier ICal");
			e.printStackTrace();
		}
		
		bdd.close();
	}
	
}
