package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;

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

					BddGestion
							.executeRequest("INSERT INTO edt.salle (salle_id, salle_batiment, salle_niveau, salle_numero, salle_capacite) VALUES (nextval('edt.seq_salle'), '"
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

}
