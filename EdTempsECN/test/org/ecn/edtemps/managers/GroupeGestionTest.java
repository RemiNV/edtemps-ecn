package org.ecn.edtemps.managers;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de GroupeGestion
 * 
 * @author Joffrey
 */
public class GroupeGestionTest {

	BddGestion bddGestion;
	GroupeGestion groupeGestionnaire;

	@Before
	public void initAllTests() throws DatabaseException {
		this.bddGestion = new BddGestion();
		this.groupeGestionnaire = new GroupeGestion(this.bddGestion);
	}

	@Test
	public void testGetGroupe() throws EdtempsException {
		// TODO
	}

}
