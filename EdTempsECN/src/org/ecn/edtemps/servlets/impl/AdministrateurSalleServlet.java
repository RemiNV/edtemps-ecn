package org.ecn.edtemps.servlets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.SalleGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Servlet pour la gestion des salles
 * 
 * @author Joffrey Terrade
 */
public class AdministrateurSalleServlet extends HttpServlet {

	private static final long serialVersionUID = 2760466483465073439L;
	private static Logger logger = LogManager.getLogger(AdministrateurSalleServlet.class.getName());
	
	/**
	 * Servlet pour la gestion des salles
	 * @param req Requête
	 * @param resp Réponse pour le client
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// Vérification des valeurs possibles dans le path de la requête
		String pathInfo = req.getPathInfo();
		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier") && !pathInfo.equals("/supprimer") ) {
			logger.error("Les seules méthodes acceptées sont l'ajout, la modification et la suppression");
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		HttpSession session = req.getSession(true);

		// Vérifie que l'utilisateur est bien connecté
		if (session.getAttribute("connect")!="OK") {
			logger.error("L'utilisateur n'est pas connecté");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		try {
			switch (pathInfo) {
				case "/ajouter":
					doAjouter(req, resp);
					break;
				case "/modifier":
					doModifier(req, resp);
					break;
				case "/supprimer":
					doSupprimer(req, resp);
					break;
			}
		} catch (NumberFormatException e) {
			logger.error("Erreur de cast des paramètres");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (EdtempsException e) {
			logger.error("Erreur du gestionnaire de salle");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}
	
	
	/**
	 * Supprimer une salle
	 * @param req Requête
	 * @param resp Réponse
	 * @throws EdtempsException 
	 * @throws IOException 
	 */
	public void doSupprimer(HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
		logger.info("Suppression d'une salle");

		// Récupération de l'identifiant de la salle à supprimer dans la requête
		Integer id = req.getParameter("id")!="" ? Integer.valueOf(req.getParameter("id")) : null;
		if (id == null) {
			logger.error("Identifiant de la salle erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Exécute la requête de suppression avec le manager
		BddGestion bdd = new BddGestion();
		SalleGestion gestionnaireSalles = new SalleGestion(bdd);
		gestionnaireSalles.supprimerSalle(id);
		bdd.close();
		
		// Redirige vers la page de liste des salles
		resp.sendRedirect(req.getContextPath()+"/admin/salles/index.jsp");
	}
	

	/**
	 * Ajouter une salle
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doAjouter(HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {
		logger.info("Ajout d'une salle");

		// Récupération des valeurs du formulaire
		String batiment = req.getParameter("ajouter_salle_batiment");
		String nom = req.getParameter("ajouter_salle_nom");
		Integer niveau = req.getParameter("ajouter_salle_niveau")!="" ? Integer.valueOf(req.getParameter("ajouter_salle_niveau")) : null;
		Integer numero = req.getParameter("ajouter_salle_numero")!="" ? Integer.valueOf(req.getParameter("ajouter_salle_numero")) : null;
		Integer capacite = req.getParameter("ajouter_salle_capacite")!="" ? Integer.valueOf(req.getParameter("ajouter_salle_capacite")) : null;
		
		// Récupère la liste des matériels avec la quantité associée
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		List<String> listeIdMateriel = Arrays.asList(req.getParameter("listeIdMateriel").split(","));
		if (CollectionUtils.isNotEmpty(listeIdMateriel)) {
			for (String materiel : listeIdMateriel) {
				int id = Integer.valueOf(materiel);
				Integer quantite = req.getParameter("ajouter_salle_materiel_"+id)!="" ? Integer.valueOf(req.getParameter("ajouter_salle_materiel_"+id)) : null;
				if (quantite>0) {
					materiels.add(new Materiel(id, "", quantite));
				}
			}
		}
		
		// Créer l'objet Salle à enregistrer en base de données
		Salle salle = new Salle(nom);
		salle.setBatiment(batiment);
		if (capacite!=null) {
			salle.setCapacite(capacite);
		}
		if (niveau!=null) {
			salle.setNiveau(niveau);
		}
		if (numero!=null) {
			salle.setNumero(numero);
		}
		salle.setMateriels(materiels);

		// Exécute la requête d'ajout avec le manager
		BddGestion bdd = new BddGestion();
		SalleGestion gestionnaireSalles = new SalleGestion(bdd);
		gestionnaireSalles.sauverSalle(salle);
		bdd.close();

		// Redirige vers la page de liste des salles
		resp.sendRedirect(req.getContextPath()+"/admin/salles/index.jsp");
	}
	
	
	/**
	 * Modifier une salle
	 * @param req Requête
	 * @param resp Réponse
	 * @throws IOException 
	 * @throws EdtempsException 
	 */
	public void doModifier(HttpServletRequest req, HttpServletResponse resp) throws IOException, EdtempsException {
		logger.info("Modification d'une salle");

		// Récupération des valeurs du formulaire
		String batiment = req.getParameter("modifier_salle_batiment");
		String nom = req.getParameter("modifier_salle_nom");
		Integer niveau = req.getParameter("modifier_salle_niveau")!="" ? Integer.valueOf(req.getParameter("modifier_salle_niveau")) : null;
		Integer numero = req.getParameter("modifier_salle_numero")!="" ? Integer.valueOf(req.getParameter("modifier_salle_numero")) : null;
		Integer capacite = req.getParameter("modifier_salle_capacite")!="" ? Integer.valueOf(req.getParameter("modifier_salle_capacite")) : null;
		Integer idSalle = req.getParameter("modifier_salle_id")!="" ? Integer.valueOf(req.getParameter("modifier_salle_id")) : null;
		if (idSalle == null) {
			logger.error("Identifiant de la salle erroné");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Récupère la liste des matériels avec la quantité associée
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		List<String> listeIdMateriel = Arrays.asList(req.getParameter("listeIdMateriel").split(","));
		if (CollectionUtils.isNotEmpty(listeIdMateriel)) {
			for (String materiel : listeIdMateriel) {
				int id = Integer.valueOf(materiel);
				Integer quantite = req.getParameter("modifier_salle_materiel_"+id)!="" ? Integer.valueOf(req.getParameter("modifier_salle_materiel_"+id)) : null;
				if (quantite>0) {
					materiels.add(new Materiel(id, "", quantite));
				}
			}
		}
		
		// Créer l'objet Salle à modifier en base de données
		SalleIdentifie salle = new SalleIdentifie(idSalle, nom);
		salle.setBatiment(batiment);
		if (capacite!=null) {
			salle.setCapacite(capacite);
		}
		if (niveau!=null) {
			salle.setNiveau(niveau);
		}
		if (numero!=null) {
			salle.setNumero(numero);
		}
		salle.setMateriels(materiels);

		// Exécute la requête de modification avec le manager
		BddGestion bdd = new BddGestion();
		SalleGestion gestionnaireSalles = new SalleGestion(bdd);
		gestionnaireSalles.modifierSalle(salle);
		bdd.close();

		// Redirige vers la page de liste des salles
		resp.sendRedirect(req.getContextPath()+"/admin/salles/index.jsp");
	}

}
