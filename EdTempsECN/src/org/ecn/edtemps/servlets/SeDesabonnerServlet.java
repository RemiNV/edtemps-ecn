package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.GroupeGestion;

/**
 * Servlet permettant le désabonnement d'un utilisateur à un groupeDeParcipants
 * @author Maxime Terrade
 *
 */
public class SeDesabonnerServlet extends RequiresConnectionServlet {
	
	private static Logger logger = LogManager.getLogger(SeDesabonnerServlet.class.getName());
	
	@Override
	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		logger.debug("Pret à faire le désabonnement");
		GroupeGestion groupeGestion = new GroupeGestion(bdd);
		String chaineIdGroupe = req.getParameter("idGroupe");
		int idGroupe = (int) Integer.parseInt(chaineIdGroupe);
		
		try {
			// Désabonnement du l'utilisateur au groupe (= modification de la BDD)
			groupeGestion.seDesabonner(userId, idGroupe, true);

			// Génération réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Désabonnement à un groupe réussi", null));
			logger.debug("Désabonnement à un groupe réussi");
			
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la suppression du rattachement", e);
		}

		bdd.close();
	}

}

