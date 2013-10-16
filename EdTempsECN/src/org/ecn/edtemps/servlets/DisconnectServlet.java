package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

public class DisconnectServlet extends RequiresConnectionServlet {

	@Override
	protected void doGetAfterLogin(int userId, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			UtilisateurGestion utilisateurGestion = new UtilisateurGestion(new BddGestion());
			utilisateurGestion.seDeconnecter(userId);
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Utilisateur déconnecté", null));
		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.DATABASE_ERROR, e.getMessage(), null));
		}
	}
}
