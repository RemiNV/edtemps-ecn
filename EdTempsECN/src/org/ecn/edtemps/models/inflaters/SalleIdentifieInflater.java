package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Classe de création d'une SalleIdentifie à partir de lignes de base de données.
 * Une ligne doit contenir les colonnes :
 * - salle_id<br>
 * - salle_batiment<br>
 * - salle_nom<br>
 * - salle_niveau<br>
 * - salle_numero<br>
 * - salle_capacite  
 * 
 * @author Remi
 *
 */
public class SalleIdentifieInflater extends AbsSalleInflater<SalleIdentifie> {

	@Override
	protected SalleIdentifie inflate(int id, String batiment, String nom, int capacite,
			int niveau, int numero, ArrayList<Materiel> materiels,
			ResultSet reponse, BddGestion bdd) throws DatabaseException, SQLException {
		
		return new SalleIdentifie(id, batiment, nom, capacite, niveau, numero, materiels);
	}

}
