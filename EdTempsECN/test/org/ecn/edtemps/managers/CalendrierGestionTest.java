package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.managers.CalendrierGestion.DroitsCalendriers;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierComplet;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de CalendrierGestion
 * 
 * @author Remi
 */
public class CalendrierGestionTest {

	protected BddGestion bdd;
	protected CalendrierGestion calendrierGestion;
	
	public CalendrierGestionTest() throws DatabaseException {
		bdd = new BddGestion();
	}
	

	@Before
	public void setUp() throws Exception {
		calendrierGestion = new CalendrierGestion(bdd);
	}
	
	
	private void comparerCalendriers(CalendrierIdentifie calRecup, Calendrier cal) {
		
		// Comparaison des propriétaires
		List<Integer> proprietairesRecup = calRecup.getIdProprietaires();
		List<Integer> proprietaires = calRecup.getIdProprietaires(); //TODO: Erreur à corriger ? Ne faut il pas récupérer la liste de propriétaire du calendrier cal ?
		
		assertEquals(proprietairesRecup.size(), proprietaires.size());
		
		for(Integer propRecup : proprietairesRecup) {
			boolean exists = false;
			for(Integer prop : proprietaires) {
				if(prop == propRecup)
					exists = true;
			}
			
			assertTrue(exists);
		}
		
		
		assertEquals(calRecup.getMatiere(), cal.getMatiere());
		assertEquals(calRecup.getNom(), cal.getNom());
		assertEquals(calRecup.getType(), cal.getType());
		assertEquals(calRecup.getIdCreateur(), cal.getIdCreateur());
	}
	

	@Test
	public void testAjoutSuppressionCalendrier() throws Exception {
		// Création d'un calendrier
		HashMap<Integer, String> matieres = calendrierGestion.listerMatieres();
		assertTrue(matieres.size() > 0); // Il doit y avoir des matières pré-rentrées dans la base de données
		
		HashMap<Integer, String> typesCalendrier = calendrierGestion.listerTypesCalendrier();
		assertTrue(typesCalendrier.size() > 0); // Il doit y avoir des types de calendrier pré-rentrés dans la BDD
		
		// Récupération d'un utilisateur de test (correspond au jeu de tests de la BDD)
		PreparedStatement requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='2'");
		int idUser1 = bdd.recupererId(requetePreparee, "utilisateur_id");
		assertTrue(idUser1 > 0);
		
		requetePreparee = bdd.getConnection().prepareStatement(
				"SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='3'");
		int idUser2 = bdd.recupererId(requetePreparee, "utilisateur_id");
		assertTrue(idUser2 > 0);
		
		ArrayList<Integer> lstProprietaires = new ArrayList<Integer>(2);
		lstProprietaires.add(idUser1);
		lstProprietaires.add(idUser2);
		
		Calendrier calendrier = new Calendrier("caltest123soleil", typesCalendrier.values().iterator().next(), matieres.values().iterator().next(), lstProprietaires, idUser1);
		
		// Ajout à la bdd
		int idCal = calendrierGestion.sauverCalendrier(calendrier, new ArrayList<Integer>(0));
		
		// Récupération du calendrier ajouté
		CalendrierIdentifie calAjoute = calendrierGestion.getCalendrier(idCal);
		
		comparerCalendriers(calAjoute, calendrier);
		
		// Suppression du calendrier et test d'absence
		calendrierGestion.supprimerCalendrier(idCal);
		
		boolean thrown = false;
		try {
			calendrierGestion.getCalendrier(idCal);
		}
		catch(EdtempsException e) {
			thrown = true;
		}
		
		assertTrue(thrown);
	}
	
	@Test
	public void testlisterCalendriersAbonnementsUtilisateur() throws Exception {
		// utilisateur 2 abonné à 2 groupes de participants (1 groupe contenant 2 calendriers et un autre contenant 1 calendrier)
		ArrayList<CalendrierIdentifie> calendriersIdentfies = this.calendrierGestion.listerCalendriersAbonnementsUtilisateur(2, true, false);
		assertEquals(3, calendriersIdentfies.size());
	}
	
	@Test
	public void testlisterCalendriersUtilisateur() throws Exception {
		// utilisateur 1 est propriétaire de 2 calendrier
		ArrayList<CalendrierComplet> calendriers = this.calendrierGestion.listerCalendriersUtilisateur(1);
		assertEquals(2, calendriers.size());
	}
	
	@Test
	public void testlisterCalendriersGroupeParticipants() throws Exception {
		// groupe inscrit à 2 calendriers
		List<CalendrierIdentifie> calendriersIdentifies = this.calendrierGestion.listerCalendriersGroupeParticipants(8);
		assertEquals(2, calendriersIdentifies.size());
		assertEquals(1, calendriersIdentifies.get(0).getId());
		assertEquals(2, calendriersIdentifies.get(1).getId());
		
		// groupe insrit à 0 calendrier
		calendriersIdentifies = this.calendrierGestion.listerCalendriersGroupeParticipants(1);
		assertEquals(0, calendriersIdentifies.size());
	}
	
	@Test
	public void testgetDroitsCalendriers() throws Exception {
		
		List<Integer> calendriersIds = new ArrayList<Integer>();
		//calendriers dont l'utilisateur est propriétaire et qui sont des cours
		calendriersIds.add(1);
		calendriersIds.add(2);
		DroitsCalendriers droits = this.calendrierGestion.getDroitsCalendriers(1, calendriersIds);
		assertTrue(droits.estProprietaire);
		assertTrue(droits.contientCours);
		
		//calendriers dont l'utilisateur n'est pas propriétaire et qui sont des cours
		droits = this.calendrierGestion.getDroitsCalendriers(2, calendriersIds);
		assertFalse(droits.estProprietaire);
		assertTrue(droits.contientCours);
		
		//calendrier dont l'utilisateur n'est pas propriétaire et qui n'est pas un cours
		calendriersIds.clear();
		calendriersIds.add(7);
		droits = this.calendrierGestion.getDroitsCalendriers(1, calendriersIds);
		assertFalse(droits.estProprietaire);
		assertFalse(droits.contientCours);
		
		//calendrier dont l'utilisateur est pas propriétaire et qui n'est pas un cours
		droits = this.calendrierGestion.getDroitsCalendriers(2, calendriersIds);
		assertFalse(droits.estProprietaire);
		assertFalse(droits.contientCours);
	}
	
	@Test
	public void testlisterTypesCalendrier() throws Exception {
		assertEquals(3, this.calendrierGestion.listerTypesCalendrier().size());
	}
	
	@Test
	public void testlisterMatieres() throws Exception {
		assertEquals(5, this.calendrierGestion.listerMatieres().size());
	}

}

