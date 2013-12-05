package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/**
 * Génération d'un calendrier à partir d'une ligne de base de données
 * 
 * Colonnes obligatoires pour tous les types de calendrier : <br>
 * - cal_id<br>
 * - cal_nom<br>
 * - matiere_nom<br>
 * - typecal_libelle
 * 
 * @author Joffrey
 *
 * @param <T> Type de calendrier à générer
 */
public abstract class AbsCalendrierInflater<T extends CalendrierIdentifie> {

	/**
	 * Méthode de génération du calendrier à partir d'une ligne de la base de données
	 * 
	 * @param reponse Ligne de la base de données
	 * @param bdd Gestionnaire de la base de données
	 * @return l'objet salle
	 * @throws DatabaseException
	 */
	public T inflateCalendrier(ResultSet reponse, BddGestion bdd) throws DatabaseException {
		
		try {
			// Informations générales
			int id = reponse.getInt("cal_id");
			String nom = reponse.getString("cal_nom");
			int idCreateur = reponse.getInt("cal_createur");
			String matiere = reponse.getString("matiere_nom");
			String type = reponse.getString("typecal_libelle");
	
			// Récupération des propriétaires du calendrier
			ResultSet rs_proprios = bdd.executeRequest("SELECT utilisateur_id FROM edt.proprietairecalendrier WHERE cal_id="+id);
			List<Integer> idProprietaires = new ArrayList<Integer>();
			while(rs_proprios.next()){
				 idProprietaires.add(rs_proprios.getInt("utilisateur_id"));
			}

			/* Si au moins un proprio existe, le ou les ajouter aux attibuts du Calendrier. 
			 * Sinon, exception EdtempsException
			 */
			if (idProprietaires.size() != 0) {
				T res = inflate(id, nom, type, matiere, idProprietaires, idCreateur, reponse, bdd);
				return res;
			}
			else {
				throw new DatabaseException("Le calendrier d'id="+id+" n'a pas de propriétaire");
			}
			
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	protected abstract T inflate(int id, String nom, String type, String matiere, List<Integer> idProprietaires, int idCreateur, ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException;
}
