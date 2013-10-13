package org.ecn.edtemps.managers;

import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.Salle;

/**
 * Classe de gestion des salles
 * 
 * @author Joffrey
 */
public class SalleGestion {

	/**
	 * Enregistre une salle dans la base de données
	 * 
	 * @param salle
	 *            salle à enregistrer
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public static void sauverSalle(Salle salle) throws EdtempsException {

		if (salle != null) {

			try {

				// Récupération des arguments sur la salle
				String batiment = salle.getBatiment();
				Integer niveau = salle.getNiveau();
				Integer numero = salle.getNumero();
				Integer capacite = salle.getCapacite();
				List<Materiel> materiels = salle.getMateriels();

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
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}

		}

	}

}
