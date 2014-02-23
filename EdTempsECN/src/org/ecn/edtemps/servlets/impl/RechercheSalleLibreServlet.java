package org.ecn.edtemps.servlets.impl;

import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.json.JSONUtils;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.SalleRecherche;
import org.ecn.edtemps.servlets.QueryWithIntervalServlet;

/**
 * Servlet pour rechercher une salle libre
 * 
 * @author Joffrey Terrade
 */
public class RechercheSalleLibreServlet extends QueryWithIntervalServlet {

	private static final long serialVersionUID = 2839852111716382792L;
	private static Logger logger = LogManager.getLogger(RechercheSalleLibreServlet.class.getName());

	@Override
	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
		// Récupération des paramètres supplémentaires
		String paramEffectif = req.getParameter("effectif");
		String paramMateriel = req.getParameter("materiel");
		String paramSallesOccupees = req.getParameter("sallesOccupees");
		String paramEvenementIgnorer = req.getParameter("idEvenementIgnorer");

		// Transformation de ces paramètres pour appeler la fonction de recherche
		Integer capacite = null;
		Integer idEvenementIgnorer = null;
		ArrayList<Materiel> listeMateriel = new ArrayList<Materiel>();
		boolean sallesOccupees = Boolean.valueOf(paramSallesOccupees);
		try {
			// Capacité de la salle
			if (StringUtils.isNotBlank(paramEffectif)) {
				capacite = Integer.valueOf(paramEffectif);
			}
			if (StringUtils.isNotBlank(paramEvenementIgnorer)) {
				idEvenementIgnorer = Integer.valueOf(paramEvenementIgnorer);
			}
			
			// Liste du matériel
			if(!paramMateriel.matches("|\\d+:\\d+(,\\d+:\\d+)*")) { // Vide ou au format entier:entier,entier:entier...
				bdd.close();
				throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format du paramètre materiel incorrect");
			}
			
			if(!paramMateriel.equals("")) {
				String[] tableauMateriel = paramMateriel.split(",");
				for (int i = 0 ; i < tableauMateriel.length ; i++) {
					String[] infos = tableauMateriel[i].split(":");
					if (Integer.valueOf(infos[1])!=0) {
						listeMateriel.add(new Materiel(Integer.valueOf(infos[0]), "", Integer.valueOf(infos[1])));
					}
				}
			}

		} catch (NumberFormatException e) {
			logger.error("Erreur lors du parsing des dates reçues en paramètres provenant de la boîte de dialogue de recherche d'une salle libre", e);
			bdd.close();
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, e);
		}
		
		SalleGestion salleGestion = new SalleGestion(bdd);
		JsonValue data;

		// Appel de la méthode de recherche
		ArrayList<SalleRecherche> listeSalles = salleGestion.rechercherSalle(dateDebut, dateFin, listeMateriel, capacite, sallesOccupees, true, idEvenementIgnorer);

		// Création de la réponse
		data = Json.createObjectBuilder()
				.add("sallesDisponibles", JSONUtils.getJsonArray(listeSalles))
				.build();

		bdd.close();
		return data;
	}
	
}
