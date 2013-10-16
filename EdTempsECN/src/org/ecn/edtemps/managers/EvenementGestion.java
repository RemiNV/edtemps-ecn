package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Evenement;

/** 
 * Classe de gestion des evenements
 * 
 * @author Maxime TERRADE
 *
 */
public class EvenementGestion {

	protected BddGestion _bdd;
	
	/**
	 * Initialise un gestionnaire d'evenements
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public EvenementGestion(BddGestion bdd) {
		_bdd = bdd;
	}
		
	
	/**
	 * Méthode d'enregistrement d'un evenement dans la base de données
	 * 
	 * @param evenement
	 */
	public void sauverEvenement(Evenement evenement) throws EdtempsException {
		
		// Récupération des attributs de l'evenement
		String nom = evenement.getNom();
		Date dateDebut = evenement.getDateDebut();
		Date dateFin = evenement.getDateFin();
		List<Integer> idCalendriers = evenement.getIdCalendriers();
		List<String> idIntervenants = evenement.getIntervenants();
		int idSalle = evenement.getSalle().getId();
				
		/*
		 * IMPORTANT POUR CONTINUER
		 * Liste de String pour les intervenants oO ? Comment on retrouve l'id ??
		 */
			
		try {
			
			// On met les dates au format DATETIME
			String dateDebutFormatee = "";
			String dateFinFormatee = "";
			
			// Début transaction
			_bdd.startTransaction();			
			
			// On crée l'événement dans la base de données
	
			ResultSet rs_ligneCreee = _bdd.executeRequest(
					"INSERT INTO edt.evenement (eve_nom, eve_dateDebut, eve_dateFin) "
					+ "VALUES ( '" + nom + "', '" + dateDebutFormatee + "', '" + dateFinFormatee + "') "
				    + "RETURNING eve_id");
			
			// On récupère l'id de l'evenement créé
			rs_ligneCreee.next();
			int id_evenement = rs_ligneCreee.getInt(1);
			
			// On rattache l'evenement aux calendriers 
			Iterator<Integer> itr = idCalendriers.iterator();
			while (itr.hasNext()){
				int id_calendrier = itr.next();
				_bdd.executeRequest(
						"INSERT INTO edt.evenementappartient (eve_id, cal_id) "
						+ "VALUES (" + id_evenement + ", " + id_calendrier + ")"
						);
			}
			
			// On rattache la salle à l'evenement
			
			// On rattache le matériel nécessité à l'évenement
			
			// On indique le(s) responsable(s) dans la base
			
			// On indique le(s) intervenant(s) dans la base
			
			// Fin transaction
			_bdd.commit();
		} 
		catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
			
	}
	
	
	// ajouté à la volée pour éviter erreur dans la méthode supprimerCalendrier, qui utilise cette méthode
	public void supprimerEvenement(int idEvenement) {
		
	}
	
}
