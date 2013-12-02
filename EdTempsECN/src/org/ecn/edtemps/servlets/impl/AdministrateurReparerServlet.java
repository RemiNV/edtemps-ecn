package org.ecn.edtemps.servlets.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.diagnosticbdd.DiagnosticsBdd;
import org.ecn.edtemps.diagnosticbdd.TestBdd;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

public class AdministrateurReparerServlet extends HttpServlet {

	private static final long serialVersionUID = -3009615295776498154L;
	private static Logger logger = LogManager.getLogger(AdministrateurReparerServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		HttpSession session = req.getSession(true);

		// Vérifie que l'utilisateur est bien connecté
		if (session.getAttribute("connect")!="OK") {
			logger.error("L'utilisateur n'est pas connecté");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		try {
			BddGestion bdd = new BddGestion();
			
			DiagnosticsBdd diagnostics = new DiagnosticsBdd(bdd);
			
			String idDiagnostic = req.getParameter("id");
			Integer id = null;
			if(idDiagnostic != null) {
				try {
					id = Integer.parseInt(idDiagnostic);
				}
				catch(NumberFormatException e) { } // id == null
			}
			
			TestBdd testBdd = null;
			if(id != null) {
				testBdd = diagnostics.createTest(id);
			}
			
			String resultatReparation = null;
			if(testBdd != null) {
				resultatReparation = testBdd.repair(bdd);
			}
			
			bdd.close();
			
			req.setAttribute("test", testBdd);
			req.setAttribute("resultatReparation", resultatReparation);
			
		} catch (DatabaseException e) {
			req.setAttribute("test", null);
			req.setAttribute("resultatReparation", "Erreur de communication avec la base de données : " + e.getMessage() + ", voir les logs du serveur");
			logger.error("Erreur lors d'une réparation de la base", e);
		}
		
		this.getServletContext().getRequestDispatcher("/admin/diagnostics/reparer.jsp").forward(req,  resp);
	}
}
