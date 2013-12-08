package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;

/**
 * Génération d'un calendrier identifié à partir d'une ligne de base de données
 * 
 * @author Joffrey
 */
public class CalendrierIdentifieInflater extends AbsCalendrierInflater<CalendrierIdentifie> {

	@Override
	protected CalendrierIdentifie inflate(int id, String nom, String type, String matiere, List<Integer> idProprietaires, int idCreateur, ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException {
		return new CalendrierIdentifie(nom, type, matiere, idProprietaires, id, idCreateur);
	}
	
}
