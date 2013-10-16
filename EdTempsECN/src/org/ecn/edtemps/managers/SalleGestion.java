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

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère la salle en base
			ResultSet requeteSalle = _bdd
					.executeRequest("SELECT * FROM edt.salle WHERE salle_id='"
							+ identifiant + "'");

			// Accède au premier élément du résultat
			requeteSalle.next();

			if (!requeteSalle.wasNull()) {

				// Informations générales
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

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return salleRecuperee;

	}

	/**
	 * Modifie une salle en base de données
	 * 
	 * @param salle
	 *            salle à modifier
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public void modifierSalle(SalleIdentifie salle) throws EdtempsException {

		if (salle != null) {

			try {
				// Récupération des nouvelles informations sur la salle
				int id = salle.getId();
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

					// Démarre une transaction
					_bdd.startTransaction();

					// Modifie les informations sur la salle
					_bdd.executeRequest("UPDATE edt.salle SET salle_batiment='"
							+ batiment + "', salle_niveau='" + niveau
							+ "', salle_numero='" + numero
							+ "', salle_capacite='" + capacite
							+ "', salle_nom='" + nom + "' WHERE salle_id='"
							+ id + "'");

					// Suppression de l'ancienne liste des matériels
					_bdd.executeRequest("DELETE FROM edt.contientmateriel WHERE salle_id='"
							+ id + "'");

					// Ajout des nouveaux liens avec les matériels
					for (Entry<Integer, Integer> materiel : materiels
							.entrySet()) {
						_bdd.executeRequest("INSERT INTO edt.contientmateriel (salle_id, materiel_id, contientmateriel_quantite) VALUES ('"
								+ id
								+ "', '"
								+ materiel.getKey()
								+ "', '"
								+ materiel.getValue() + "')");
					}

					// Termine la transaction
					_bdd.commit();

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
					"Tentative de modifier un objet NULL en base de données.");
		}

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

				// Démarre une transaction
				_bdd.startTransaction();

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

					// Ajoute la salle dans la bdd et récupère l'identifiant de
					// la ligne
					ResultSet resultat = _bdd
							.executeRequest("INSERT INTO edt.salle (salle_batiment, salle_niveau, salle_numero, salle_capacite, salle_nom) VALUES ('"
									+ batiment
									+ "', '"
									+ niveau
									+ "', '"
									+ numero
									+ "', '"
									+ capacite
									+ "', '"
									+ nom
									+ "') RETURNING salle_id");
					resultat.next();
					int lastInsert = resultat.getInt(1);
					resultat.close();

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

				// Termine la transaction
				_bdd.commit();

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

	/**
	 * Supprime une salle en base de données
	 * 
	 * @param idSalle
	 *            identifiant de la salle à supprimer
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public void supprimerSalle(int idSalle) throws EdtempsException {

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Supprime le matériel
			_bdd.executeRequest("DELETE FROM edt.contientmateriel WHERE salle_id='"
					+ idSalle + "'");

			// Supprime les liens avec les événements
			_bdd.executeRequest("DELETE FROM edt.alieuensalle WHERE salle_id='"
					+ idSalle + "'");

			// Supprime la salle
			_bdd.executeRequest("DELETE FROM edt.salle WHERE salle_id='"
					+ idSalle + "'");

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
}
