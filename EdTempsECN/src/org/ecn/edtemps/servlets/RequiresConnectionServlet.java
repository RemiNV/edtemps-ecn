package org.ecn.edtemps.servlets;

import javax.servlet.http.HttpServletResponse;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.IdentificationException;
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
}
