package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.SalleIdentifie;

/**
 * Classe de gestion des salles
 * 
 * @author Joffrey
 */
public class SalleGestion {

	protected BddGestion _bdd;

	/**
	 * Initialise un gestionnaire de salles
	 * 
	 * @param bdd
	 *            Gestionnaire de base de données à utiliser
	 */
	public SalleGestion(BddGestion bdd) {
		_bdd = bdd;
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
			ResultSet requeteSalle = _bdd
					.executeRequest("SELECT * FROM edt.salle WHERE salle_id='"
							+ identifiant + "'");

			// Accède au premier élément du résultat
			requeteSalle.next();

			if (!requeteSalle.wasNull()) {
				salleRecuperee = new SalleIdentifie();
				salleRecuperee.setId(requeteSalle.getInt("salle_id"));
				salleRecuperee.setBatiment(requeteSalle
						.getString("salle_batiment"));
				salleRecuperee.setNom(requeteSalle.getString("salle_nom"));
				salleRecuperee.setNiveau(requeteSalle.getInt("salle_niveau"));
				salleRecuperee.setNumero(requeteSalle.getInt("salle_numero"));
				salleRecuperee.setCapacite(requeteSalle
						.getInt("salle_capacite"));
				requeteSalle.close();

				// Récupérer la liste des matériels de la salle avec la quantité
				ResultSet requeteMateriel = _bdd
						.executeRequest("SELECT * FROM edt.contientmateriel WHERE salle_id="
								+ identifiant);
				while (requeteMateriel.next()) {
					salleRecuperee
							.getMateriels()
							.put(requeteMateriel.getInt("materiel_id"),
									requeteMateriel
											.getInt("contientmateriel_quantite"));
				}
				requeteMateriel.close();

			}

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return salleRecuperee;

	}

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
				if (StringUtils.isBlank(batiment)) {
					batiment = "";
				}
				String nom = salle.getNom();
				Integer niveau = salle.getNiveau();
				Integer numero = salle.getNumero();
				Integer capacite = salle.getCapacite();
				Map<Integer, Integer> materiels = salle.getMateriels();

				// Vérification de la cohérence des valeurs
				if (StringUtils.isNotBlank(nom)) {

					_bdd.executeRequest("INSERT INTO edt.salle (salle_batiment, salle_niveau, salle_numero, salle_capacite, salle_nom) VALUES ('"
							+ batiment
							+ "', '"
							+ niveau
							+ "', '"
							+ numero
							+ "', '" + capacite + "', '" + nom + "')");

					// Récupérer l'identifiant de la ligne qui vient d'être
					// ajoutée
					ResultSet resultat = _bdd
							.executeRequest("SELECT currval('edt.salle_salle_id_seq');");
					resultat.next();
					int lastInsert = resultat.getInt(1);

					// Ajout du lien avec les matériels
					for (Entry<Integer, Integer> materiel : materiels
							.entrySet()) {
						_bdd.executeRequest("INSERT INTO edt.contientmateriel (salle_id, materiel_id, contientmateriel_quantite) VALUES ('"
								+ lastInsert
								+ "', '"
								+ materiel.getKey()
								+ "', '" + materiel.getValue() + "')");
					}

				} else {
					throw new EdtempsException(ResultCode.DATABASE_ERROR,
							"Tentative d'enregistrer une salle en base de données sans nom.");
				}

			} catch (DatabaseException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			} catch (SQLException e) {
				throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
			}

		} else {
			throw new EdtempsException(ResultCode.DATABASE_ERROR,
					"Tentative d'enregistrer un objet NULL en base de données.");
		}

	}
}
