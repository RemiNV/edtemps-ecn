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
import org.ecn.edtemps.models.identifie.EvenementComplet;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
import org.ecn.edtemps.models.inflaters.AbsEvenementInflater;
import org.ecn.edtemps.models.inflaters.EvenementCompletInflater;
import org.ecn.edtemps.models.inflaters.EvenementIdentifieInflater;
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
		evenementGestion.sauverEvenement("EvenementTestJUnit", dateDebut, dateFin, listeIdCal, 1, listeIdSalles, listeIdIntervenants, listeIdResponsables, true);
		
		//Test de l'impossibilité d'enregistrer un évènement au même moment dans une même salle
		int resultatException = 0;
		try{
			evenementGestion.sauverEvenement("EvenementTestJUnit2", dateDebut, dateFin, listeIdCal, 1, listeIdSalles, listeIdIntervenants, listeIdResponsables, true);
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

		assertTrue(evenementGestion.getEvenement(idEvenementEnregistre)==null);
		
		
	}
	
	/**
	 * Methode pour comparer un evenement complet recuperé avec les données connues.
	 * @param evenement : evenement récupéré
	 * @param nom : nom de l'événement
	 * @param dateDebut : date et heure de début de l'événement
	 * @param dateFin : date et heure de fin de l'événement
	 * @param nomsIntervenants : noms des intervenants de l'événement
	 * @param nomsSalles : noms des salles dans lesquelles l'événement a lieu
	 * @param nomMatiere : noms des matières de l'événement
	 * @param nomType : noms des types de l'événement
	 */
	public void comparerEvenementComplet(EvenementComplet evenement, String nom, Date dateDebut, Date dateFin, List<String> nomsIntervenants, List<String> nomsSalles, List<String> nomsMatieres, List<String> nomsTypes){
		
		assertEquals(evenement.getNom(), nom);
		assertEquals(evenement.getDateDebut().compareTo(dateDebut),0);
		assertEquals(evenement.getDateFin().compareTo(dateFin),0);
		
		//Comparaison de la liste des intervenants
		assertEquals(evenement.getIntervenants().size(), nomsIntervenants.size());
		for (UtilisateurIdentifie utilisateur : evenement.getIntervenants()) {
			boolean exists = false;
			for (String nomProf : nomsIntervenants) {
				if (utilisateur.getNom().equals(nomProf)) {
					exists = true;
				}
			}
		assertTrue(exists);
		}
	
		//Comparaison de la liste des salles
		assertEquals(evenement.getSalles().size(), nomsSalles.size());
		for (SalleIdentifie salle : evenement.getSalles()) {
			boolean exists = false;
			for (String nomSalle : nomsSalles) {
				if (salle.getNom().equals(nomSalle)) {
					exists = true;
				}
			}
		assertTrue(exists);
		}
	
		//Comparaison de la liste des matieres
		assertEquals(evenement.getMatieres().size(), nomsMatieres.size());
		for (String matiere : evenement.getMatieres()) {
			assertTrue(nomsMatieres.contains(matiere));
		}
		
		//Comparaison de la liste des types
		assertEquals(evenement.getTypes().size(), nomsTypes.size());
		for (String type : evenement.getTypes()) {
			assertTrue(nomsTypes.contains(type));
		}
	
	}
	
	/**
	 * Test de la méthode listerEvenementGroupe
	 * Le test se base sur des données de test présente dans la base de données de test.
	 */
	@Test
	public void testListerEvenementsGroupe() throws Exception {
	
		//Utilisation de données spécifiques insérées dans la base de données : 6 événements testCalendrierGestion1 à testCalendrierGestion6
		//4 de ces événements sont rattachés à un groupe de participant, 1 événement à un groupe père et le dernier à un groupe fils.
				
		//récupération de l'id du groupe
		PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(
						"SELECT groupeParticipant_id FROM edt.GroupeParticipant WHERE groupeparticipant_nom = 'testEvenementGestion'");
		int idGroupe = bdd.recupererId(requetePreparee, "groupeparticipant_id");
		
		//Les donnees de test contiennent des evenement créés entre le 01/10/2013 et le 04/10/2013
		Date dateDebut = new Date(113, 8, 01, 06, 00, 00);
		Date dateFin = new Date(113, 10, 05, 18, 00, 00);

		
		//Récupération des évenements correspondant au groupe de test : 6 évenements récupérés
		ArrayList<EvenementComplet> listeEvenements = evenementGestion.listerEvenementsGroupe(idGroupe, dateDebut, dateFin, true);
		assertTrue(listeEvenements.size() == 6);
		
		//On initialise les listes qui vont servir à comparer. 
		//Tous les événements de tests sont dans la même salle avec le même intervenant
		ArrayList<String> nomsIntervenants = new ArrayList<String>();
		nomsIntervenants.add("Doe");
		ArrayList<String> nomsSalles = new ArrayList<String>();
		nomsSalles.add("Salle D03");
		ArrayList<String> nomsMatieres = new ArrayList<String>();
		nomsMatieres.add("THERE");
		ArrayList<String> nomsTypes = new ArrayList<String>();
		nomsTypes.add("CM");
		
		for (EvenementComplet evenement : listeEvenements) {
			switch (evenement.getNom()) {
			
			case "testEvenementGestion1":
				this.comparerEvenementComplet(evenement, "testEvenementGestion1", new Date(113, 9, 01, 8, 00, 00), new Date(113, 9, 01, 10, 00, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
				
			case "testEvenementGestion2":
				this.comparerEvenementComplet(evenement, "testEvenementGestion2", new Date(113, 9, 01, 10, 15, 00), new Date(113, 9, 01, 12, 15, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
				
			case "testEvenementGestion3":
				this.comparerEvenementComplet(evenement, "testEvenementGestion3", new Date(113, 9, 01, 13, 45, 00), new Date(113, 9, 01, 15, 45, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
				
			case "testEvenementGestion4":
				this.comparerEvenementComplet(evenement, "testEvenementGestion4", new Date(113, 9, 01, 16, 00, 00), new Date(113, 9, 01, 18, 00, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
			
			case "testEvenementGestion5":
				this.comparerEvenementComplet(evenement, "testEvenementGestion5", new Date(113, 9, 04, 8, 00, 00), new Date(113, 9, 04, 10, 00, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
			
			case "testEvenementGestion6":
				//Evenement ayant lieu dans deux salles
				nomsSalles.add("Salle info B12");
				this.comparerEvenementComplet(evenement, "testEvenementGestion6", new Date(113, 9, 04, 10, 15, 00), new Date(113, 9, 04, 12, 15, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				nomsSalles.remove("Salle info B12");
				break;
			
				
			default:
				fail();
				break;
			}
		}
		
	}
	
	
	/**
	 * Test de la méthode listerEvenementCompletsSalle de la classe EvenementGestion
	 * 
	 * Le test se base sur les données de test de la base de données
	 */
	@Test
	public void testListerEvenementSalle() throws Exception{
		
		//Test de la méthode sur la salle C02 : 2 événements présents dans la base ayant lieu dans cette salle
		//Récupération de l'id de la salle
		PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT salle_id FROM edt.Salle WHERE salle_nom = 'Salle C02'");
		int idSalle = bdd.recupererId(requetePreparee, "salle_id");
		
		
		//On initialise les listes qui vont servir à comparer. 
		ArrayList<String> nomsIntervenants = new ArrayList<String>();
		nomsIntervenants.add("ProfSportThere");
		ArrayList<String> nomsSalles = new ArrayList<String>();
		nomsSalles.add("Salle C02");
		ArrayList<String> nomsMatieres = new ArrayList<String>();
		nomsMatieres.add("dSIBAD");
		ArrayList<String> nomsTypes = new ArrayList<String>();
		nomsTypes.add("TD");
		
		ArrayList<EvenementComplet> listeEvenement = new ArrayList<EvenementComplet>();
		listeEvenement = evenementGestion.listerEvenementCompletsSalle(idSalle, new Date(2013,9,21, 8,00,00),new Date(2013,9,22,18,00,00), true);
		
		//On s'attend à avoir récupéré 2 événements
		assertTrue(listeEvenement.size()==2);
		for (EvenementComplet evenement : listeEvenement) {
			switch (evenement.getDateDebut().getDay()) {
			
			case 21:
				this.comparerEvenementComplet(evenement, "dSIBAD", new Date(113, 9, 21, 8, 00, 00), new Date(113, 9, 21, 10, 00, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
				
			case 22:
				this.comparerEvenementComplet(evenement, "dSIBAD", new Date(113, 9, 22, 10, 15, 00), new Date(113, 9, 22, 12, 15, 00), nomsIntervenants, nomsSalles, nomsMatieres, nomsTypes);
				break;
				
			default:
				fail();
				break;
			}
		}
	}

}
