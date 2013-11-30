package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

/**
 * Génération d'un événement identifié à partir d'une ligne de base de données
 * 
 * @author Remi
 */
public class EvenementIdentifieInflater extends AbsEvenementInflater<EvenementIdentifie> {

	@Override
	protected EvenementIdentifie inflate(String nom, Date dateDebut, Date dateFin, ArrayList<Integer> idCalendriers, Integer idCreateur, 
			ArrayList<SalleIdentifie> salles, ArrayList<UtilisateurIdentifie> intervenants, 
			ArrayList<UtilisateurIdentifie> responsables, int id, ResultSet reponse, BddGestion bdd) throws DatabaseException {
		return new EvenementIdentifie(nom, dateDebut, dateFin, idCalendriers, idCreateur, salles, intervenants, responsables, id);
	}

	
}
