package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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
	 * Créé une salle à partir d'une ligne de base de données
	 * @param row Résultat de requête placé à la ligne à lire
	 * @return Salle créée
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	private SalleIdentifie inflateSalleFromRow(ResultSet row) throws SQLException, DatabaseException {
		// Informations générales
		
		int id = row.getInt("salle_id");
		String batiment = row.getString("salle_batiment");
		String nom = row.getString("salle_nom");
		int niveau = row.getInt("salle_niveau");
		int numero = row.getInt("salle_numero");
		int capacite = row.getInt("salle_capacite");

		// Récupérer la liste des matériels de la salle avec la quantité
		ResultSet requeteMateriel = _bdd.executeRequest(
				"SELECT * "
				+ "FROM edt.contientmateriel "
				+ "INNER JOIN edt.materiel ON materiel.materiel_id = contientmateriel.materiel_id "
				+ "WHERE salle_id =" + id);
		
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		while (requeteMateriel.next()) {
			materiels.add(new Materiel(requeteMateriel.getInt("materiel_id"), requeteMateriel.getString("materiel_nom"), requeteMateriel.getInt("contientmateriel_quantite")));
		}
		
		requeteMateriel.close();
		
		SalleIdentifie res = new SalleIdentifie(id, batiment, nom, capacite, niveau, numero, materiels);
		
		return res;
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
			if (requeteSalle.next()) {
				salleRecuperee = inflateSalleFromRow(requeteSalle);
				requeteSalle.close();
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
				ArrayList<Materiel> materiels = salle.getMateriels();

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
					for (int i = 0; i<materiels.size(); i++) {
						_bdd.executeRequest(
								"INSERT INTO edt.contientmateriel "
								+ "(salle_id, materiel_id, contientmateriel_quantite) "
								+ "VALUES ('"
								+ id
								+ "', '"
								+ materiels.get(i).getId()
								+ "', '"
								+ materiels.get(i).getQuantite() + "')");
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
				ArrayList<Materiel> materiels = salle.getMateriels();

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
					for (int i = 0; i<materiels.size(); i++){
						_bdd.executeRequest(
								"INSERT INTO edt.contientmateriel "
								+ "(salle_id, materiel_id, contientmateriel_quantite) "
								+ "VALUES ('"
								+ lastInsert
								+ "', '"
								+ materiels.get(i).getId()
								+ "', '" + materiels.get(i).getQuantite() + "')");
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
	
	/**
	 * Listing des salles disponibles pour la création d'un nouvel événement
	 *@param dateDebut date de début de l'événement (paramètre obligatoire)
	 *@param dateFin date de fin de l'événement (paramètre obligatoire)
	 *@param matériels liste de matériel nécessaire dans la salle recherchée (paramètre facultatif, pouvant être null)
	 *@param capacité nombre de personne que la salle doit pouvoir accueillir (paramètre obligatoire)
	 *
	 *@return Liste des salles disponibles
	 *@throws DatabaseException
	 */
	public ArrayList<SalleIdentifie> rechercherSalle(Date dateDebut, Date dateFin, ArrayList<Materiel> materiels, int capacite) throws DatabaseException {
		try{		
			// sélection des ids des salles avec la capacité requise
			ArrayList<Integer> idsSallesAvecCapacite = new ArrayList<Integer>();
			idsSallesAvecCapacite.add(_bdd.recupererId(
					"SELECT DISTINCT * " 
					+ "FROM edt.salle "
					+ "WHERE salle_capacite >=" + capacite, "salle_id"));
			
			// sélection des ids des salles avec le matériel nécessaire en plus de la capacité requise
			ArrayList<Integer> idsSallesAvecMaterielEtCapacite = new ArrayList<Integer>();
			if (!materiels.isEmpty() && !idsSallesAvecCapacite.isEmpty()){			
				idsSallesAvecMaterielEtCapacite.add(_bdd.recupererId(
						"SELECT DISTINCT * " 
						+ "FROM edt.salle "
						+ "INNER JOIN edt.contientmateriel ON contientmateriel.salle_id = salle.salle_id " 
						+ "WHERE materiel_id = " + materiels.get(0).getId() + " "
						+ "AND contientmateriel_quantite >= " + materiels.get(0).getQuantite() + " "
						+ "AND salle.salle_id IN (" + getValuesSallesRetenues(idsSallesAvecCapacite) +")", "salle_id"));
				// suppression des salles ne possédant pas les autres matériels requis
				for (int i = 1; i < materiels.size(); i++){
					idsSallesAvecMaterielEtCapacite.remove(_bdd.recupererId(
							"SELECT DISTINCT * " 
							+ "FROM edt.salle "
							+ "INNER JOIN edt.contientmateriel ON contientmateriel.salle_id = salle.salle_id " 
							+ "WHERE materiel_id = " + materiels.get(i).getId() + " "
							+ "AND contientmateriel_quantite < " + materiels.get(i).getQuantite() + " "
							+ "AND salle.salle_id IN (" + getValuesSallesRetenues(idsSallesAvecMaterielEtCapacite) +")", "salle_id"));
				}
			}
			
			// sélection des ids des salles occupées
			ArrayList<Integer> idsSallesOccupees = new ArrayList<Integer>();
			PreparedStatement req = _bdd.getConnection().prepareStatement(
					"SELECT DISTINCT salle_id"
					+ "FROM edt.salle "
					+ "INNER JOIN edt.alieuensalle ON alienensalle.salle_id = salle.salle_id "
					+ "INNER JOIN edt.evenement ON evenement.eve_id = alieuensalle.eve_id "
					// événement dont le début est entre le début et la fin de l'événement pour lequel on cherche une salle
					+ "WHERE (evenement.eve_datedebut >= ? "
					+ "AND evenement.eve_datedebut <= ? )"
					// événement dont la fin est entre le début et la fin de l'événement pour lequel on cherche une salle
					+ "OR (evenement.eve_datefin >= ? "
					+ "AND evenement.eve_datefin <= ?) "
					// événements qui englobent l'événement pour lequel on cherche une salle
					+ "OR (evenement.eve_datedebut <= ? "
					+ "AND evenement.ev_datefin >= ?)");
			
			req.setTimestamp(1, new Timestamp(dateDebut.getTime()));
			req.setTimestamp(2, new Timestamp(dateFin.getTime()));
			req.setTimestamp(3, new Timestamp(dateDebut.getTime()));
			req.setTimestamp(4, new Timestamp(dateFin.getTime()));
			req.setTimestamp(5, new Timestamp(dateDebut.getTime()));
			req.setTimestamp(6, new Timestamp(dateFin.getTime()));
			
			ResultSet reponse = req.executeQuery();
			while(reponse.next()) {
				idsSallesOccupees.add(reponse.getInt("salle_id"));
			}
			reponse.close();
			
			// sélection des ids des salles disponibles, avec le matériel et la capacité requise
			ArrayList<Integer> idsSallesPossibles= idsSallesAvecMaterielEtCapacite;
			idsSallesPossibles.removeAll(idsSallesOccupees);
			
			// sélection des salles disponibles avec le matériel et la capacité requise
			ResultSet reponse2 = _bdd.executeRequest(
					"SELECT salle.salle_id, salle.salle_nom, salle.salle_batiment, salle.salle_niveau, " +
					"salle.salle_numero, salle.salle_capacite " +
					"FROM edt.salle " +
					"WHERE salle_id IN (" + getValuesSallesRetenues(idsSallesPossibles) +")");
			ArrayList<SalleIdentifie> res = new ArrayList<SalleIdentifie>();
			while(reponse2.next()) {
				res.add(inflateSalleFromRow(reponse2));
			}
			reponse2.close();
			return res;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}		
	}
	
	/**
	 * Récupération d'une string d'entiers à partir d'une liste d''entiers
	 * @param idsSallesRetenues liste d'entiers
	 * @return valuesIdsSallesRetenues string pouvant être utilisé dans une requête SQl de choix de valeurs
	 */
	private String getValuesSallesRetenues(ArrayList<Integer> idsSallesRetenues){
		String valuesIdsSallesRetenues = idsSallesRetenues.get(0).toString();
		for (int i = 1; i < idsSallesRetenues.size(); i++){
			valuesIdsSallesRetenues = valuesIdsSallesRetenues +", "	+ idsSallesRetenues.get(i);
		}
		return valuesIdsSallesRetenues;
	}
	
	
	/**
	 * Listing des salles disponibles pour la création d'un nouvel événement
	 *@param dateDebut date de début de l'événement (paramètre obligatoire)
	 *@param dateFin date de fin de l'événement (paramètre obligatoire)
	 *@param matériels liste de matériel nécessaire dans la salle recherchée (paramètre facultatif, pouvant être null)
	 *@param capacité nombre de personne que la salle doit pouvoir accueillir (paramètre obligatoire)
	 *
	 *@return Liste des salles disponibles
	 *@throws DatabaseException
	 */
	public ArrayList<SalleIdentifie> rechercherSalle2(Date dateDebut, Date dateFin, ArrayList<Materiel> materiels, int capacite) throws DatabaseException {

		// Recherche les salles qui ont la capacité suffisante et qui sont disponibles 
		ResultSet requete = _bdd.executeRequest(
		"SELECT * FROM edt.salle S WHERE (" +
			"S.salle_capacite >= " + capacite + " AND S.salle_id NOT IN (" +
				"SELECT A.salle_id FROM edt.alieuensalle A WHERE A.eve_id IN (" +
					"SELECT E.eve_id FROM edt.evenement E WHERE (" +
						"(E.eve_datedebut <= '" + dateDebut + "' AND '" + dateDebut + "' <= E.eve_datefin) OR (E.eve_datedebut <= '" + dateFin + "' AND '" + dateFin + "' <= E.eve_datefin)" +
		")   )   )   )");

		ArrayList<SalleIdentifie> resultatRecherche = new ArrayList<SalleIdentifie>();
		try {

			// Balayage pour chaque élément retour de la requête
			while(requete.next()) {

				// Transformation de la ligne de la bdd en objet java
				SalleIdentifie salle = inflateSalleFromRow(requete);

				// Pour chaque salle, vérifie si le matériel requis est présent
				boolean salleValide = true;
				for (Materiel mat : materiels) {
					salleValide &= salle.containMateriel(mat.getId(), mat.getQuantite());
				}
				if (salleValide) {
					resultatRecherche.add(salle);
				}
			}

			// Ferme la requête
			requete.close();

			// Retourne le résultat de la recherche
			return resultatRecherche;

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	/**
	 * Récupération des salles dans lesquelles se déroulent un évènement
	 * @param evenementId ID de l'évènement concerné
	 * @return Liste des salles enregistrées
	 * @throws DatabaseException 
	 */
	public ArrayList<SalleIdentifie> getSallesEvenement(int evenementId) throws DatabaseException {
		
		ResultSet reponse = _bdd.executeRequest("SELECT salle.salle_id, salle.salle_nom, salle.salle_batiment, salle.salle_niveau," +
				"salle.salle_numero, salle.salle_capacite " +
				"FROM edt.salle INNER JOIN edt.alieuensalle ON alieuensalle.salle_id = salle.salle_id " +
				"AND alieuensalle.eve_id = " + evenementId);
		
		ArrayList<SalleIdentifie> res = new ArrayList<SalleIdentifie>();
		try {
			while(reponse.next()) {
				res.add(inflateSalleFromRow(reponse));
			}
			
			reponse.close();
			
			return res;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
