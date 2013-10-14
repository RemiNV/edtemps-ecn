package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
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
	public static void sauverCalendrier(Calendrier calendrier) throws EdtempsException {
		
		// Récupération des attributs du calendrier
		String matiere = calendrier.getMatiere();
		String nom = calendrier.getNom();
		String type = calendrier.getType();
		List<Integer> idProprietaires = calendrier.getIdProprietaires(); 
				
		// Récupération de l'id de la matiere et du type
		int matiere_id;
		int type_id;
		try {
			matiere_id = BddGestion.recupererId("SELECT * FROM matiere WHERE matiere_nom LIKE '" + matiere + "'", "matiere_id");
			type_id = BddGestion.recupererId("SELECT * FROM typecalendrier WHERE typecal_libelle LIKE '" + type + "'", "typecal_id");
		} catch (DatabaseException e){
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
		// Vérification unicité/existence des id récupérés 
		if ((matiere_id != -1) && (type_id != -1)) { 			
			
			try {
				// On crée le calendrier dans la base de données
				BddGestion.executeRequest("INSERT INTO edt.calendrier (matiere_id, cal_nom, typeCal_id) "
						+ "VALUES ( "
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
			catch (DatabaseException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}
			
		}
		//pour debug
		else {
			System.out.println("ID matiere ou type non existant/unique");
		}
			
	}
	

}
