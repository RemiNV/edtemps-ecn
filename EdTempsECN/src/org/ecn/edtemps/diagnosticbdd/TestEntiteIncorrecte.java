package org.ecn.edtemps.diagnosticbdd;

import java.sql.SQLException;
import java.util.ArrayList;

import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.managers.BddGestion;

public abstract class TestEntiteIncorrecte extends TestBdd {

	public TestEntiteIncorrecte(String nom, int id, String repairMessage) {
		super(nom, id, repairMessage);
	}

	protected ArrayList<Integer> getLstIncorrects(BddGestion bdd) throws DatabaseException {
		try {
			return bdd.recupererIds(bdd.getConnection().prepareStatement(getRequeteListing()), getColonneId());
		}
		catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public final TestBddResult test(BddGestion bdd) throws DatabaseException {
		ArrayList<Integer> lstIncorrects = getLstIncorrects(bdd);
		
		if(lstIncorrects.isEmpty()) {
			return new TestBddResult(TestBddResultCode.OK, "Aucune entité incorrecte trouvée", this);
		}
		else {
			return new TestBddResult(TestBddResultCode.WARNING, "Des entités incorrectes ont été trouvées : ID " + DiagnosticsBdd.getStrPremiersIds(lstIncorrects), this);
		}
	}

	@Override
	public final String repair(BddGestion bdd) throws DatabaseException {
		
		ArrayList<Integer> lstIncorrects = getLstIncorrects(bdd);
		return reparerIncorrects(lstIncorrects);
	}
	
	protected abstract String reparerIncorrects(ArrayList<Integer> ids) throws DatabaseException;
	
	protected abstract String getRequeteListing();
	protected abstract String getColonneId();

}
