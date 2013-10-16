package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/** 
 * Classe de gestion des calendriers 
 * 
 * @author Maxime TERRADE
 *
 */
public class CalendrierGestion {
	
	protected BddGestion _bdd;
	
	/**
	 * Initialise un gestionnaire de calendriers
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public CalendrierGestion(BddGestion bdd) {
		_bdd = bdd;
	}
	
	
	/**
	 * Méthode d'enregistrement du Calendrier "calendrier" dans la base de données
	 *
	 * NB : le rattachement d'un calendrier à un groupeDeParticipants n'est pas réalisé dans cette fonction.
	 * 
	 * @param calendrier
	 */
	public void sauverCalendrier(Calendrier calendrier) throws EdtempsException {
		
		// Récupération des attributs du calendrier
		String matiere = calendrier.getMatiere();
		String nom = calendrier.getNom();
		String type = calendrier.getType();
		List<Integer> idProprietaires = calendrier.getIdProprietaires(); 
				
		// Récupération de l'id de la matiere et du type
		int matiere_id;
		int type_id;
		try {
			matiere_id = _bdd.recupererId("SELECT * FROM matiere WHERE matiere_nom LIKE '" + matiere + "'", "matiere_id");
			type_id = _bdd.recupererId("SELECT * FROM typecalendrier WHERE typecal_libelle LIKE '" + type + "'", "typecal_id");
		} catch (DatabaseException e){
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
		// Vérification unicité/existence des id récupérés 
		if ((matiere_id != -1) && (type_id != -1)) { 			
			
			try {
				// Début transaction
				_bdd.startTransaction();
				
				// On crée le calendrier dans la base de données
				ResultSet rs_ligneCreee = _bdd.executeRequest("INSERT INTO edt.calendrier (matiere_id, cal_nom, typeCal_id) "
						+ "VALUES ( " + matiere_id + ", '" + nom + "', " + type_id + ") RETURNING cal_id");
				
				// On récupère l'id du calendrier créé
				rs_ligneCreee.next();
				int id_calendrier = rs_ligneCreee.getInt(1);
	
				// On définit les utilisateurs idProprietaires comme proprietaires du calendrier créé
				Iterator<Integer> itr = idProprietaires.iterator();
				while (itr.hasNext()){
					int id_utilisateur = itr.next();
					_bdd.executeRequest(
							"INSERT INTO edt.proprietairecalendrier (utilisateur_id, cal_id) "
							+ "VALUES (" + id_utilisateur + ", " + id_calendrier + ")"
							);
				}
				
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
		else {
			throw new EdtempsException(ResultCode.DATABASE_ERROR,"ID matiere ou type non existant ou non unique");
		}
			
	}
	
	
	public Calendrier getCalendrier(int idCalendrier) throws EdtempsException {
		
		Calendrier result = new Calendrier();
		String nom = null, matiere = null, type = null;
		List<Integer> idProprietaires = new ArrayList<Integer>(); 
		
		try {
			
			// Récupération du calendrier (nom, matiere, type) cherché sous forme de ResultSet
			ResultSet rs_calendrier = _bdd.executeRequest(
					"SELECT * FROM calendrier "
					+ "INNER JOIN matiere ON calendrier.matiere_id = matiere.matiere_id "
					+ "INNER JOIN typecalendrier ON typecalendrier.typeCal_id = calendrier.typeCal_id "
					+ "WHERE cal_id = " + idCalendrier );

			while(rs_calendrier.next()){
				 nom = rs_calendrier.getString("cal_nom");
				 matiere = rs_calendrier.getString("matiere_nom");
				 type = rs_calendrier.getString("typeCal_libelle");
			}
			
			/* Si le ResultSet contient bien une et une seule ligne
			 * Sinon, exception EdtempsException
			 */
			if (rs_calendrier.getRow() == 1) {
				
				// On remplit les attributs du Calendrier result 
				result.setNom(nom);
				result.setMatiere(matiere);
				result.setType(type);
				
				// Récupération du calendrier (nom, matiere, type) cherché sous forme de ResultSet
				ResultSet rs_proprios = _bdd.executeRequest(
						"SELECT * FROM proprietairecalendrier WHERE cal_id = " + idCalendrier );
				
				while(rs_proprios.next()){
					 idProprietaires.add(rs_proprios.getInt("utilisateur_id"));
				}
				
				/* Si au moins un proprio existe, le ou les ajouter aux attibuts du Calendrier. 
				 * Sinon, exception EdtempsException
				 */
				if (rs_proprios.getRow() != 0) {
					result.setIdProprietaires(idProprietaires);
				}
				else {
					throw new EdtempsException(ResultCode.DATABASE_ERROR, "getCalendrier() error : liste des proprietaires vides");
				}
			}
			else {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, "getCalendrier() error : pas de calendrier correspondant à l'idCalendrier en argument");
			}
			
		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
		return result;
		
	}
	
	
	/**
	 * Méthode modifierCalendrier(CalendrierIdentifie calId)
	 * 
	 * Permet de remplacer, dans la base de données, 
	 * les anciennes valeurs du calendrier défini par l'id de calId (en attribut) 
	 * par les valeurs contenues dans calId (en attributs)
	 * 
	 * @param calId : CalendrierIdentifie
	 * @throws EdtempsException
	 */
	public void modifierCalendrier(CalendrierIdentifie calId) 
			throws EdtempsException {
		
		try {
			
			// Commencer une transaction
			_bdd.startTransaction();
			
			// Récupération de l'id de la matiere et du type
			int matiere_id;
			int type_id;
			try {
				matiere_id = _bdd.recupererId("SELECT * FROM matiere WHERE matiere_nom LIKE '" + calId.getMatiere() + "'", "matiere_id");
				type_id = _bdd.recupererId("SELECT * FROM typecalendrier WHERE typecal_libelle LIKE '" + calId.getType() + "'", "typecal_id");
			} catch (DatabaseException e){
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}
			
			// Modifier matiere, nom, type du calendrier
			_bdd.executeRequest(
					"UPDATE calendrier "
					+ "SET (matiere_id, cal_nom, typeCal_id) = "
					+ "(" + matiere_id + ", '" + calId.getNom() + "', " + type_id + ") "
					+ "WHERE cal_id = " + calId.getId() );
		
			// Supprimer ancienne liste de propriétaires du calendrier
			_bdd.executeRequest(
					"DELETE FROM proprietairecalendrier "
					 + "WHERE cal_id = " + calId.getId() 
			);
			
			// Ajouter nouvelle liste de propriétaires du calendrier		
			Iterator<Integer> itrProprios = calId.getIdProprietaires().iterator();
			while (itrProprios.hasNext()){
				int idProprio = itrProprios.next();
				_bdd.executeRequest(
						"INSERT INTO proprietairecalendrier "
						 + "VALUES (utilisateur_id, cal_id) = "
						 + "(" + idProprio +", " + calId.getId() + ") " 
				);
			}
			
			// Fin transaction
			_bdd.commit();
			
		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
	}

	
	
	/**
	 * Méthode supprimerCalendrier(int idCalendrier)
	 * 
	 * Permet de supprimer un calendrier dans la base de données,
	 * calendrier défini par l'entier en argument, correspondant à l'ID du calendrier 
	 * 
	 * @param idCalendrier
	 * @throws EdtempsException
	 */
	public void supprimerCalendrier(int idCalendrier) 
			throws EdtempsException {
		
		try {
			// Début transaction
			_bdd.startTransaction();
			
			// Supprimer liste de propriétaires du calendrier
			_bdd.executeRequest(
					"DELETE FROM proprietairecalendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Supprimer dépendance avec les groupes de participants
			_bdd.executeRequest(
					"DELETE FROM calendrierAppartientGroupe "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			/* Supprimer les événements associés au calendrier
			 * 		1 - Récupération des id des evenements associés
			 * 		2 - Suppression du lien entre les evenements et le calendrier
			 * 		3 - Suppression des evenements eux-même
			 */
			ResultSet rs_evenementsAssocies = _bdd.executeRequest(
					"SELECT * FROM  evenementAppartient "
					+ "WHERE cal_id = " + idCalendrier 
					);
			_bdd.executeRequest(
					"DELETE FROM evenementAppartient "
					 + "WHERE cal_id = " + idCalendrier  
					 );
			while(rs_evenementsAssocies.next()){
				EvenementGestion eveGestionnaire = new EvenementGestion(this._bdd);
				eveGestionnaire.supprimerEvenement(rs_evenementsAssocies.getInt("eve_id"));
			}
			// Supprimer calendrier
			_bdd.executeRequest(
					"DELETE FROM calendrier "
					 + "WHERE cal_id = " + idCalendrier 
					 );
			// Fin transaction
			_bdd.commit(); 

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}
		
	}	
	
	
	
}
