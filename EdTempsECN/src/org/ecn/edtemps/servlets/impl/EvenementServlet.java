package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class EvenementServlet extends RequiresConnectionServlet {

	
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String pathInfo = req.getPathInfo();
		
		if(!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			bdd.close();
			return;
		}
		
		// Récupération des infos de l'objet évènement
		
		
		if(pathInfo.equals("/ajouter")) { // Page /abonnements/
			doAjouterEvenement(userId, bdd, resp);
		}
		else if(pathInfo.equals("/modifier")) { // Récupération uniquement des évènements, page /abonnements/evenements
			
			// TODO : compléter
			// int idEvenement = ...
			
			// doModifierEvenement(
		}
		
		
		
		bdd.close();
	}
	
	
	
	protected void doAjouterEvenement(int userId, BddGestion bdd, HttpServletResponse resp) {
		
		// Récupération des paramètres de l'évènement
		
	}
	
	
	protected void doModifierEvenement(int userId, BddGestion bdd, HttpServletResponse resp) throws IOException {
		// TODO : remplir
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
