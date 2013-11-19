package org.ecn.edtemps.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.EvenementGestion;
import org.ecn.edtemps.models.Evenement;
import org.ecn.edtemps.models.identifie.*;
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
	 * Methode pour comparer l'evenement récupéré de la base de donnée et les informations ayant servi à créer cet évenement
	 * 
	 * @param evenementRecup : évènement récupéré de la base de donnée
	 * @param nom : nom de l'évenement créé
	 * @param dateDebut : date du début de l'évenement créé
	 * @param dateFin : date de fin de l'évenement créé
	 * @param idCalendriers : liste des ids des calendriers auxquels est rattaché l'évenement créé
	 * @param idSalles : liste des ids des salles dans lesquelles a lieu l'évenement créé
	 * @param idIntervenants : liste des ids des intervenants de l'évenement créé
	 * @param idResponsables : liste des ids des responsables de l'évenement créé
	 */
	private void comparerEvenements(EvenementIdentifie evenementRecup, String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<Integer> idSalles, 
			List<Integer> idIntervenants, List<Integer> idResponsables) {
		
		assertEquals(evenementRecup.getNom(), nom);
		assertEquals(evenementRecup.getDateDebut(), dateDebut);
		assertEquals(evenementRecup.getDateFin(), dateFin);
		
		//Comparaison des calendriers de rattachement
		List<Integer> idCalRecup = evenementRecup.getIdCalendriers();
		
		assertEquals(idCalRecup.size(), idCalendriers.size());
		
		for(Integer idRecup : idCalRecup) {
			boolean exists = false;
			for(Integer id : idCalendriers) {
				if(id == idRecup)
					exists = true;
			}
			
			assertTrue(exists);
		}
		
		//Comparaison des salles rattachées à l'évenement
			List<SalleIdentifie> sallesCalRecup = evenementRecup.getSalles();
				
				assertEquals(sallesCalRecup.size(), idSalles.size());
				
				for(SalleIdentifie salleRecup : sallesCalRecup) {
					boolean exists = false;
					for(Integer idSalle : idSalles) {
						if(idSalle == salleRecup.getId())
							exists = true;
					}
					
					assertTrue(exists);
				}
	
				//Comparaison des intervenants rattachés à l'évenement
				List<UtilisateurIdentifie> intervenantsCalRecup = evenementRecup.getIntervenants();
					
					assertEquals(intervenantsCalRecup.size(), idIntervenants.size());
					
					for(UtilisateurIdentifie intervenantRecup : intervenantsCalRecup) {
						boolean exists = false;
						for(Integer idIntervenant : idIntervenants) {
							if(idIntervenant == intervenantRecup.getId())
								exists = true;
						}
						
						assertTrue(exists);
					}
		
					//Comparaison des responsables rattachés à l'évenement
					List<UtilisateurIdentifie> responsablesCalRecup = evenementRecup.getResponsables();
						
						assertEquals(responsablesCalRecup.size(), idResponsables.size());
						
						for(UtilisateurIdentifie responsableRecup : responsablesCalRecup) {
							boolean exists = false;
							for(Integer idResponsable : idResponsables) {
								if(idResponsable == responsableRecup.getId())
									exists = true;
							}
							
							assertTrue(exists);
						}
	}
	

	@Test
	public void testAjoutSuppressionEvenement() throws Exception {
		
		
		
		
	}
	
	

}
