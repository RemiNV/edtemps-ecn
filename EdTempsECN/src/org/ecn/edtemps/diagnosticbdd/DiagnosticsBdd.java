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

public class DiagnosticsBdd {
	
	Logger logger = LogManager.getLogger(DiagnosticsBdd.class.getName());

	protected BddGestion bdd;
	
	public DiagnosticsBdd(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	public ArrayList<TestBddResult> runAllTests() {
		int nbTests = 1; // Nombre de tests gérés dans createTest
		
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
		
		
		default:
			return null;
		}
	}
	
	protected TestBdd createTestCalendrierPossedeGroupeUnique(int id) {
		return new TestBdd("Rattachement de tous les calendriers à un groupe de participants \"groupe unique\"", id, "Ajouter les groupes manquants") {
			
			protected ArrayList<Integer> getCalendriersSansGroupeUnique(BddGestion bdd) throws DatabaseException {
				ResultSet reponse = bdd.executeRequest("SELECT calendrier.cal_id, COUNT(groupeunique.groupeparticipant_id) AS nb_unique FROM edt.calendrier " +
					"LEFT JOIN edt.calendrierappartientgroupe cag ON calendrier.cal_id=cag.cal_id " +
					"LEFT JOIN edt.groupeparticipant groupeunique ON cag.groupeparticipant_id=groupeunique.groupeparticipant_id " +
					"AND groupeunique.groupeparticipant_estcalendrierunique " +
					"GROUP BY calendrier.cal_id " +
					"HAVING COUNT(groupeunique.groupeparticipant_id)=0");
				
				try {
					ArrayList<Integer> res = new ArrayList<Integer>();
					while(reponse.next()) {
						res.add(reponse.getInt("cal_id"));
					}
					reponse.close();
					return res;
				} catch (SQLException e) {
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
					List<Integer> idsAffichage = cals.subList(0, Math.min(4, cals.size()));
					
					String strCalendriers = StringUtils.join(idsAffichage, ", ");
					String strAutres = cals.size() > 5 ? "..." : "";
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains calendriers (ID " + strCalendriers + strAutres + ") n'ont pas de groupe unique", this);
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
}
