package org.ecn.edtemps.managers;

import Neuneu;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.ecn.edtemps.models.identifie.Utilisateur;

/** 
 * Classe de gestion des calendriers 
 * @author Maxime TERRADE
 *
 */
public class CalendrierGestion {
	
	/**
	 * Méthode (statique) d'enregistrement du calendrier <calendrier> dans la base de données
	 * Le propriétaire du calendrier à enregistrer est l'utilisateur <proprietaire>
	 * 
	 * NB : le rattachement d'un calendrier à un groupeDeParticipants n'est pas réalisé dans cette fonction.
	 * 
	 * @param calendrier
	 * @param proprietaire
	 */
	public static void sauverCalendrier(Calendrier calendrier) {
		
		// Récupération des attributs du calendrier
		String matiere = calendrier.getMatiere();
		String nom = calendrier.getNom();
		String type = calendrier.getType();
		List<Integer> idProprietaires = calendrier.getIdProprietaires(); 
				
		// Récupération de l'id de la matiere
		int matiere_id = BddGestion.recupererId("SELECT * FROM matiere WHERE matiere_nom LIKE '" + matiere + "'", "matiere_id");
		// Récupération de l'id du type
		int type_id = BddGestion.recupererId("SELECT * FROM typecalendrier WHERE typecal_libelle LIKE '" + type + "'", "typecal_id");
		// Vérification unicité/existence des id récupérés 
		if ((matiere_id != -1) && (type_id != -1)) { 				
			try {
				// On crée le calendrier dans la base de données
				BddGestion.executeRequest("INSERT INTO edt.calendrier (cal_id, matiere_id, cal_nom, typeCal_id) "
						+ "VALUES ("
						+ "nextval('edt.seq_calendrier'), '"
						+ matiere_id
						+ "', '"
						+ nom
						+ "', '"
						+ type_id
						+ "')");
				// On récupère l'id du calendrier créé
				int id_calendrier = BddGestion.recupererId(
						"SELECT * FROM calendrier "
						+ "WHERE matiere_id = '" + matiere_id + "' "
						+ "AND cal_nom = '" + nom + "' "
						+ "AND typeCal_id = '" + type_id + "' ",
						"cal_id");
				;
				// On définit les utilisateurs idProprietaires comme proprietaires du calendrier créé
				Iterator<Integer> itr = idProprietaires.iterator();
				while (itr.hasNext()){
					int id_utilisateur = itr.next();
					BddGestion.executeRequest("INSERT INTO edt.proprietairecalendrier (utilisateur_id, cal_id) "
							+ "VALUES ("
							+ id_utilisateur
							+ "', '"
							+ id_calendrier 
							+ "')");
				}
			} 
			catch (NullPointerException e) {
				// TODO : exception générée quand salle mal définie / remplie
				e.printStackTrace();
			}
			catch (DatabaseException e) {
				// TODO : je propose qu'on la remonte... puisqu'il faudra une fonction au dessus pour créer la salle
				e.printStackTrace();
			}
			
		}
		else {
			//throw new 
		}
			
	}
	

}
