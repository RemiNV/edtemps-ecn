package org.ecn.edtemps.servlets;

import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
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
public class RechercheSalleLibreServlet extends QueryWithIntervalServlet {

	private static final long serialVersionUID = 2839852111716382792L;

	private static Logger logger = LogManager.getLogger(RechercheSalleLibreServlet.class.getName());

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		// Récupération des paramètres supplémentaires
		String paramEffectif = req.getParameter("effectif");
		String paramMateriel = req.getParameter("materiel");

		// Transformation de ces paramètres pour appeler la fonction de recherche
		Integer capacite = null;
		ArrayList<Materiel> listeMateriel = new ArrayList<Materiel>();;
		try {
			// Capacité de la salle
			capacite = Integer.valueOf(paramEffectif);
			
			// Liste du matériel
			String[] tableauMateriel = paramMateriel.split(",");
			for (int i = 0 ; i < tableauMateriel.length ; i++) {
				String[] infos = tableauMateriel[i].split(":");
				if (Integer.valueOf(infos[1])!=0) {
					listeMateriel.add(new Materiel(Integer.valueOf(infos[0]), "", Integer.valueOf(infos[1])));
				}
			}

		} catch (NumberFormatException e) {
			logger.error("Erreur lors du parsing des dates reçues en paramètres provenant de la boîte de dialogue de recherche d'une salle libre", e);
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, e);
		}
		
		SalleGestion salleGestion = new SalleGestion(bdd);
		JsonValue data;

		// Appel de la méthode de recherche
		ArrayList<SalleIdentifie> listeSalles = salleGestion.rechercherSalle(dateDebut, dateFin, listeMateriel, capacite);

		// Création de la réponse
		data = Json.createObjectBuilder()
				.add("sallesDisponibles", JSONUtils.getJsonArray(listeSalles))
				.build();

		bdd.close();
		return data;
	}
	
}
