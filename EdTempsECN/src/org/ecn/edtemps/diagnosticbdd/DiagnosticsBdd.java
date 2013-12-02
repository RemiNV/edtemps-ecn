package org.ecn.edtemps.diagnosticbdd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

public class DiagnosticsBdd {

	protected BddGestion bdd;
	
	public DiagnosticsBdd(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	/**
	 * Créé un test 
	 * @param idTest
	 * @return
	 */
	protected TestBdd createTest(int idTest) {
		switch(idTest) {
		
		
		
		default:
			return null;
		}
	}
	
	protected TestBdd createTestCalendrierPossedeGroupeUnique(int id) {
		return new TestBdd("Tous les calendriers sont rattachés à un groupe de participants \"groupe unique de calendrier\"", id) {
			
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
				
				if(cals.size() > 0) {
					return new TestBddResult(TestBddResultCode.OK, "Les calendriers sont tous associés à au moins un groupe unique");
				}
				else {
					List<Integer> idsAffichage = cals.subList(0, 4);
					
					String strCalendriers = StringUtils.join(idsAffichage, ", ");
					String strAutres = cals.size() > 5 ? "..." : "";
					
					return new TestBddResult(TestBddResultCode.ERROR, "Certains calendriers (IDs " + strCalendriers + strAutres + ") n'ont pas de groupe unique");
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
