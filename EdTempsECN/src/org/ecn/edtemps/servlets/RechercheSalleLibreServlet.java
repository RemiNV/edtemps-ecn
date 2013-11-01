package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.json.ResponseManager;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Servlet pour rechercher une salle libre
 * 
 * @author Joffrey Terrade
 * 
 */
public class RechercheSalleLibreServlet extends RequiresConnectionServlet {

	private static final long serialVersionUID = 2839852111716382792L;

	private static Logger logger = LogManager.getLogger(RechercheSalleLibreServlet.class.getName());

	@Override
	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// Récupération des paramètres
		String paramDate = req.getParameter("date");
		String paramHeureDebut = req.getParameter("heureDebut");
		String paramHeureFin = req.getParameter("heureFin");
		String paramCapacite = req.getParameter("capacite");
		String paramMateriel = req.getParameter("materiel");

		// Transformation de ces paramètres pour appeler la fonction de recherche
		Date dateDebut = null;
		Date dateFin = null;
		Integer capacite = null;
		ArrayList<Materiel> listeMateriel = new ArrayList<Materiel>();;
		try {
			// Dates de début et fin
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateDebut = simpleDateFormat.parse(paramDate + " " + paramHeureDebut + ":00");
			dateFin = simpleDateFormat.parse(paramDate + " " + paramHeureFin + ":00");
		
			// Capacité de la salle
			capacite = Integer.valueOf(paramCapacite);
			
			// Liste du matériel
			String[] tableauMateriel = paramMateriel.split(",");
			for (int i = 0 ; i < tableauMateriel.length ; i++) {
				String[] infos = tableauMateriel[i].split(":");
				if (Integer.valueOf(infos[1])!=0) {
					listeMateriel.add(new Materiel(Integer.valueOf(infos[0]), "", Integer.valueOf(infos[1])));
				}
			}

		} catch (ParseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, e.getMessage(), null));
			logger.error("Erreur lors du parsing des dates reçues en paramètres provenant de la boîte de dialogue de recherche d'une salle libre", e);
		}

		
		
		SalleGestion salleGestion = new SalleGestion(bdd);
		JsonValue data;

		try {
			// Appel de la méthode de recherche
			ArrayList<SalleIdentifie> listeSalles = salleGestion.rechercherSalle(dateDebut, dateFin, listeMateriel, capacite);

			// Création de la réponse
			data = Json.createObjectBuilder()
					.add("sallesDisponibles", JSONUtils.getJsonArray(listeSalles))
					.build();

			// Génération de la réponse
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Salles disponibles récupérées", data));

		} catch (DatabaseException e) {
			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
			logger.error("Erreur d'accès à la base de données lors de la recherche des salles correspondant aux critères de recherche de dsisponibilité", e);
		}

		bdd.close();
	}
	
}
