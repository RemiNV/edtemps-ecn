package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Groupe;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;

/**
 * Classe de gestion des groupes de gestion
 * 
 * @author Joffrey
 */
public class GroupeGestion {

	protected BddGestion _bdd;

	protected CalendrierGestion gestionnaireCalendriers;

	/**
	 * Initialise un gestionnaire de groupes de participants
	 * 
	 * @param bdd
	 *            Gestionnaire de base de données à utiliser
	 */
	public GroupeGestion(BddGestion bdd) {
		_bdd = bdd;
	}

	/**
	 * Récupérer un groupe de participants dans la base de données
	 * 
	 * @param identifiant
	 *            identifiant du groupe à récupérer
	 * 
	 * @return le groupe
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur de connexion avec la base de données
	 */
	public GroupeIdentifie getGroupe(int identifiant) throws EdtempsException {

		GroupeIdentifie groupeRecuperee = null;

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Récupère le groupe en base
			ResultSet requeteGroupe = _bdd
					.executeRequest("SELECT * FROM edt.groupedeparticipant WHERE groupeparticipant_id='"
							+ identifiant + "'");

			// Accède au premier élément du résultat
			requeteGroupe.next();

			if (!requeteGroupe.wasNull()) {

				// Informations générales
				groupeRecuperee = new GroupeIdentifie();
				groupeRecuperee.setId(requeteGroupe
						.getInt("groupeparticipant_id"));
				groupeRecuperee.setNom(requeteGroupe
						.getString("groupeparticipant_nom"));
				groupeRecuperee.setRattachementAutorise(requeteGroupe
						.getBoolean("groupeparticipant_rattachementautorise"));
				groupeRecuperee.setParentId(requeteGroupe
						.getInt("groupedeparticipant_id_parent"));
				requeteGroupe.close();

				// Récupérer la liste des identifiants des calendriers */
				ResultSet requeteCalendriers = _bdd
						.executeRequest("SELECT * FROM edt.calendrierappartientgroupe WHERE groupeparticipant_id="
								+ identifiant);
				while (requeteCalendriers.next()) {
					groupeRecuperee.getIdCalendriers().add(
							requeteCalendriers.getInt("cal_id"));
				}
				requeteCalendriers.close();

				// Récupérer la liste des identifiants des propriétaires */
				ResultSet requeteProprietaires = _bdd
						.executeRequest("SELECT * FROM edt.proprietairegroupedeparticipant WHERE groupeparticipant_id="
								+ identifiant);
				while (requeteProprietaires.next()) {
					groupeRecuperee.getIdProprietaires().add(
							requeteCalendriers.getInt("utilisateur_id"));
				}
				requeteProprietaires.close();

			}

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

		return groupeRecuperee;

	}

	/**
	 * Groupe à enregistrer en base de données
	 * 
	 * @param groupe
	 *            groupe à sauver
	 * 
	 * @return l'identifiant de la ligne insérée
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public int sauverGroupe(Groupe groupe) throws EdtempsException {

		int idInsertion = -1;

		if (groupe != null) {

			try {

				// Démarre une transaction
				_bdd.startTransaction();

				// Récupération des arguments sur le groupe
				String nom = groupe.getNom();
				if (StringUtils.isBlank(nom)) {
					nom = "";
				}
				int parentId = groupe.getParentId();
				boolean ratachementAutorise = groupe.getRattachementAutorise();

				// Vérification de la cohérence des valeurs
				if (StringUtils.isNotBlank(nom)) {

					// Ajoute le groupe dans la bdd et récupère l'identifiant de
					// la ligne
					ResultSet resultat = _bdd
							.executeRequest("INSERT INTO edt.groupedeparticipant (groupeparticipant_nom, groupeparticipant_rattachementautorise, groupedeparticipant_id_parent) VALUES ('"
									+ nom
									+ "', '"
									+ ratachementAutorise
									+ "', '"
									+ parentId
									+ "') RETURNING groupeparticipant_id ");
					resultat.next();
					idInsertion = resultat.getInt(1);
					resultat.close();

					// Ajout des propriétaires
					if (CollectionUtils.isNotEmpty(groupe.getIdProprietaires())) {
						for (Integer idProprietaire : groupe
								.getIdProprietaires()) {
							_bdd.executeRequest("INSERT INTO edt.ProprietaireGroupedeParticipant (utilisateur_id, groupeParticipant_id) VALUES ('"
									+ idProprietaire
									+ "', '"
									+ idInsertion
									+ "')");
						}
					} else {
						throw new EdtempsException(ResultCode.DATABASE_ERROR,
								"Tentative d'enregistrer un groupe en base de données sans propriétaire.");
					}

				} else {
					throw new EdtempsException(ResultCode.DATABASE_ERROR,
							"Tentative d'enregistrer un groupe en base de données sans nom.");
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

		return idInsertion;
	}

	/**
	 * Supprime un groupe en base de données
	 * 
	 * @param idGroupe
	 *            identifiant du groupe à supprimer
	 * 
	 * @throws EdtempsException
	 *             en cas d'erreur
	 */
	public void supprimerGroupe(int idGroupe) throws EdtempsException {

		// Initialise le gestionnaire des calendriers avec l'accès à la base de
		// données déjà créé ici
		this.gestionnaireCalendriers = new CalendrierGestion(_bdd);

		try {

			// Démarre une transaction
			_bdd.startTransaction();

			// Supprime les calendriers
			ResultSet listeCalendriers = _bdd
					.executeRequest("SELECT cal_id FROM edt.calendrierappartientgroupe WHERE groupeParticipant_id='"
							+ idGroupe + "'");
			while (listeCalendriers.next()) {
				this.gestionnaireCalendriers
						.supprimerCalendrier(listeCalendriers.getInt(1));
			}
			listeCalendriers.close();

			// Supprime les liens avec les propriétaires
			_bdd.executeRequest("DELETE FROM edt.ProprietaireGroupedeParticipant WHERE groupeParticipant_id='"
					+ idGroupe + "'");

			// Supprime les abonnements
			_bdd.executeRequest("DELETE FROM edt.AbonneGroupeParticipant WHERE groupeParticipant_id='"
					+ idGroupe + "'");

			// Supprime le groupe
			_bdd.executeRequest("DELETE FROM edt.GroupedeParticipant WHERE groupeParticipant_id='"
					+ idGroupe + "'");

			// Termine la transaction
			_bdd.commit();

		} catch (DatabaseException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
}
