package org.ecn.edtemps.managers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.models.Salle;
import org.ecn.edtemps.models.identifie.SalleIdentifie;
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
		this.salleGestionnaire.getSalle(7);
	}

	@Test
	public void testModifierSalle() throws EdtempsException {
		SalleIdentifie salle = this.salleGestionnaire.getSalle(6);
		salle.setNom("test");
		this.salleGestionnaire.modifierSalle(salle);
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
		salle.setNom("nom");
		this.salleGestionnaire.sauverSalle(salle);

		// Cas nominal sans matériel
		salle.setBatiment("batiment");
		salle.setCapacite(30);
		salle.setNiveau(1);
		salle.setNumero(10);
		this.salleGestionnaire.sauverSalle(salle);

		// Cas nominal avec matériel
		// TODO: Audrey a pété le test, et ne sait pas vraiment comment le récupérer ... le Matériel est désormais une classe
		/*Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(1, 2);
		map.put(2, 1);
		salle.setMateriels(map);
		this.salleGestionnaire.sauverSalle(salle);*/

	}

	@Test
	public void testSupprimerSalle() throws EdtempsException {
		this.salleGestionnaire.supprimerSalle(7);
	}

}
