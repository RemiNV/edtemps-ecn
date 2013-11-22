package org.ecn.edtemps.servlets;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet permettant de récupérer les rapports d'erreur JS des clients.
 * Un client ne peut pas signaler plus d'un certain nombre d'erreurs pendant une certaine durée 
 * 
 * @author Remi
 *
 */
public class ClientLoggingServlet extends HttpServlet {
	
	private static final long serialVersionUID = -4840232540550780272L;

	private static Logger logger = LogManager.getLogger(ClientLoggingServlet.class.getName());
	
	// Délai minimal entre deux signalements d'un utilisateur (20 sec)
	private static final long MIN_DELAY_REPORTS = 20*1000;
	
	// Mémorisation de la date (timestamp) du dernier rapport de chaque utilisateur
	private Hashtable<String, Long> dernierRapportUtilisateur = new Hashtable<String, Long>();

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		String clientIp = req.getRemoteAddr();
		Long dernierRapport;
		if((dernierRapport = dernierRapportUtilisateur.get(clientIp)) != null && dernierRapport + MIN_DELAY_REPORTS > System.currentTimeMillis()) {
			return; // L'utilisateur ne peut pas envoyer des rapports trop souvent
		}
		
		dernierRapportUtilisateur.put(clientIp, System.currentTimeMillis());
		
		String message = req.getParameter("message");
		if(message != null) {
			logger.error("Erreur signalée par le client " + req.getRemoteAddr() + " : " + message);
		}
	}
}
