package org.ecn.edtemps.models.inflaters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleRecherche;

/**
 * Création d'une salle "recherche" à partir d'une ligne de base de données.
 * Colonnes obligatoires pour les lignes : <br>
 * - salle_id<br>
 * - salle_batiment<br>
 * - salle_nom<br>
 * - salle_niveau<br>
 * - salle_numero<br>
 * - salle_capacite<br>
 * - salle_est_occupe
 * 
 * @author Remi
 *
 */
public class SalleRechercheInflater extends AbsSalleInflater<SalleRecherche> {

	protected Date dateDebut;
	protected Date dateFin;
	protected boolean createTransactions;
	
	public SalleRechercheInflater(Date dateDebut, Date dateFin, boolean createTransactions) {
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.createTransactions = createTransactions;
	}
	
	@Override
	protected SalleRecherche inflate(int id, String batiment, String nom,
			int capacite, int niveau, int numero,
			ArrayList<Materiel> materiels, ResultSet reponse, BddGestion bdd)
			throws DatabaseException, SQLException {
		
		boolean estOccupe = reponse.getBoolean("salle_est_occupe");
		
		ArrayList<EvenementIdentifie> evenements = null;
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		if(estOccupe) {
			// Récupération des évènements de la salle
			evenements = evenementGestion.listerEvenementIdentifiesSalle(id, dateDebut, dateFin, createTransactions);
		}
		
		return new SalleRecherche(id, batiment, nom, capacite, niveau, numero, materiels, evenements);
	}

	
}
