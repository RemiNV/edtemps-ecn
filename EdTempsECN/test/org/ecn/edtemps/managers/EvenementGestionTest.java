package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Evenement;
import org.ecn.edtemps.models.identifie.*;
import org.junit.Before;
import org.junit.Test;



/**
 * Classe de test de EvenementGestion
 * 
 * @author Felix
 *
 */
public class EvenementGestionTest {
	
	protected BddGestion bdd;
	protected EvenementGestion evenementGestion;

	public EvenementGestionTest() throws DatabaseException {
		bdd = new BddGestion();
	}
	

	@Before
	public void setUp() throws Exception {
		evenementGestion = new EvenementGestion(bdd);
	}
	
	/**
	 * Methode pour comparer l'evenement récupéré de la base de donnée et les informations ayant servi à créer cet évenement
	 * 
	 * @param evenementRecup : évènement récupéré de la base de donnée
	 * @param nom : nom de l'évenement créé
	 * @param dateDebut : date du début de l'évenement créé
	 * @param dateFin : date de fin de l'évenement créé
	 * @param idCalendriers : liste des ids des calendriers auxquels est rattaché l'évenement créé
	 * @param idSalles : liste des ids des salles dans lesquelles a lieu l'évenement créé
	 * @param idIntervenants : liste des ids des intervenants de l'évenement créé
	 * @param idResponsables : liste des ids des responsables de l'évenement créé
	 */
	private void comparerEvenements(EvenementIdentifie evenementRecup, String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<Integer> idSalles, 
			List<Integer> idIntervenants, List<Integer> idResponsables) {
		
		assertEquals(evenementRecup.getNom(), nom);
		assertEquals(evenementRecup.getDateDebut().compareTo(dateDebut),0);
		assertEquals(evenementRecup.getDateFin().compareTo(dateFin),0);
		
		//Comparaison des calendriers de rattachement
		List<Integer> idCalRecup = evenementRecup.getIdCalendriers();
		
		assertEquals(idCalRecup.size(), idCalendriers.size());
		
		for(Integer idRecup : idCalRecup) {
			boolean exists = false;
			for(Integer id : idCalendriers) {
				if(id == idRecup)
					exists = true;
			}
			
			assertTrue(exists);
		}
		
		//Comparaison des salles rattachées à l'évenement
			List<SalleIdentifie> sallesCalRecup = evenementRecup.getSalles();
				
				assertEquals(sallesCalRecup.size(), idSalles.size());
				
				for(SalleIdentifie salleRecup : sallesCalRecup) {
					boolean exists = false;
					for(Integer idSalle : idSalles) {
						if(idSalle == salleRecup.getId())
							exists = true;
					}
					
					assertTrue(exists);
				}
	
				//Comparaison des intervenants rattachés à l'évenement
				List<UtilisateurIdentifie> intervenantsCalRecup = evenementRecup.getIntervenants();
					
					assertEquals(intervenantsCalRecup.size(), idIntervenants.size());
					
					for(UtilisateurIdentifie intervenantRecup : intervenantsCalRecup) {
						boolean exists = false;
						for(Integer idIntervenant : idIntervenants) {
							if(idIntervenant == intervenantRecup.getId())
								exists = true;
						}
						
						assertTrue(exists);
					}
		
					//Comparaison des responsables rattachés à l'évenement
					List<UtilisateurIdentifie> responsablesCalRecup = evenementRecup.getResponsables();
						
						assertEquals(responsablesCalRecup.size(), idResponsables.size());
						
						for(UtilisateurIdentifie responsableRecup : responsablesCalRecup) {
							boolean exists = false;
							for(Integer idResponsable : idResponsables) {
								if(idResponsable == responsableRecup.getId())
									exists = true;
							}
							
							assertTrue(exists);
						}
	}
	

	@Test
	public void testAjoutSuppressionEvenement() throws Exception {
		
		//Création d'un évènement
		
		//Creation des dates de début et fin (25/11/2013 16h et 25/11/2013 18h)
		Date dateDebut = new Date(113, 10, 25, 16, 00, 00);
		Date dateFin = new Date(113, 10, 25, 18, 00, 00);
		
		//Récupération d'un id de calendrier
		PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT min(cal_id) AS idcal FROM edt.calendrier");
		int idCal = bdd.recupererId(requetePreparee, "idcal");
		assertTrue(idCal > 0);
		
		//Récupération d'un id de salle
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT min(salle_id) AS idSalle FROM edt.salle");
		int idSalle = bdd.recupererId(requetePreparee, "idSalle");
		assertTrue(idSalle > 0);
		
		// Récupération de 2 utilisateurs de test (correspond au jeu de tests de la BDD)
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='2'");
		int idUser1 = bdd.recupererId(requetePreparee, "utilisateur_id");
		assertTrue(idUser1 > 0);

		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='3'");
		int idUser2 = bdd.recupererId(requetePreparee, "utilisateur_id");
		assertTrue(idUser2 > 0);
		

		//Création des listes
		ArrayList<Integer> listeIdCal = new ArrayList<Integer>();
		listeIdCal.add(idCal);
		
		ArrayList<Integer> listeIdSalles = new ArrayList<Integer>();
		listeIdSalles.add(idSalle);
		
		ArrayList<Integer> listeIdResponsables = new ArrayList<Integer>();
		listeIdResponsables.add(idUser1);
		listeIdResponsables.add(idUser2);
		
		ArrayList<Integer> listeIdIntervenants = new ArrayList<Integer>();
		listeIdIntervenants.add(idUser1);
		
		
		//Enregistrement de l'évenement
		evenementGestion.sauverEvenement("EvenementTestJUnit", dateDebut, dateFin, listeIdCal, null, listeIdSalles, listeIdIntervenants, listeIdResponsables, true);
		
		//Test de l'impossibilité d'enregistrer un évènement au même moment dans une même salle
		int resultatException = 0;
		try{
			evenementGestion.sauverEvenement("EvenementTestJUnit2", dateDebut, dateFin, listeIdCal, null, listeIdSalles, listeIdIntervenants, listeIdResponsables, true);
		}
		catch(EdtempsException e) {
			 
			resultatException = e.getResultCode().getCode();
		}
		assertTrue(resultatException == 10); //Code 10 : Salle Occupée
		
		//Récupération de l'id de l'évenement créé
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT eve_id FROM edt.evenement WHERE eve_nom = 'EvenementTestJUnit'");
		int idEvenementEnregistre = bdd.recupererId(requetePreparee, "eve_id");
		
		assertTrue(idEvenementEnregistre != -1);
		
		//Récupération de l'évenement enregistré
		EvenementIdentifie evenementEnregistre = evenementGestion.getEvenement(idEvenementEnregistre);
			
		//Comparaison
		this.comparerEvenements(evenementEnregistre, "EvenementTestJUnit", dateDebut, dateFin, listeIdCal, listeIdSalles, listeIdIntervenants, listeIdResponsables);
		
		
		
		
		
		//Test modification de l'évènement
		
		//Creation des dates de début et fin (26/11/2013 16h et 26/11/2013 18h)
		dateDebut = new Date(113, 10, 26, 16, 00, 00);
		dateFin = new Date(113, 10, 26, 18, 00, 00);

		//Récupération d'un id de calendrier
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT cal_id FROM edt.calendrier");
		ArrayList<Integer> idsCalendrier = bdd.recupererIds(requetePreparee, "cal_id");
		assertTrue(idsCalendrier.size() > 1);
		idCal = idsCalendrier.get(1);

		//Récupération d'un id de salle
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT salle_id FROM edt.salle");
		ArrayList<Integer> idsSalle = bdd.recupererIds(requetePreparee, "salle_id");
		assertTrue(idsSalle.size() > 1);
		idSalle = idsSalle.get(1);
		
		//Modoification des listes
		listeIdCal.set(0, idCal);
		listeIdSalles.set(0, idSalle);
		listeIdResponsables.set(0, idUser1);
		listeIdResponsables.remove(1);
		listeIdIntervenants.set(0, idUser2);
		
		//Modification de l'évènement
		evenementGestion.modifierEvenement(idEvenementEnregistre, "EvenementTestJUnitModifie", dateDebut, dateFin, listeIdCal, listeIdSalles, listeIdIntervenants, listeIdResponsables, true);
		
		//Récupération de l'évenement enregistré
		evenementEnregistre = evenementGestion.getEvenement(idEvenementEnregistre);

		//Comparaison
		this.comparerEvenements(evenementEnregistre, "EvenementTestJUnitModifie", dateDebut, dateFin, listeIdCal, listeIdSalles, listeIdIntervenants, listeIdResponsables);


				
		
		
		//Suppression de l'evenement enregistré
		evenementGestion.supprimerEvenement(idEvenementEnregistre, true);

		boolean thrown = false;
		try {
			evenementGestion.getEvenement(idEvenementEnregistre);
		}
		catch(EdtempsException e) {
			thrown = true;
		}
		
		assertTrue(thrown);
		
		
	}
	

	
	

}
