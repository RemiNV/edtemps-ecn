package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.servlets.RequiresConnectionServlet;

public class EvenementServlet extends RequiresConnectionServlet {

	
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		
		
		
		bdd.close();
	}
	
	
	
	protected void doAjouterEvenement(int userId, BddGestion bdd, HttpServletResponse resp) {
		
		// Récupération des paramètres de l'évènement
		
	}
}
