package org.ecn.edtemps.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.ResponseManager;

public class CheckConnectionServlet extends RequiresConnectionServlet {

	@Override
	protected void doGetAfterLogin(int userId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Si cette méthode est appelée, c'est que l'utilisateur a été identifié avec succès
		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Identifiants valides.", null));
	}
}
