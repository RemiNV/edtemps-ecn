package org.ecn.edtemps.managers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Classe de test de AdministrateurGestion
 * 
 * @author Joffrey
 */
public class AdministrateurGestionTest {

	BddGestion bdd;
	AdministrateurGestion gestionAdmin;

	@Before
	public void initAllTests() throws DatabaseException {
		this.bdd = new BddGestion();
		this.gestionAdmin = new AdministrateurGestion(this.bdd);
	}
	
	@Test
	public void testListerAdministrateurs() throws DatabaseException, SQLException {
		
		// Requête à la main pour vérifier que la méthode fait bien son travail
		ResultSet requete = bdd.executeRequest("SELECT COUNT(*) FROM edt.administrateurs");
		requete.next();
		int nbAdministrateurs = requete.getInt(1);

		
		// Vérifie qu'il y a bien le bon nombre d'administrateurs
		Assert.assertEquals(nbAdministrateurs ,gestionAdmin.listerAdministrateurs().size());
	}
	
	@Test
	public void testSeConnecter() throws SQLException, DatabaseException, InvalidKeyException, NoSuchAlgorithmException {
		
		// Ajoute un administrateur dans la base pour le test
		Random rand = new Random();
		String login = String.valueOf(rand.nextLong());
		String password = "password";
		String cryptedPassword = UtilisateurGestion.hmac_sha256("Chaine de cryptage", password);
		ResultSet ajoutRequete = bdd.executeRequest("INSERT INTO edt.administrateurs (admin_login, admin_password) VALUES ('"+login+"', '"+cryptedPassword+"') RETURNING admin_id");
		ajoutRequete.next();
		int idInsertion = ajoutRequete.getInt(1);
		ajoutRequete.close();
		
		// Test de connexion
		Assert.assertTrue(gestionAdmin.seConnecter(login, password));
		
		// Suppression de l'administrateur pour éviter les impacts
		bdd.executeRequest("DELETE FROM edt.administrateurs WHERE admin_id="+idInsertion);
	}
	
	@Test
	public void testAjouterAdministrateur() throws DatabaseException, SQLException, InvalidKeyException, NoSuchAlgorithmException {

		// Ajoute un administrateur dans la base
		Random rand = new Random();
		String login = String.valueOf(rand.nextLong());
		String password = "password";
		int id = gestionAdmin.ajouterAdministrateur(login, password);
		
		// Vérification que l'administrateur a bien été supprimé
		ResultSet verif = bdd.executeRequest("SELECT * FROM edt.administrateurs WHERE admin_id="+id);
		Assert.assertTrue(verif.next());
		
		// Suppression de l'administrateur pour éviter les impacts
		bdd.executeRequest("DELETE FROM edt.administrateurs WHERE admin_id="+id);

	}
	
	@Test
	public void testSupprimerAdministrateur() throws DatabaseException, SQLException {
		// Ajoute un administrateur dans la base
		Random rand = new Random();
		String login = String.valueOf(rand.nextLong());
		String password = "password";
		ResultSet ajoutRequete = bdd.executeRequest("INSERT INTO edt.administrateurs (admin_login, admin_password) VALUES ('"+login+"', '"+password+"') RETURNING admin_id");
		ajoutRequete.next();
		int idInsertion = ajoutRequete.getInt(1);
		ajoutRequete.close();

		// Supprime l'administrateur ajouté
		this.gestionAdmin.supprimerAdministrateur(idInsertion);

		// Vérification que l'administrateur a bien été supprimé
		ResultSet verif = bdd.executeRequest("SELECT * FROM edt.administrateurs WHERE admin_id="+idInsertion);
		Assert.assertFalse(verif.next());
	}

}
