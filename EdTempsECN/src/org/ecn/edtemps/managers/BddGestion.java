package org.ecn.edtemps.managers;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 
 * @author Joffrey
 * 
 */
public class BddGestion {

	public Connection connect() {

		Connection conn = null;

		try {

			Class.forName("org.postgresql.Driver");
			System.out.println("Driver chargé");

			String url = "jdbc:postgresql://localhost:5432/edtemps-ecn";
			String user = "edtemps-ecn";
			String passwd = "passwordEdtemps";

			conn = DriverManager.getConnection(url, user, passwd);
			System.out.println("Connexion OK");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;

	}

}
