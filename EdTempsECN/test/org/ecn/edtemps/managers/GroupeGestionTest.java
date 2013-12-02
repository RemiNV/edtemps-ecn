package org.ecn.edtemps.managers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.exceptions.EdtempsException;
import org.ecn.edtemps.models.Groupe;
import org.ecn.edtemps.models.identifie.GroupeIdentifie;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
	
	private void comparerGroupes(Groupe groupe1, Groupe groupe2) {
		assertEquals(groupe1.getNom(), groupe2.getNom());
		assertEquals(groupe1.getParentId(), groupe2.getParentId());
		assertEquals(groupe1.getRattachementAutorise(), groupe2.getRattachementAutorise());
		assertEquals(groupe1.estCalendrierUnique(), groupe2.estCalendrierUnique());
		assertEquals(groupe1.estCours(), groupe2.estCours());
		
		// Comparaison des propriétaires
		List<Integer> lstProprietaires1 = groupe1.getIdProprietaires();
		List<Integer> lstProprietaires2 = groupe2.getIdProprietaires();
		assertEquals(lstProprietaires1.size(), lstProprietaires2.size());
		
		for(Integer prop : lstProprietaires1) {
			assertTrue(lstProprietaires2.contains(prop));
		}
		
		// Comparaison des IDs des calendriers
		List<Integer> lstCalendriers1 = groupe1.getIdCalendriers();
		List<Integer> lstCalendriers2 = groupe2.getIdCalendriers();
		assertEquals(lstCalendriers1.size(), lstCalendriers2.size());
		
		for(Integer cal : lstCalendriers1) {
			assertTrue(lstCalendriers2.contains(cal));
		}
	}
	

	@Test
	public void testSauverGetSupprimer() throws EdtempsException, SQLException {
		// TODO : exécuter le test dans une transaction pour ne pas avoir des groupes résiduels si il plante avant la suppression
		// Récupération d'un propriétaire au pif
		PreparedStatement requetePreparee = bddGestion.getConnection().prepareStatement(
				"SELECT utilisateur_id FROM edt.utilisateur LIMIT 1");
		int idUtilisateur = this.bddGestion.recupererId(requetePreparee, "utilisateur_id");
		
		assertTrue(idUtilisateur > 0); // La base de données doit contenir au moins un utilisateur qui peut être un propriétaire
		
		
		ArrayList<Integer> lstProprietaire = new ArrayList<Integer>(1);
		lstProprietaire.add(idUtilisateur);
		
		
		// Création d'un groupe bidon à ajouter
		Groupe groupe1 = new Groupe("groupe de \"test\" 123' haha", lstProprietaire, false, false, false);
		groupe1.setIdCalendriers(new ArrayList<Integer>());
		Groupe groupe2 = new Groupe("groupe de \"test\" 345' haha", lstProprietaire, true, true, false);
		groupe2.setIdCalendriers(new ArrayList<Integer>());
		
		int idGroupe1 = this.groupeGestionnaire.sauverGroupe("groupe de test 123 haha", null, false, false, lstProprietaire, 1);
		int idGroupe2 = this.groupeGestionnaire.sauverGroupe("groupe de test 345 haha", null, true, true, lstProprietaire, 1);
		
		GroupeIdentifie groupe1Recup = this.groupeGestionnaire.getGroupe(idGroupe1);
		GroupeIdentifie groupe2Recup = this.groupeGestionnaire.getGroupe(idGroupe2);
		
		comparerGroupes(groupe1, groupe1Recup);
		comparerGroupes(groupe2, groupe2Recup);
		
		// Suppression de la base
		this.groupeGestionnaire.supprimerGroupe(idGroupe1, true);
		this.groupeGestionnaire.supprimerGroupe(idGroupe2, true);
		
		assertNull(this.groupeGestionnaire.getGroupe(idGroupe1));
		assertNull(this.groupeGestionnaire.getGroupe(idGroupe2));
	}

}
