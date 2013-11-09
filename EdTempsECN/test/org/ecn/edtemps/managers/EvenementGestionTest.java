package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Evenement;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;
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
	 * Methode pour comparer l'evenement récupéré de la base de donnée et l'évènement modèle
	 * 
	 * @param evenementRecup : évènement récupéré de la base de donnée
	 * @param evenement : évènement modèle de test
	 */
	private void comparerEvenements(EvenementIdentifie evenementRecup, Evenement evenement) {
		
		assertEquals(evenementRecup.getNom(), evenement.getNom());
		assertEquals(evenementRecup.getDateDebut(), evenement.getDateDebut());
		assertEquals(evenementRecup.getDateFin(), evenement.getDateFin());
		
		//Comparaison des calendriers de rattachement
		List<Integer> idCalRecup = evenementRecup.getIdCalendriers();
		List<Integer> idCal = evenement.getIdCalendriers();
		
		assertEquals(idCalRecup.size(), idCal.size());
		
		for(Integer idRecup : idCalRecup) {
			boolean exists = false;
			for(Integer id : idCal) {
				if(id == idRecup)
					exists = true;
			}
			
			assertTrue(exists);
		}
		
		
	
		
		
	}
	


}
