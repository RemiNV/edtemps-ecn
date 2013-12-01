package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
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
		assertEquals(3, this.materielGestionnaire.getListeMateriel().size());
	}
	
	@Test
	public void testSauverMateriel() throws EdtempsException {
		int resultatException = 0;
		
		// test d'impossibilité de sauver un matériel sans nom
		try{
			this.materielGestionnaire.sauverMateriel("");
		}
		catch(EdtempsException e) {
			resultatException = e.getResultCode().getCode();
		}
		assertTrue(resultatException == 7); //Code 7 : Nom invalide
		
		// test d'impossibilité de sauver un matériel avec un nom non alphanumérique
				try{
					this.materielGestionnaire.sauverMateriel("Bonjour*+-");
				}
				catch(EdtempsException e) {
					resultatException = e.getResultCode().getCode();
				}
				assertTrue(resultatException == 11); //Code 11 : Nom non alphanumérique
		
		// test d'impossibilité de sauver un matériel déjà existant en base de données
		try{
			this.materielGestionnaire.sauverMateriel("Ordinateur");
		}
		catch(EdtempsException e) {
			resultatException = e.getResultCode().getCode();
		}
		assertTrue(resultatException == 8); //Code 8 : Nom déjà pris en base de données
		
		//test de sauvegarde d'un nouveau matériel
		this.materielGestionnaire.sauverMateriel("Tablette");
		assertEquals(4, this.materielGestionnaire.getListeMateriel().size());
	}
	
	@Test
	public void testSupprimerMateriel() throws EdtempsException {
		this.materielGestionnaire.supprimerMateriel(4);
		assertEquals(3, this.materielGestionnaire.getListeMateriel().size());
	}

}
