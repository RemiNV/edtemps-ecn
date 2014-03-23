package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.ecn.edtemps.models.JourFerie;
import org.ecn.edtemps.models.identifie.JourFerieIdentifie;

/**
 * Classe de gestion de jours fériés
 * 
 * @author Joffrey
 */
public class JourFerieGestion {

	protected BddGestion bdd;
	private static Logger logger = LogManager.getLogger(JourFerieGestion.class.getName());
	
	/** Nombre de caractères maximum pour le libellé d'un jour férié */
	public static final int TAILLE_MAX_LIBELLE = 50;
	
	/**
	 * Initialise un gestionnaire de jours fériés
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public JourFerieGestion(BddGestion bdd) {
		this.bdd = bdd;
	}

	
	/**
	 * Création d'un jour férié à partir d'une ligne de base de données.
	 * Colonnes nécessaires dans le ResultSet : jourferie_id, jourferie_libelle, jourferie_date
	 * 
	 * @param row Résultat de la requête placé sur la ligne à lire
	 * @return JourFerie créé
	 * @throws SQLException
	 */
	private JourFerieIdentifie inflateJourFerieFromRow(ResultSet row) throws SQLException {
		int id = row.getInt("jourferie_id");
		String libelle = row.getString("jourferie_libelle");
		Date date = row.getDate("jourferie_date");
		
		return new JourFerieIdentifie(id, libelle, date);
	}
	
	
	/**
	 * Récupérer la liste des jours fériés dans la base de données pour une période donnée
	 * 
	 * @param debut Début de la période de recherche
	 * @param fin Fin de la période de recherche
	 * @return la liste des jours fériés
	 * @throws EdtempsException 
	 */
	public ArrayList<JourFerieIdentifie> getJoursFeries(Date debut, Date fin) throws DatabaseException {
		
		try {

			String requeteString = "SELECT jourferie_id, jourferie_libelle, jourferie_date" +
					" FROM edt.joursferies" +
					" WHERE jourferie_date >= ? AND jourferie_date <= ?" +
					" ORDER BY jourferie_date";
			
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(requeteString);
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(debut.getTime()));
			requetePreparee.setTimestamp(2, new java.sql.Timestamp(fin.getTime()));
			
			
			// Récupère les jours en base
			ResultSet requete = requetePreparee.executeQuery();
			logger.info("Récupération de la liste des jours fériés dans la base de données, pour la période du "+debut+" au "+fin);

			// Parcours le résultat de la requête
			ArrayList<JourFerieIdentifie> listeJours = new ArrayList<JourFerieIdentifie>();
			while (requete.next()) {
				listeJours.add(this.inflateJourFerieFromRow(requete));
			}

			// Ferme la requete
			requete.close();

			return listeJours;

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}

	}
	

	/**
	 * Sauver un jour férié dans la base de données
	 * 
	 * @param libelle Libellé du jour férié
	 * @param date Date du jour férié
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @return l'identifiant de la ligne ajoutée
	 * @throws EdtempsException 
	 */
	public int sauverJourFerie(String libelle,  Date date, int userId) throws EdtempsException {

		// Quelques vérifications sur l'objet à enregistrer
		if (date==null || StringUtils.isEmpty(libelle)) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un jour férié doit avoir un libellé et une date");
		}
		
		// Vérifie que le libellé est bien alphanumérique
		if(libelle.length() > TAILLE_MAX_LIBELLE || !libelle.matches("^['a-zA-Z \u00C0-\u00FF0-9]+$")) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le libelle d'un jour férié doit être alphanumérique et de moins de " + TAILLE_MAX_LIBELLE + " caractères");
		}

		// Vérfie si un jour identique est déjà présent en base
		if (verifierPresenceJourFerie(date, null)) {
			throw new EdtempsException(ResultCode.DAY_TAKEN, "Il y a déjà un jour férié à cette date dans la base de données");
		}
				
		try {
						
			// Prépare la requête
			PreparedStatement requete = bdd.getConnection().prepareStatement("INSERT INTO edt.joursferies" +
					" (jourferie_libelle, jourferie_date) " +
					" VALUES (?, ?) " +
				    " RETURNING jourferie_id");
			requete.setString(1, libelle);
			requete.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
			
			// Exécute la requête
			ResultSet ligneCreee = requete.executeQuery();
			logger.info("Sauvegarde d'un jour férié");
			 
			// On récupère l'id du jour créé
			ligneCreee.next();
			int idLigneCree = ligneCreee.getInt("jourferie_id");
			ligneCreee.close();
			requete.close();
			
			return idLigneCree;

		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
	
	/**
	 * Supprimer un jour férié dans la base de données
	 * 
	 * @param id Identifiant (bdd) du jour férié à supprimer
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @throws EdtempsException 
	 */
	public void supprimerJourFerie(int id, int userId) throws EdtempsException {

		// Supprime le jour férié de la base de données à partir de l'identifiant
		bdd.executeUpdate("DELETE FROM edt.joursferies WHERE jourferie_id = " + id);
		logger.info("Suppression d'un jour férié");

	}
	
	
	/**
	 * Modifier un jour férié dans la base de données
	 * 
	 * @param jour Objet de type JourFerieIdentifie à modifier en base de donnée
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @throws EdtempsException 
	 */
	public void modifierJourFerie(JourFerieIdentifie jour, int userId) throws EdtempsException {

		// Quelques vérifications sur l'objet à modifier
		if (jour==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "La méthode pour sauver un jour férié doit recevoir un jour non null en paramètre");
		} else if (jour.getDate()==null || StringUtils.isEmpty(jour.getLibelle())) {
			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un jour férié doit avoir un nom et une date");
		}
		
		// Vérifie que le libellé est bien alphanumérique
		if(jour.getLibelle().length() > TAILLE_MAX_LIBELLE || !jour.getLibelle().matches("^['a-zA-Z \u00C0-\u00FF0-9]+$")) {
			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le libelle d'un jour férié doit être alphanumérique et de moins de " + TAILLE_MAX_LIBELLE + " caractères");
		}

		// Vérfie si un jour identique est déjà présent en base
		if (verifierPresenceJourFerie(jour.getDate(), jour.getId())) {
			throw new EdtempsException(ResultCode.DAY_TAKEN, "Il y a déjà un jour férié à cette date dans la base de données");
		}		
		
		try {
			
			// Prépare la requête
			PreparedStatement requete = bdd.getConnection().prepareStatement("UPDATE edt.joursferies" +
					" SET jourferie_libelle = ?, jourferie_date = ?" +
					" WHERE jourferie_id = " + jour.getId());
			requete.setString(1, jour.getLibelle());
			requete.setTimestamp(2, new java.sql.Timestamp(jour.getDate().getTime()));

			// Exécute la requête
			requete.execute();
			requete.close();
			logger.info("Modification d'un jour férié");
			
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
	
	/**
	 * Vérifier si un jour férié est présent à une date
	 * Possibilité d'ignorer un jour férié dans la recherche
	 * 
	 * @param date Date du jour à vérifier
	 * @param ignoreId Identifiant du jour à ignorer dans la recherche
	 * @return VRAI s'il existe déjà un jour férié à cette date en base de données
	 * @throws EdtempsException 
	 */
	public boolean verifierPresenceJourFerie(Date date, Integer ignoreId) throws EdtempsException {
		
		if (date==null) {
			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "La date ne doit pas être nulle");
		}
		
		try {
			
			// Préparation de la requête
			PreparedStatement requetePreparee = bdd.getConnection().prepareStatement("SELECT jourferie_id" +
					" FROM edt.joursferies WHERE jourferie_date = ?" +
					(ignoreId==null ? "" : " AND jourferie_id<>"+ignoreId));
			requetePreparee.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
			
			// Tente de récupérer le jour en base
			ResultSet requete = requetePreparee.executeQuery();

			// Vérifie si il existe un résultat
			boolean resultat = requete.next();

			// Ferme la requete
			requete.close();

			return resultat;
			
		} catch (SQLException e) {
			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
		}

	}
	
	

	/**
	 * Ajouter automatiquement tous les jours de l'année
	 * 
	 * @param annee Numéro de l'année de départ (par exemple, ce serait 2013 pour l'année scolaire 2013-2014)
	 * @param userId Identifiant de l'utilisateur qui fait la requête
	 * @return une map dont la clé vaut le libellé du jour férié et la valeur vaux true si il y a eu ajout, false sinon 
	 * @throws EdtempsException 
	 */
	public Map<String, Boolean> ajoutAutomatiqueJoursFeries(int annee, int userId) throws EdtempsException {

		// Récupère la liste des jours fériés pour l'année scolaire en cours
		List<JourFerie> listeJours = getJourFeries(annee);

		// Prépare une map pour stocker les résultats d'ajout
		Map<String, Boolean> mapJours = new HashMap<String, Boolean>(); 
		
		for (JourFerie jour : listeJours) {
			try {
				this.sauverJourFerie(jour.getLibelle(), jour.getDate(), userId);
				mapJours.put(jour.getLibelle(), true);
			} catch(EdtempsException e) {
				if (e.getResultCode()==ResultCode.DAY_TAKEN) {
					mapJours.put(jour.getLibelle(), false);
				} else {
					throw e;
				}
			}
		}
		
		return mapJours;

	}
	
	
	/**
	 * Recherche de la liste des jours fériés d'une année scolaire
	 * 
	 * @param annee Numéro de l'année de départ (par exemple, ce serait 2013 pour l'année scolaire 2013-2014)
	 * @return la liste des jours fériés de l'année scolaire annee<>annee+1
	 */
	public List<JourFerie> getJourFeries(int annee) {
		List<JourFerie> datesFeries = new ArrayList<JourFerie>();

		datesFeries.add(new JourFerie("Jour de l'an", new GregorianCalendar(annee+1, 0, 1).getTime()));
		datesFeries.add(new JourFerie("Fête du travail", new GregorianCalendar(annee+1, 4, 1).getTime()));
		datesFeries.add(new JourFerie("8 mai", new GregorianCalendar(annee+1, 4, 8).getTime()));
		datesFeries.add(new JourFerie("Armistice", new GregorianCalendar(annee, 10, 11).getTime()));
		datesFeries.add(new JourFerie("Noël", new GregorianCalendar(annee, 11, 25).getTime()));
		datesFeries.add(new JourFerie("Fête Nationale", new GregorianCalendar(annee+1, 6, 14).getTime()));
		datesFeries.add(new JourFerie("Assomption", new GregorianCalendar(annee+1, 7, 15).getTime()));
		datesFeries.add(new JourFerie("Toussaint", new GregorianCalendar(annee, 10, 1).getTime()));

		// Lundi de Pâques
		GregorianCalendar paques = calculLundiPaques(annee+1);
		datesFeries.add(new JourFerie("Lundi de Pâques", paques.getTime()));

		// Ascension (= Pâques + 38 jours)
		GregorianCalendar ascension = new GregorianCalendar(annee+1,
				paques.get(GregorianCalendar.MONTH),
				paques.get(GregorianCalendar.DAY_OF_MONTH));
		ascension.add(GregorianCalendar.DAY_OF_MONTH, 38);
		datesFeries.add(new JourFerie("Ascension", ascension.getTime()));

		// Pentecôte (= Pâques + 49 jours)
		GregorianCalendar pentecote = new GregorianCalendar(annee+1,
				paques.get(GregorianCalendar.MONTH),
				paques.get(GregorianCalendar.DAY_OF_MONTH));
		pentecote.add(GregorianCalendar.DAY_OF_MONTH, 49);
		datesFeries.add(new JourFerie("Pentecôte", pentecote.getTime()));

		return datesFeries;
	}
	
	
	/**
	 * Calcule la date du lundi de Pâques de l'année
	 * 
	 * @param annee Année de calcul
	 * @return la date du lundi de Pâques
	 */
	public GregorianCalendar calculLundiPaques(int annee) {
		int a = annee / 100;
		int b = annee % 100;
		int c = (3 * (a + 25)) / 4;
		int d = (3 * (a + 25)) % 4;
		int e = (8 * (a + 11)) / 25;
		int f = (5 * a + b) % 19;
		int g = (19 * f + c - e) % 30;
		int h = (f + 11 * g) / 319;
		int j = (60 * (5 - d) + b) / 4;
		int k = (60 * (5 - d) + b) % 4;
		int m = (2 * j - k - g + h) % 7;
		int n = (g - h + m + 114) / 31;
		int p = (g - h + m + 114) % 31;
		int jour = p + 1;
		int mois = n;

		GregorianCalendar date = new GregorianCalendar(annee, mois - 1, jour);
		date.add(GregorianCalendar.DAY_OF_MONTH, 1);
		return date;
	}
	
}
