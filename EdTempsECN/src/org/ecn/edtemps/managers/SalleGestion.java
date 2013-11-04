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
					.executeRequest("SELECT * FROM edt.salle WHERE salle_id=" + identifiant);

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
					PreparedStatement requete = _bdd.getConnection().prepareStatement(
							"UPDATE edt.salle SET" +
							" salle_batiment=?" +
							", salle_niveau=" + niveau +
							", salle_numero=" + numero +
							", salle_capacite=" + capacite +
							", salle_nom=? WHERE salle_id=" + id);
					requete.setString(1, batiment);
					requete.setString(2, nom);
					
					requete.execute();
					
					// Suppression de l'ancienne liste des matériels
					_bdd.executeRequest("DELETE FROM edt.contientmateriel WHERE salle_id="+id);

					// Ajout des nouveaux liens avec les matériels
					for (int i = 0; i<materiels.size(); i++) {
						_bdd.executeRequest(
								"INSERT INTO edt.contientmateriel " +
								"(salle_id, materiel_id, contientmateriel_quantite) VALUES (" +
								id + ", " +
								materiels.get(i).getId() + ", " +
								materiels.get(i).getQuantite() +")");
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
					PreparedStatement requete = _bdd.getConnection().prepareStatement(
							"INSERT INTO edt.salle (salle_batiment, salle_niveau, salle_numero, salle_capacite, salle_nom) VALUES (" +
							"?" + 
							"," + niveau + 
							", " + numero +
							", " + capacite +
							", ?) RETURNING salle_id");
					requete.setString(1, batiment);
					requete.setString(2, nom);
					
					// Exécute la requête
					ResultSet resultat = requete.executeQuery();
					
					// Récupère l'identifiant de la ligne ajoutée
					resultat.next();
					int lastInsert = resultat.getInt(1);
					resultat.close();

					// Ajout du lien avec les matériels
					for (int i = 0; i<materiels.size(); i++){
						_bdd.executeRequest(
								"INSERT INTO edt.contientmateriel " +
								"(salle_id, materiel_id, contientmateriel_quantite) VALUES (" +
								lastInsert + ", " +
								materiels.get(i).getId() + ", " +
								materiels.get(i).getQuantite() + ")");
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
			_bdd.executeRequest("DELETE FROM edt.contientmateriel WHERE salle_id=" + idSalle);

			// Supprime les liens avec les événements
			_bdd.executeRequest("DELETE FROM edt.alieuensalle WHERE salle_id=" + idSalle);

			// Supprime la salle
			_bdd.executeRequest("DELETE FROM edt.salle WHERE salle_id=" + idSalle);

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
	 * 
	 * @param dateDebut
	 *			date de début de l'événement
	 * @param dateFin
	 *			date de fin de l'événement
	 * @param materiels
	 *			liste de matériel nécessaire dans la salle recherchée
	 * @param capacite
	 *			nombre de personne que la salle doit pouvoir accueillir
	 *
	 * @return Liste des salles disponibles
	 * 
	 * @throws DatabaseException
	 */
	public ArrayList<SalleIdentifie> rechercherSalle(Date dateDebut, Date dateFin, ArrayList<Materiel> materiels, int capacite) throws DatabaseException {

		String requeteString =
		"SELECT salle.salle_id, salle.salle_batiment, salle.salle_niveau, salle.salle_nom, salle.salle_numero, salle.salle_capacite" +
	    " FROM edt.salle";
		
	    /* Join avec les matériels que la salle contient et qui sont nécessaires, si il y en a */
		if (!materiels.isEmpty()) {
			requeteString += " LEFT JOIN edt.contientmateriel ON salle.salle_id = contientmateriel.salle_id AND (";
			for (int i = 0 ; i < materiels.size() ; i++) {
				if (i!=0) {
					requeteString += " OR ";
				}
				requeteString += "(contientmateriel.materiel_id = "+materiels.get(i).getId()+" AND contientmateriel.contientmateriel_quantite >= "+materiels.get(i).getQuantite()+")";
			}
			requeteString += ")";
		}

		requeteString += " LEFT JOIN edt.alieuensalle ON alieuensalle.salle_id = salle.salle_id" +
	    " LEFT JOIN edt.evenement ON evenement.eve_id = alieuensalle.eve_id" +
	    " AND (evenement.eve_datedebut < ?) AND (evenement.eve_datefin > ?)" + /* Join avec les évènements qui se passent dans la salle au créneau demandé */
	    " WHERE evenement.eve_id IS NULL" + /* Aucun évènement qui se passe dans la salle au créneau demandé (LEFT JOIN, donc aucune correspondance -> colonnes null) */
	    " AND salle.salle_capacite>=" + capacite + /* Vérifie la capacité de la salle */
	    " GROUP BY salle.salle_id"; /* On somme les matériels *par salle* */
		
		if(!materiels.isEmpty()) {
			/* Le nombre de types de matériels que la salle contient et qui sont nécessaires correspond avec le nombre de matériels demandés */
			requeteString += " HAVING COUNT(DISTINCT contientmateriel.materiel_id) = "+materiels.size();
		}
	    
		requeteString += " ORDER BY salle.salle_capacite";

		ArrayList<SalleIdentifie> resultatRecherche = new ArrayList<SalleIdentifie>();
		try {
			// Prépare la requête
			PreparedStatement requetePreparee = _bdd.getConnection().prepareStatement(requeteString);
			requetePreparee.setTimestamp(1, new Timestamp(dateFin.getTime()));
			requetePreparee.setTimestamp(2, new Timestamp(dateDebut.getTime()));
			
		    // Effectue la requête
			ResultSet requete = requetePreparee.executeQuery();

			// Balayage pour chaque élément retour de la requête
			while(requete.next()) {
				resultatRecherche.add(inflateSalleFromRow(requete));
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
