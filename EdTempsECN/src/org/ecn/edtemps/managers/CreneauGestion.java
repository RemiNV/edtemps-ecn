package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.Creneau;
import org.ecn.edtemps.models.identifie.CreneauIdentifie;

/**
 * Gestionnaire des créneaux horaires
 * 
 * @author joffrey
 */
public class CreneauGestion {
	
	protected BddGestion bdd;
	private static Logger logger = LogManager.getLogger(CreneauGestion.class.getName());
	
	/** Nombre de caractères maximum pour le libellé d'un créneau */
	public static final int TAILLE_MAX_LIBELLE = 5;
	
	/**
	 * Initialise un gestionnaire de créneaux
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public CreneauGestion(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	
	/**
	 * Création d'un créneau à partir d'une ligne de base de données
	 * Colonnes nécessaires dans le ResultSet : creneau_id, creneau_libelle, creneau_debut, creneau_fin
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return Creneau créé
	 * @throws SQLException
	 */
	private CreneauIdentifie inflateCreneauFromRow(ResultSet row) throws SQLException {
		int id = row.getInt("creneau_id");
		String libelle = row.getString("creneau_libelle");
		Date debut = row.getDate("creneau_debut");
		Date fin = row.getDate("creneau_fin");
		
		return new CreneauIdentifie(id, libelle, debut, fin);
	}
	

	/**
	 * Récupérer la liste des créneaux
	 * 
	 * @return la liste des créneaux
	 * @throws EdtempsException 
	 */
	public ArrayList<CreneauIdentifie> getCreneaux() throws DatabaseException {
		
		try {
			
			ArrayList<CreneauIdentifie> resultat = new ArrayList<CreneauIdentifie>();

			ResultSet requete = bdd.executeRequest("SELECT * FROM edt.creneau");
			logger.info("Récupération de la liste des créneaux");

			while (requete.next()) {
				resultat.add(this.inflateCreneauFromRow(requete));
			}

			requete.close();

			return resultat;

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

	}
	
	
	/**
	 * Créer un créneau
	 * @param creneau Créneau à ajouter
	 * @throws EdtempsException 
	 */
	public int creer(Creneau creneau) throws EdtempsException {
		
		creneauValide(creneau);
		
		try {
			
			// On crée le créneau dans la base de données
			PreparedStatement req = bdd.getConnection().prepareStatement(
					"INSERT INTO edt.creneau (creneau_libelle, creneau_debut, creneau_fin) VALUES (?, ?, ?) RETURNING creneau_id");
			
			req.setString(1, creneau.getLibelle());
			req.setTimestamp(2, new java.sql.Timestamp(creneau.getDebut().getTime()));
			req.setTimestamp(3, new java.sql.Timestamp(creneau.getFin().getTime()));
			
			ResultSet rsLigneCreee = req.executeQuery();
			 
			// On récupère l'id du créneau créé
			rsLigneCreee.next();
			int idCreneau = rsLigneCreee.getInt("creneau_id");
			rsLigneCreee.close();

			logger.info("Ajout d'un créneau en base de données (id="+idCreneau+")");
			
			return idCreneau;

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		
	}
	

	/**
	 * Modifier un créneau
	 * @param creneau Créneau à modifier
	 * @throws EdtempsException 
	 */
	public void modifier(CreneauIdentifie creneau) throws EdtempsException {
		
		creneauValide(creneau);
		
		try {
			
			PreparedStatement req = bdd.getConnection().prepareStatement(
					"UPDATE edt.creneau SET creneau_libelle=?, creneau_debut=?, creneau_fin=? WHERE creneau_id");
			
			req.setString(1, creneau.getLibelle());
			req.setTimestamp(2, new java.sql.Timestamp(creneau.getDebut().getTime()));
			req.setTimestamp(3, new java.sql.Timestamp(creneau.getFin().getTime()));
			
			logger.info("Modification d'un créneau en base de données (id="+creneau.getId()+")");

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
			
	}
	
	
	/**
	 * Supprimer un créneau
	 * @param id
	 */
	public void supprimer(int id) throws DatabaseException {
		bdd.executeUpdate("DELETE FROM edt.creneau WHERE creneau_id="+id);
		logger.info("Suppression d'un créneau (id="+id+")");
	}
	
	
	/**
	 * Vérifie qu'un créneau est valide
	 * @param creneau
	 * @throws EdtempsException
	 */
	public void creneauValide(Creneau creneau) throws EdtempsException {
		
		// Vérifie que l'objet à créer est complet
		if (creneau==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "La méthode pour sauver un créneau doit recevoir un objet non null en paramètre");
		} else if (creneau.getDebut()==null || creneau.getFin()==null || StringUtils.isEmpty(creneau.getLibelle())) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un créneau doit avoir un libellé, un horaire de début et un horaire de fin");
		}
		
		// Vérifie que le libellé est bien alphanumérique
		if(creneau.getLibelle().length() > TAILLE_MAX_LIBELLE || !creneau.getLibelle().matches("^['a-zA-Z \u00C0-\u00FF0-9]+$")) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le libelle d'un créneau doit être alphanumérique et de moins de " + TAILLE_MAX_LIBELLE + " caractères");
		}

		// Vérifie que les horaires sont dans l'ordre
		if (creneau.getDebut().getTime() > creneau.getFin().getTime()) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Les horaires ne sont pas cohérents (l'horaire de début doit être inférieur à l'horaire de fin)");
		}
		
	}
}
