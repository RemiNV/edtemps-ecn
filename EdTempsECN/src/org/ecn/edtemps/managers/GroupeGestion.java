package org.ecn.edtemps.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

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
	 */
	public void sauverGroupe(Groupe groupe) {

		if (groupe != null) {

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

}
