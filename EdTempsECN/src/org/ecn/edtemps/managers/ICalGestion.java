package org.ecn.edtemps.managers;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang3.StringUtils;
import org.ecn.edtemps.exceptions.DatabaseException;
import org.ecn.edtemps.models.identifie.EvenementIdentifie;

/**
 * Classe de gestion de l'export ICal
 * 
 * @author Rémi
 */
public class ICalGestion {

	/** Gestionnaire de base de données */
	private BddGestion bdd;
	
	/**
	 * Initialise un gestionnaire d'export ICal
	 * @param bdd Gestionnaire de base de données à utiliser
	 */
	public ICalGestion(BddGestion bdd) {
		this.bdd = bdd;
	}
	
	
	/**
	 * Génération du contenu d'un fichier iCal contenant tous les abonnements de l'utilisateur
	 * @param idUtilisateur
	 * @return
	 * @throws DatabaseException Erreur de communication avec la base de données
	 */
	public String genererICalAbonnements(int idUtilisateur) throws DatabaseException {
		
		// Récupération des évènements
		EvenementGestion evenementGestion = new EvenementGestion(bdd);
		
		// On prend un an d'évènement centré sur la date actuelle
		GregorianCalendar dateDebut = new GregorianCalendar();
		dateDebut.add(GregorianCalendar.MONTH, -6);
		
		GregorianCalendar dateFin = new GregorianCalendar();
		dateFin.add(GregorianCalendar.MONTH, 6);
		
		
		ArrayList<EvenementIdentifie> evenements = evenementGestion.listerEvenementsUtilisateur(idUtilisateur, dateDebut.getTime(), dateFin.getTime(), true, false);
		
		Calendar calendar = new Calendar();
		
		calendar.getProperties().add(new ProdId("-//Ecole Centrale Nantes//Emploi du temps ECN//FR"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		
		for(EvenementIdentifie even : evenements) {
			VEvent calEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(even.getDateDebut()), new net.fortuna.ical4j.model.DateTime(even.getDateFin()), even.getNom());
			
			String strSalles = StringUtils.join(even.getSalles(), ", ");
			
			calEvent.getProperties().add(new Location(strSalles));
			
			calendar.getComponents().add(calEvent);
		}
		
		return calendar.toString();
	}
}
