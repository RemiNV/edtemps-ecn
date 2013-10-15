package org.ecn.edtemps.managers;

import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.models.Salle;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de SalleGestion
 * 
 * @author Joffrey
 */
public class SalleGestionTest {

	SalleGestion salleGestionnaire;

	@Before
	public void initAllTests() {
		salleGestionnaire = new SalleGestion();
	}

	@Test
	public void testSauverSalle() throws EdtempsException {

		Salle salle = null;
		this.salleGestionnaire.sauverSalle(salle);

	}

}
