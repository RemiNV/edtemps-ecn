package org.ecn.edtemps.managers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.models.Materiel;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
import org.ecn.edtemps.models.identifie.SalleRecherche;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de SalleGestion
 * 
 * @author Joffrey
 */
public class SalleGestionTest {

	BddGestion bddGestion;
	SalleGestion salleGestionnaire;

	@Before
	public void initAllTests() throws DatabaseException {
		this.bddGestion = new BddGestion();
		this.salleGestionnaire = new SalleGestion(this.bddGestion);
	}

	@Test
	public void testGetSalle() throws EdtempsException {
		SalleIdentifie salleIdentifie = this.salleGestionnaire.getSalle(1, true);
		assertTrue(salleIdentifie.getId()==1);
	}

	@Test
	public void testModifierSalle() throws EdtempsException {
		SalleIdentifie salle = this.salleGestionnaire.getSalle(6, true);
		
		Materiel materiel1 = new Materiel(1, "Ordinateur", 20);
		Materiel materiel2 = new Materiel(2, "Vidéoprojecteur", 0);
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		materiels.add(materiel1);
		materiels.add(materiel2);
		
		//Modification de tous les paramètres de la salle
		salle.setNom("testModification");
		salle.setBatiment("bat X");
		salle.setCapacite(30);
		salle.setNiveau(0);
		salle.setNumero(1);
		salle.setMateriels(materiels);
		
		this.salleGestionnaire.modifierSalle(salle);
		SalleIdentifie salleModifie = this.salleGestionnaire.getSalle(6, true);
		
		assertTrue(salleModifie.getNom().equals("testModification"));
		assertTrue(salleModifie.getBatiment().equals("bat X"));
		assertTrue(salleModifie.getCapacite()==30);
		assertTrue(salleModifie.getNiveau()==0);
		assertTrue(salleModifie.getNumero()==1);
		assertTrue(salleModifie.getMateriels().get(0).getQuantite()==20);
		assertTrue(salleModifie.getMateriels().get(1).getQuantite()==0);
	}

	@Test
	public void testSauverSalle() throws EdtempsException {

		// Cas null
		Salle salle = null;
		try {
			this.salleGestionnaire.sauverSalle(salle);
			fail("Une exception doit être levée");
		} catch (EdtempsException e) {
			assertTrue(StringUtils
					.contains(e.getMessage(),
							"Tentative d'enregistrer un objet NULL en base de données."));
		}

		// Cas vide
		salle = new Salle("");
		try {
			this.salleGestionnaire.sauverSalle(salle);
			fail("Une exception doit être levée");
		} catch (EdtempsException e) {
			assertTrue(StringUtils
					.contains(e.getMessage(),
							"Tentative d'enregistrer une salle en base de données sans nom."));
		}

		// Cas minimal sans matériel
		salle.setNom("testSauvegardeSalleNom");
		int idSalle = this.salleGestionnaire.sauverSalle(salle);
		SalleIdentifie salleSauvegardeNom = this.salleGestionnaire.getSalle(idSalle, true);
		assertTrue(salleSauvegardeNom.getNom().equals("testSauvegardeSalleNom"));

		// Cas nominal sans matériel
		salle.setNom("test de sauvegarde de salle avec description");
		salle.setBatiment("batiment X");
		salle.setCapacite(30);
		salle.setNiveau(1);
		salle.setNumero(10);
		
		idSalle = this.salleGestionnaire.sauverSalle(salle);
		SalleIdentifie salleSauvegardeDescription = this.salleGestionnaire.getSalle(idSalle, true);
		
		assertTrue(salleSauvegardeDescription.getNom().equals("test de sauvegarde de salle avec description"));
		assertTrue(salleSauvegardeDescription.getBatiment().equals("batiment X"));
		assertTrue(salleSauvegardeDescription.getCapacite()==30);
		assertTrue(salleSauvegardeDescription.getNiveau()==1);
		assertTrue(salleSauvegardeDescription.getNumero()==10);
		
		// Cas nominal avec matériel
		Materiel materiel1 = new Materiel(1, "Ordinateur", 10);
		Materiel materiel2 = new Materiel(2, "Vidéoprojecteur", 1); // les noms et id doivent correspondre à ceux de la base de données
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		materiels.add(materiel1);
		materiels.add(materiel2);
		
		salle.setNom("test de sauvegarde de salle avec matériel");
		salle.setMateriels(materiels);
		
		idSalle = this.salleGestionnaire.sauverSalle(salle);
		SalleIdentifie salleSauvegardeMateriel = this.salleGestionnaire.getSalle(idSalle, true);
		
		assertTrue(salleSauvegardeMateriel.getNom().equals("test de sauvegarde de salle avec matériel"));
		assertTrue(salleSauvegardeMateriel.getMateriels().get(0).getNom().equals("Ordinateur"));
		assertTrue(salleSauvegardeMateriel.getMateriels().get(0).getQuantite()==10);
		assertTrue(salleSauvegardeMateriel.getMateriels().get(1).getNom().equals("Vidéoprojecteur"));
		assertTrue(salleSauvegardeMateriel.getMateriels().get(1).getQuantite()==1);
	}

	@Test
	// Ne marche que sur la base de données originale (sinon la salle avec cette id a déjà été supprimée en faisant jouer le test une fois)
	public void testSupprimerSalle() throws EdtempsException {
		this.salleGestionnaire.supprimerSalle(7);
		SalleIdentifie salleSupprimee = salleGestionnaire.getSalle(7, true);
		assertTrue(salleSupprimee.equals(null));
	}
	
	@Test
	public void testRechercherSalle() throws EdtempsException, ParseException {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date a = dfm.parse("2013-10-21 10:15:00");
		Date b = dfm.parse("2013-10-21 12:15:00");
		
		/* Cas avec pas de matériel requis juste entre deux dates avec une capacité de 10 personnes,
		la salle d'id 5 est déjà occupée pour un événement entre cesdeux dates,
		la salle d'id 4 est occupée le même jour mais pas dans ce créneau horaire*/
		ArrayList<Materiel> materiels = new ArrayList<Materiel>();
		ArrayList<SalleRecherche> sallesRecherchees = salleGestionnaire.rechercherSalle(a, b, materiels, 10, false, true);
		boolean salle5 = false;
		boolean salle4 = false;
		boolean salle1  = false;
		for (int i=0; i<sallesRecherchees.size(); i++){
			if (sallesRecherchees.get(i).getId()==5){
				salle5= true;
			} else if (sallesRecherchees.get(i).getId()==4){
				salle4 =  true;
			} else if (sallesRecherchees.get(i).getId()==1){
				salle1 =  true;
			}
		}
		assertFalse(salle5); // salle 5 occupée
		assertTrue(salle4); // salle 4 libre
		assertTrue(salle1); // salle 1 libre
		
		/*Cas avec matériel recherché
		la salle 4 libre dans le cas précédent ne doit plus être proposée car elle ne contient que 2 ordinateurs alors que 10 sont demandés */
		Materiel materiel1 = new Materiel(1, "Ordinateur", 9);
		materiels.add(materiel1);
		sallesRecherchees = salleGestionnaire.rechercherSalle(a, b, materiels, 10, false, true);
		salle5 = false;
		salle4 = false;
		salle1  = false;
		for (int i=0; i<sallesRecherchees.size(); i++){
			if (sallesRecherchees.get(i).getId()==5){
				salle5= true;
			} else if (sallesRecherchees.get(i).getId()==4){
				salle4 =  true;
			} else if (sallesRecherchees.get(i).getId()==1){
				salle1 =  true;
			}
		}
		assertFalse(salle5); // salle 5 occupée
		assertFalse(salle4); // salle 4 n'a pas assez d'ordinateurs
		assertTrue(salle1); // salle 2 libre et a suffisament d'ordinateurs (10 en base de données)
		
		/*Cas sans acceptation des salles où il y a un événement qui n'est pas un cours, puis avec acceptation
		 * dans le premier cas la salle 5 n'apparait pas, dans le deuxième cas la salle 5 apparait*/
		a = dfm.parse("2013-10-23 16:00:00");
		b = dfm.parse("2013-10-23 19:00:00");
		sallesRecherchees = salleGestionnaire.rechercherSalle(a, b, new ArrayList<Materiel>(), 10, false, true);
		salle5 = false;
		for (int i=0; i<sallesRecherchees.size(); i++){
			if (sallesRecherchees.get(i).getId()==5){
				salle5= true;
			}
		}
		assertFalse(salle5); // salle 5 occupée
		sallesRecherchees = salleGestionnaire.rechercherSalle(a, b, new ArrayList<Materiel>(), 10, true, true);
		salle5 = false;
		for (int i=0; i<sallesRecherchees.size(); i++){
			if (sallesRecherchees.get(i).getId()==5){
				salle5= true;
			}
		}
		assertTrue(salle5); // salle 5 occupée mais pas par un cours
	}
	
	@Test
	public void testgetSallesEvenement() throws EdtempsException {
		ArrayList<SalleIdentifie> salleIdentifie = salleGestionnaire.getSallesEvenement(1);
		assertTrue(salleIdentifie.get(0).getId()==5);
	}

}
