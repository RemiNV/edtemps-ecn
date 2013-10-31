package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.MaterielGestion;
import org.ecn.edtemps.models.Materiel;

/**
 * Servlet pour rechercher une salle libre
 * 
 * @author Joffrey Terrade
 * 
 */
public class RechercheSalleLibreServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2839852111716382792L;

	private static Logger logger = LogManager.getLogger(RechercheSalleLibreServlet.class
			.getName());

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		System.out.println(req.toString());
		
	}

}