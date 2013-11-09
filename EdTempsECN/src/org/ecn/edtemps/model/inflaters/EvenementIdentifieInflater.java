package org.ecn.edtemps.model.inflaters;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;

public class EvenementIdentifieInflater extends AbsEvenementInflater<EvenementIdentifie> {

	@Override
	protected EvenementIdentifie inflate(String nom, Date dateDebut, Date dateFin, ArrayList<Integer> idCalendriers, 
			ArrayList<SalleIdentifie> salles, ArrayList<UtilisateurIdentifie> intervenants, 
			ArrayList<UtilisateurIdentifie> responsables, int id, ResultSet reponse, BddGestion bdd) throws DatabaseException {
		return new EvenementIdentifie(nom, dateDebut, dateFin, idCalendriers, salles, intervenants, responsables, id);
	}

	
}
