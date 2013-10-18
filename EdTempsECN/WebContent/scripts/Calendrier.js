define(["lib/fullcalendar.translated.min"], function() {

	var Calendrier = function(onViewRender) {
		var date = new Date();
		var d = date.getDate();
		var m = date.getMonth();
		var y = date.getFullYear();
		
		var me = this;

		this.jqCalendar = $("#calendar");
		// Initialisation du calendrier
		this.jqCalendar.fullCalendar({
			weekNumbers: true,
			weekNumberTitle: "Sem.",
			firstDay: 0,
			editable: true,
			defaultView: "agendaWeek",
			timeFormat: "HH'h'(:mm)",
			axisFormat: "HH'h'(:mm)",
			titleFormat: {
				month: 'MMMM yyyy',                             // Septembre 2013
				week: "d [ MMM] [ yyyy] '&#8212;' {d MMM yyyy}", // 7 - 13 Sep 2013
				day: 'dddd d MMM yyyy'                  // Mardi 8 Sep 2013
			},
			columnFormat: {
				month: 'ddd',    // Lun
				week: 'ddd d/M', // Lun 7/9
				day: 'dddd M/d'  // Lundi 7/9
			},
			header: {
				right: '',
				center: 'title',
				left: 'prev,next today month,agendaWeek,agendaDay'
			},
			height: Math.max(window.innerHeight - 150, 500),
			windowResize: function(view) {
				me.jqCalendar.fullCalendar("option", "height", Math.max(window.innerHeight - 150, 500));
			},
			viewRender: onViewRender
		});
	};
	
	/**
	 * Remplit le calendrier avec les abonnements fournis.
	 * Le format de l'objet abonnements est le même que le résultat de EvenementGestion.listerEvenementsAbonnement */
	Calendrier.prototype.remplirCalendrier = function(abonnements) {
		var parsedEvents = this.parseEvents(abonnements);
		this.jqCalendar.fullCalendar("removeEvents");
		
		for(var i=0, max = parsedEvents.length; i<max; i++) {
			this.jqCalendar.fullCalendar("renderEvent", parsedEvents[i]);
		}
	};
	
	/**
	 * Retourne un tableau d'évènements compatibles fullCalendar
	 * a partir d'un objet d'abonnements */
	Calendrier.prototype.parseEvents = function(abonnements) {
		
		var evenements = abonnements.evenements;
		var res = Array();
		for(var i=0, max = evenements.length; i<max; i++) {
			// Chaîne de salles
			var strSalles = "";
			for(var j=0, maxj = evenements[i].salles.length; j<maxj; j++) {
				if(j != 0)
					strSalles += ", ";
				strSalles += evenements[i].salles[j].nom;
			}
		
			res[i] = {
				id: evenements[i].id,
				title: evenements[i].nom,
				start: new Date(evenements[i].dateDebut),
				end: new Date(evenements[i].dateFin),
				salle: strSalles,
				allDay: false
			};
		}
	
		return res;
	};
	
	/**
	 * Récupération de l'intervalle de jours visibles du calendrier.
	 * Renvoie un objet contenant les deux attributs "begin" et "end".
	 * Ces attributs sont les dates de début et de fin.
	 * "end" est situé juste après le dernier jour visible; */
	Calendrier.prototype.getDisplayedInterval = function() {
		// Jour situé dans l'intervalle affiché
		view = this.jqCalendar.fullCalendar("getView");
		
		return { begin: view.visStart, end: view.visEnd };
	};


	return Calendrier;
});