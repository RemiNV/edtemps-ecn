package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;

public class CheckConnectionServlet extends RequiresConnectionServlet {

	private static Logger logger = LogManager.getLogger(CheckConnectionServlet.class.getName());
	
	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Si cette méthode est appelée, c'est que l'utilisateur a été identifié avec succès
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Identifiants valides.", null));
		logger.debug("Identifiants soumis à vérification par le client valides");
		bdd.close();
	}
}
