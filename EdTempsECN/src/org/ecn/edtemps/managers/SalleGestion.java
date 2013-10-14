package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Classe de gestion des salles
 * 
 * @author Joffrey
 */
public class SalleGestion {

	/**
	 * Enregistrer une salle dans la base de données
	 * 
	 * @param salle
	 *            salle à enregistrer
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public void sauverSalle(Salle salle) throws EdtempsException {

		if (salle != null) {

			try {

				// Récupération des arguments sur la salle
				String batiment = salle.getBatiment();
				String nom = salle.getNom();
				Integer niveau = salle.getNiveau();
				Integer numero = salle.getNumero();
				Integer capacite = salle.getCapacite();
				List<Materiel> materiels = salle.getMateriels();

				// Vérification de la cohérence des valeurs
				if (batiment != null && niveau != null && numero != null
						&& capacite != null && nom != null) {

					BddGestion
							.executeRequest("INSERT INTO edt.salle (salle_id, salle_batiment, salle_niveau, salle_numero, salle_capacite, salle_nom) VALUES (nextval('edt.seq_salle'), '"
									+ batiment
									+ "', '"
									+ niveau
									+ "', '"
									+ numero
									+ "', '"
									+ capacite
									+ "', '"
									+ nom
									+ "')");

					// Récupérer l'identifiant de la ligne qui vient d'être
					// ajoutée
					BddGestion
							.executeRequest("SELECT currval('edt.seq_salle');");
				}

			} catch (DatabaseException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}

		}

	}

	/**
	 * Récupérer une salle dans la base de données
	 * 
	 * @param identifiant
	 *            identifiant de la salle à récupérer
	 * 
	 * @return la salle
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public SalleIdentifie getSalle(int identifiant) throws EdtempsException {

		SalleIdentifie salleRecuperee = null;

		try {

			// Récupère la salle en base
			ResultSet resultat = BddGestion
					.executeRequest("SELECT * FROM edt.salle WHERE salle_id="
							+ identifiant);

			// Accède au premier élément du résultat
			resultat.first();

			if (!resultat.wasNull()) {
				salleRecuperee = new SalleIdentifie();
				salleRecuperee.setId(resultat.getInt("salle_id"));
				salleRecuperee
						.setBatiment(resultat.getString("salle_batiment"));
				salleRecuperee.setNom(resultat.getString("salle_nom"));
				salleRecuperee.setNiveau(resultat.getInt("salle_niveau"));
				salleRecuperee.setNumero(resultat.getInt("salle_numero"));
				salleRecuperee.setCapacite(resultat.getInt("salle_capacite"));

				// TODO : MATERIEL

			}

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return salleRecuperee;

	}
}
