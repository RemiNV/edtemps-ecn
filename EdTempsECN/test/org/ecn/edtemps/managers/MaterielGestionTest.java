package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.exceptions.ResultCode;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de MaterielGestion
 * 
 * @author Audrey
 */
public class MaterielGestionTest {

	BddGestion bddGestion;
	MaterielGestion materielGestionnaire;

	@Before
	public void initAllTests() throws DatabaseException {
		this.bddGestion = new BddGestion();
		this.materielGestionnaire = new MaterielGestion(this.bddGestion);
	}

	@Test
	public void testgetListeMateriel() throws EdtempsException {
		assertEquals(this.materielGestionnaire.getListeMateriel().size(), 3);
	}
	
	@Test
	public void testSauverMateriel() throws EdtempsException {
		ResultCode resultatException = null;
		
		// test d'impossibilité de sauver un matériel sans nom
		try{
			this.materielGestionnaire.sauverMateriel("");
		}
		catch(EdtempsException e) {
			resultatException = e.getResultCode();
		}
		assertTrue(resultatException == ResultCode.INVALID_OBJECT); //Code 7 : Nom invalide
		resultatException = null;
		
		// test d'impossibilité de sauver un matériel avec un nom non alphanumérique
		try{
			this.materielGestionnaire.sauverMateriel("Bonjour*+-");
		}
		catch(EdtempsException e) {
			resultatException = e.getResultCode();
		}
		assertTrue(resultatException == ResultCode.ALPHANUMERIC_REQUIRED); //Code 11 : Nom non alphanumérique
		resultatException = null;
		
		// test d'impossibilité de sauver un matériel déjà existant en base de données
		try{
			this.materielGestionnaire.sauverMateriel("Ordinateur");
		}
		catch(EdtempsException e) {
			resultatException = e.getResultCode();
		}
		assertTrue(resultatException == ResultCode.NAME_TAKEN); //Code 8 : Nom déjà pris en base de données
		resultatException = null;
		
		//test de sauvegarde d'un nouveau matériel
		int idMateriel = this.materielGestionnaire.sauverMateriel("Tablette");
		assertEquals(4, this.materielGestionnaire.getListeMateriel().size());
		
		// Test de suppression du matériel
		this.materielGestionnaire.supprimerMateriel(idMateriel);
		assertEquals(3, this.materielGestionnaire.getListeMateriel().size());
	}

}
