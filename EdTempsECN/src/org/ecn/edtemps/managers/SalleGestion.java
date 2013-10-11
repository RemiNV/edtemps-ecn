package org.ecn.edtemps.managers;

import java.sql.ResultSet;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.models.Salle;

/**
 * Classe de gestion des salles
 * 
 * @author Joffrey
 */
public class SalleGestion {

	public static void sauverSalle(Salle salle) {

		if (salle != null) {

			try {

				// Récupération des arguments sur la salle
				String batiment = salle.getBatiment();
				Integer niveau = salle.getNiveau();
				Integer numero = salle.getNumero();
				Integer capacite = salle.getCapacite();

				// Vérification de la cohérence des valeurs
				if (batiment != null && niveau != null && numero != null
						&& capacite != null) {

					ResultSet res = BddGestion
							.executeRequest("SELECT nextval('Salle')");

					BddGestion
							.executeRequest("INSERT INTO edt.salle (salle_id, salle_batiment, salle_niveau, salle_numero, salle_capacite) VALUES ('', '"
									+ batiment
									+ "', '"
									+ niveau
									+ "', '"
									+ numero + "', '" + capacite + "')");
				}

			} catch (DatabaseException e) {
				// TODO : faire quelque chose
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] arg) {
		Salle salle = new Salle("B", 1, 11, 30);

		SalleGestion.sauverSalle(salle);
	}

}
