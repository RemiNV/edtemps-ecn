package org.ecn.edtemps.servlets;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.IdentificationException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

/**
 * Servlet de base à étendre pour toutes les requêtes nécessitant l'identification de l'utilisateur par son token de connexion
 * @author Remi
 *
 */
public abstract class RequiresConnectionServlet extends TokenServlet {

	private static final long serialVersionUID = -115726731815825551L;

	@Override
	protected final int verifierToken(BddGestion bdd, String token) throws IdentificationException, DatabaseException {
		UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
		return utilisateurGestion.verifierConnexion(token);
	}
	
	/**
	 * Méthode permettant de définir les headers à envoyer dans la réponse.
	 * Toujours appeler super.setHeaders(resp) en surclassant cette méthode.
	 * 
	 * Les réponses envoyées par ce servlet demandent la désactivation du cache.
	 */
	@Override
	protected void setHeaders(HttpServletResponse resp) {
		super.setHeaders(resp);
		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		resp.setDateHeader("Expires", 0);
	}
	

	/**
	 * Récupérer une date dans une requête
	 * 
	 * @param req Requête à traiter
	 * @param param Libellé de la date à récupérer dans la requête
	 * @throws EdtempsException
	 */
	protected Date getDateInRequest(HttpServletRequest req, String param) throws EdtempsException {

		String strTimestamp = req.getParameter(param);
		
		if(StringUtils.isBlank(strTimestamp)) {
			logger.info("Requête effectuée avec un paramètre de date absent : " + param);
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètres "+param+" absent");
		}
		
		Date date = null;

		try {
			long timestamp = Long.parseLong(strTimestamp);
			date = new Date(timestamp);
		} catch(NumberFormatException e) {
			logger.info("Requête effectuée avec un paramètre de date non numérique : " + param);
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format du paramètre "+param+" incorrect");
		}
		
		return date;
	}

}
