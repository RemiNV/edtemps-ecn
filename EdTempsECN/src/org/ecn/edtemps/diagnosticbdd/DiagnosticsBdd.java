package org.ecn.edtemps.diagnosticbdd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResult;
import org.ecn.edtemps.diagnosticbdd.TestBdd.TestBddResultCode;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.managers.GroupeGestion;
import org.ecn.edtemps.managers.UtilisateurGestion;

public class DiagnosticsBdd {
	
	Logger logger = LogManager.getLogger(DiagnosticsBdd.class.getName());

	protected BddGestion bdd;
	
	public DiagnosticsBdd(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	public ArrayList<TestBddResult> runAllTests() {
		int nbTests = 5; // Nombre de tests gérés dans createTest (et de clauses dans le switch)
		
		ArrayList<TestBddResult> res = new ArrayList<TestBddResult>(nbTests);
		
		for(int i=1; i<=nbTests; i++) {
			TestBdd test = createTest(i);
			
			try {
				bdd.startTransaction();
				res.add(test.test(bdd));
				bdd.commit();
			}
			catch(DatabaseException e) {
				try { 
					bdd.rollback();
				}
				catch(DatabaseException exRollback) {
					logger.error("Erreur lors de l'execution d'un rollback lors des tests", exRollback);
				}
				
				String errMessage = "Echec de l'exécution du test \"" + test.getNom() + "\" : " + e.getMessage();
				res.add(new TestBddResult(TestBddResultCode.TEST_FAILED, errMessage	+ ", examinez les logs du serveur pour la pile d'appel", test));
				
				logger.error(errMessage, e);
			}
		}
		
		return res;
	}
	
	/**
	 * Créé un test 
	 * @param idTest
	 * @return
	 */
	public TestBdd createTest(int idTest) {
		switch(idTest) {
		
		case 1:
			return createTestCalendrierPossedeGroupeUnique(1);
		
		case 2:
			return createTestGroupeUniquePossedeCalendrier(2);
			
		case 3:
			return createTestEvenementPossedeCalendrier(3);
			
		case 4:
			return createTestParenteCirculaireGroupes(4);
			
		case 5:
			return createTestVieuxComptesUtilisateur(5);
			
			// TODO : ajouter une vérification des groupes n'ayant pas de propriétaire
			// TODO : ajouter une vérification des calendriers n'ayant pas propriétaire
			// TODO : ajouter une vérification des événements n'ayant pas de propriétaire
		
		default:
			return null;
		}
	}
	
	protected String getStrPremiersIds(List<Integer> ids) {
		List<Integer> idsAffichage = ids.subList(0, Math.min(4, ids.size()));
		String strAutres = ids.size() > 5 ? "..." : "";
		
		return StringUtils.join(idsAffichage, ", ") + strAutres;
	}
	
	protected TestBdd createTestCalendrierPossedeGroupeUnique(int id) {
		return new TestBdd("Rattachement de tous les calendriers à un groupe de participants \"groupe unique\"", id, "Ajouter les groupes manquants") {
			
			protected ArrayList<Integer> getCalendriersSansGroupeUnique(BddGestion bdd) throws DatabaseException {
				
				try {
					ArrayList<Integer> res = bdd.recupererIds(bdd.getConnection().prepareStatement(
							"SELECT calendrier.cal_id FROM edt.calendrier " +
							"LEFT JOIN edt.calendrierappartientgroupe cag ON calendrier.cal_id=cag.cal_id " +
							"LEFT JOIN edt.groupeparticipant groupeunique ON cag.groupeparticipant_id=groupeunique.groupeparticipant_id " +
							"AND groupeunique.groupeparticipant_estcalendrierunique " +
							"GROUP BY calendrier.cal_id " +
							"HAVING COUNT(groupeunique.groupeparticipant_id)=0"), "cal_id");
					
					return res;
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
			}
			
			@Override
			public TestBddResult test(BddGestion bdd) throws DatabaseException {
				
				ArrayList<Integer> cals = getCalendriersSansGroupeUnique(bdd);
				
				if(cals.size() == 0) {
					return new TestBddResult(TestBddResultCode.OK, "Les calendriers sont tous associés à au moins un groupe unique", this);
				}
				else {
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains calendriers (ID " + getStrPremiersIds(cals) + ") n'ont pas de groupe unique", this);
				}
			}

			@Override
			public String repair(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> cals = getCalendriersSansGroupeUnique(bdd);
				
				try {
					for(Integer idCal : cals) {
						ResultSet reponse = bdd.executeRequest("INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, " +
								"groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
								"SELECT cal_nom, FALSE, FALSE, TRUE FROM edt.calendrier WHERE cal_id = " + idCal + " " +
								"RETURNING groupeparticipant_id");
						
						reponse.next();
						int idGroupe = reponse.getInt(1);
						reponse.close();
						
						bdd.executeUpdate("INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id) VALUES(" + idGroupe + "," + idCal + ")");
					}
					
					return cals.size() + " groupe uniques ajoutés.";
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
				
			}
		};
	}
	
	protected TestBdd createTestGroupeUniquePossedeCalendrier(int id) {
		return new TestBdd("Rattachement de chaque \"groupe unique\" à un calendrier", id, "Supprimer les groupes inutiles") {
			
			protected ArrayList<Integer> getIdsGroupesUniquesSansCalendrier(BddGestion bdd) throws DatabaseException {
				
				try {
					ArrayList<Integer> res = bdd.recupererIds(bdd.getConnection().prepareStatement(
							"SELECT groupeparticipant.groupeparticipant_id FROM edt.groupeparticipant " +
							"LEFT JOIN edt.calendrierappartientgroupe cag ON cag.groupeparticipant_id=groupeparticipant.groupeparticipant_id " +
							"WHERE groupeparticipant.groupeparticipant_estcalendrierunique " +
							"GROUP BY groupeparticipant.groupeparticipant_id " +
							"HAVING COUNT(cag.cal_id) = 0"), "groupeparticipant_id");
					
					return res;
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
			}
			
			@Override
			public TestBddResult test(BddGestion bdd) throws DatabaseException {
				
				ArrayList<Integer> idGroupesUniques = getIdsGroupesUniquesSansCalendrier(bdd);
				
				if(idGroupesUniques.size() == 0) {
					return new TestBddResult(TestBddResultCode.OK, "Les groupes uniques sont tous associés à au moins un calendrier", this);
				}
				else {
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains groupes uniques (ID " + getStrPremiersIds(idGroupesUniques) + ") n'ont pas de calendrier", this);
				}
			}

			@Override
			public String repair(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> idGroupesUniques = getIdsGroupesUniquesSansCalendrier(bdd);
				
				GroupeGestion groupeGestion = new GroupeGestion(bdd);
				
				for(int id : idGroupesUniques) {
					groupeGestion.supprimerGroupe(id, false, true);
				}
				
				return idGroupesUniques.size() + " groupes supprimés";
			}
			
		};
	}
	
	protected TestBdd createTestEvenementPossedeCalendrier(int id) {
		return new TestBdd("Rattachement de chaque événement à au moins un calendrier", id, "Supprimer les événements inutiles") {

			protected ArrayList<Integer> getIdsEvenementsSansCalendrier(BddGestion bdd) throws DatabaseException {
				
				try {
					ArrayList<Integer> res = bdd.recupererIds(bdd.getConnection().prepareStatement(
							"SELECT evenement.eve_id FROM edt.evenement " +
							"LEFT JOIN edt.evenementappartient ON evenementappartient.eve_id=evenement.eve_id " +
							"GROUP BY evenement.eve_id " +
							"HAVING COUNT(evenementappartient.cal_id) = 0"), "eve_id");
					
					return res;
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
			}
			
			@Override
			public TestBddResult test(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> idEvenements = getIdsEvenementsSansCalendrier(bdd);
				
				if(idEvenements.size() == 0) {
					return new TestBddResult(TestBddResultCode.OK, "Les événements sont tous associés à au moins un calendrier", this);
				}
				else {
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains événements (ID " + getStrPremiersIds(idEvenements) + ") n'ont pas de calendrier", this);
				}
			}

			@Override
			public String repair(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> idEvenements = getIdsEvenementsSansCalendrier(bdd);
				
				EvenementGestion evenementGestion = new EvenementGestion(bdd);
				
				for(int id : idEvenements) {
					evenementGestion.supprimerEvenement(id, false);
				}
				
				return idEvenements.size() + " événements supprimés";
			}
			
		};
	}
	
	protected TestBdd createTestParenteCirculaireGroupes(int id) {
		// Principe : à chaque itération on remonte un lien de parenté. Si on retombe sur le groupe de départ à une itération, il y a lien circulaire.
		// On parcourt tous les liens de tous les groupes de manière parallèle
		return new TestBdd("Liens de parenté circulaire entre les groupes de participants", id, "Briser tous les liens circulaires (tous les liens de chaque boucle seront supprimés !)") {

			private void createTempTableParente(BddGestion bdd) throws DatabaseException {
				bdd.executeUpdate("CREATE TEMP TABLE tmp_derniers_parents(groupe_id INTEGER, iteration INTEGER, groupe_point_depart INTEGER, " +
						"PRIMARY KEY (groupe_id, iteration, groupe_point_depart)) ON COMMIT DROP");
				
				bdd.executeUpdate("INSERT INTO tmp_derniers_parents(groupe_id, iteration, groupe_point_depart) " +
						"SELECT groupeparticipant_id, 0, groupeparticipant_id FROM edt.groupeparticipant WHERE groupeparticipant_id_parent IS NOT NULL");
				
				// Première itération (sans exclure les liens partant de groupes égaux au point de départ : on n'est pas à la fin d'une boucle mais au début)
				bdd.executeUpdate("INSERT INTO tmp_derniers_parents(groupe_id, iteration, groupe_point_depart) " +
						"SELECT DISTINCT groupeparticipant.groupeparticipant_id_parent, 1, tmp_derniers_parents.groupe_point_depart FROM edt.groupeparticipant " +
						"INNER JOIN tmp_derniers_parents ON tmp_derniers_parents.groupe_id = groupeparticipant.groupeparticipant_id AND tmp_derniers_parents.iteration = 0 " +
						"WHERE groupeparticipant_id_parent IS NOT NULL");
			}
			
			private PreparedStatement makeStatementIteration(BddGestion bdd) throws SQLException {
				return bdd.getConnection().prepareStatement("INSERT INTO tmp_derniers_parents(groupe_id, iteration, groupe_point_depart) " +
						"SELECT DISTINCT groupeparticipant.groupeparticipant_id_parent, ?, tmp_derniers_parents.groupe_point_depart FROM edt.groupeparticipant " +
						"INNER JOIN tmp_derniers_parents ON tmp_derniers_parents.groupe_id = groupeparticipant.groupeparticipant_id AND tmp_derniers_parents.iteration = ? " +
						"AND tmp_derniers_parents.groupe_id <> tmp_derniers_parents.groupe_point_depart " +
						"WHERE groupeparticipant_id_parent IS NOT NULL");
			}
			
			/**
			 * Donne des IDs de groupes impliqués dans un lien circulaire. Plusieurs groupes peuvent faire partie du même lien.
			 * @return IDs des groupes, la liste peut être vide
			 * @throws DatabaseException
			 */
			private ArrayList<Integer> getIdsGroupesLienCirculaire() throws DatabaseException {
				try {
					
					PreparedStatement statementIteration = makeStatementIteration(bdd);
					
					createTempTableParente(bdd);
					ResultSet reponse = null;
					for(int idIteration=2, nbParents=1; nbParents > 0; idIteration++) {
						
						statementIteration.setInt(1, idIteration);
						statementIteration.setInt(2, idIteration - 1);
						
						nbParents = statementIteration.executeUpdate();
					}
					
					statementIteration.close();
				
					// Récupération des groupes impliqués dans un lien circulaire
					reponse = bdd.executeRequest("SELECT groupe_id FROM tmp_derniers_parents " +
							"WHERE groupe_id = groupe_point_depart AND iteration <> 0");
					
					ArrayList<Integer> res = new ArrayList<Integer>();
					while(reponse.next()) {
						res.add(reponse.getInt(1));
					}
					
					reponse.close();
					
					return res;
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
			}
			
			
			@Override
			public TestBddResult test(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> groupesLienCirculaire = getIdsGroupesLienCirculaire();
				
				if(groupesLienCirculaire.size() == 0) {
					return new TestBddResult(TestBddResultCode.OK, "Aucun lien de parenté circulaire", this);
				}
				else {
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains groupes (ID " + getStrPremiersIds(groupesLienCirculaire) + ") ont des liens circulaires", this);
				}
				
			}

			@Override
			public String repair(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> groupesLienCirculaire = getIdsGroupesLienCirculaire();
				
				if(groupesLienCirculaire.size() == 0) {
					return "Aucun lien circulaire trouvé";
				}
				
				String strIds = StringUtils.join(groupesLienCirculaire, ",");
				
				bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent = NULL WHERE groupeparticipant_id IN (" + strIds + ")");
				
				return groupesLienCirculaire.size() + " liens de groupes vers leurs parents mis à NULL";
			}
			
		};
	}
	
	protected TestBdd createTestVieuxComptesUtilisateur(int id) {
		return new TestBdd("Présence de comptes d'utilisateur inutilisés depuis 2 ans ou plus", id, "Désactiver ces comptes utilisateur") {

			protected ArrayList<Integer> getIdsComptes(BddGestion bdd) throws DatabaseException {
				try {
				return bdd.recupererIds(bdd.getConnection().prepareStatement("SELECT utilisateur_id FROM edt.utilisateur " +
						"WHERE utilisateur_active AND utilisateur_token_expire IS NOT NULL AND utilisateur_token_expire < now() - interval '2 years'"), "utilisateur_id");
				}
				catch(SQLException e) {
					throw new DatabaseException(e);
				}
			}
			
			@Override
			public TestBddResult test(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> idComptes = getIdsComptes(bdd);
				if(idComptes.size() > 0) {
					return new TestBddResult(TestBddResultCode.WARNING, "Des comptes (ID " + getStrPremiersIds(idComptes) + ") inutilisés ont été trouvés", this);
				}
				else {
					return new TestBddResult(TestBddResultCode.OK, "Aucun compte inutilisé trouvé", this);
				}
			}

			@Override
			public String repair(BddGestion bdd) throws DatabaseException {
				ArrayList<Integer> idComptes = getIdsComptes(bdd);
				UtilisateurGestion utilisateurGestion = new UtilisateurGestion(bdd);
				for(int id : idComptes) {
					utilisateurGestion.desactiverUtilisateur(id, false);
				}
				
				return idComptes.size() + " comptes d'utilisateur désactivés";
			}
			
		};
	}
}
