package org.ecn.edtemps.diagnosticbdd;

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

public class DiagnosticsBdd {
	
	Logger logger = LogManager.getLogger(DiagnosticsBdd.class.getName());

	protected BddGestion bdd;
	
	public DiagnosticsBdd(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	public ArrayList<TestBddResult> runAllTests() {
		int nbTests = 3; // Nombre de tests gérés dans createTest
		
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
			
			// TODO : ajouter une vérification de l'absence de liens de parenté circulaires (groupes)
		
		default:
			return null;
		}
	}
	
	protected String getStrPremiersIds(List<Integer> ids) {
		List<Integer> idsAffichage = ids.subList(0, Math.min(4, ids.size()));
		
		return StringUtils.join(idsAffichage, ", ");
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
					String strAutres = cals.size() > 5 ? "..." : "";
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains calendriers (ID " + getStrPremiersIds(cals) + strAutres + ") n'ont pas de groupe unique", this);
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
						
						bdd.executeRequest("INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id) VALUES(" + idGroupe + "," + idCal + ")");
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
					String strAutres = idGroupesUniques.size() > 5 ? "..." : "";
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains groupes uniques (ID " + getStrPremiersIds(idGroupesUniques) + strAutres + ") n'ont pas de calendrier", this);
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
					String strAutres = idEvenements.size() > 5 ? "..." : "";
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains événements (ID " + getStrPremiersIds(idEvenements) + strAutres + ") n'ont pas de calendrier", this);
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
}
