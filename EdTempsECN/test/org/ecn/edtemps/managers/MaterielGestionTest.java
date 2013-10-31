package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.junit.Before;
import org.junit.Test;

public class MaterielGestionTest {

	BddGestion bddGestion;
	MaterielGestion materielGestionnaire;

	@Before
	public void initAllTests() throws DatabaseException {
		this.bddGestion = new BddGestion();
		this.materielGestionnaire = new MaterielGestion(this.bddGestion);
	}

	@Test
	public void testGetSalle() throws EdtempsException {
		assertEquals(3, this.materielGestionnaire.getListeMateriel().size());
	}

}
