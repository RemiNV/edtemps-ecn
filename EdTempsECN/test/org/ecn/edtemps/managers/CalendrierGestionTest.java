package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.models.Calendrier;
import org.ecn.edtemps.models.identifie.CalendrierIdentifie;
import org.junit.Before;
import org.junit.Test;

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
		List<Integer> proprietaires = calRecup.getIdProprietaires();
		
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
	}
	

	@Test
	public void testAjoutSuppressionCalendrier() throws Exception {
		// Création d'un calendrier
		HashMap<Integer, String> matieres = calendrierGestion.listerMatieres();
		assertTrue(matieres.size() > 0); // Il doit y avoi des matières pré-rentrées dans la base de données
		
		HashMap<Integer, String> typesCalendrier = calendrierGestion.listerTypesCalendrier();
		assertTrue(typesCalendrier.size() > 0); // Il doit y avoir des types de calendrier pré-rentrés dans la BDD
		
		// Récupération d'un utilisateur de test (correspond au jeu de tests de la BDD)
		int idUser1 = bdd.recupererId("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='2'", "utilisateur_id");
		assertTrue(idUser1 > 0);
		
		int idUser2 = bdd.recupererId("SELECT utilisateur_id FROM edt.utilisateur WHERE utilisateur_token='3'", "utilisateur_id");
		assertTrue(idUser2 > 0);
		
		ArrayList<Integer> lstProprietaires = new ArrayList<Integer>(2);
		lstProprietaires.add(idUser1);
		lstProprietaires.add(idUser2);
		
		Calendrier calendrier = new Calendrier("caltest123'soleil", typesCalendrier.values().iterator().next(), matieres.values().iterator().next(), lstProprietaires);
		
		// Ajout à la bdd
		int idCal = calendrierGestion.sauverCalendrier(calendrier);
		
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

}

