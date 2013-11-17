package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

/**
 * Génération d'un groupe de participant à partir d'une ligne de base de données
 *  - groupeparticipant_id<br>
 *  - groupeparticipant_nom<br>
 *  - groupeparticipant_rattachementautorise<br>
 *  - groupeparticipant_id_parent<br>
 *  - groupeparticipant_estcours<br>
 *  - groupeparticipant_estcalendrierunique<br>
 *  - groupeparticipant_id_parent_tmp
 * 
 * @author Joffrey
 *
 * @param <T> Type de groupe à générer
 */
public abstract class AbsGroupeInflater<T extends GroupeIdentifie> {

	/**
	 * Méthode de génération du groupe à partir d'une ligne de la base de données
	 * 
	 * @param reponse Ligne de la base de données
	 * @param bdd Gestionnaire de la base de données
	 * @return l'objet groupe du type souhaité
	 * @throws DatabaseException
	 */
	public T inflateGroupe(ResultSet reponse, BddGestion bdd) throws DatabaseException {
		
		try {
			
			// Informations générales
			int id = reponse.getInt("groupeparticipant_id");
			String nom = reponse.getString("groupeparticipant_nom");
			int idParent = reponse.getInt("groupeparticipant_id_parent");
			int idParentTmp = reponse.getInt("groupeparticipant_id_parent_tmp");
			boolean rattachementAutorise = reponse.getBoolean("groupeparticipant_rattachementautorise");
			boolean estCours = reponse.getBoolean("groupeparticipant_estcours");
			boolean estCalendrierUnique = reponse.getBoolean("groupeparticipant_estcalendrierunique");

			T res = inflate(id, nom, idParent, idParentTmp, rattachementAutorise, estCours, estCalendrierUnique, reponse, bdd);
			
			return res;
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	protected abstract T inflate(int id, String nom, int idParent, int idParentTmp,boolean rattachementAutorise,boolean estCours,
			boolean estCalendrierUnique, ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException;
}
