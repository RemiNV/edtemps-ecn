define(["lib/fullcalendar.translated.min"], function() {
	
	// Constructeur
	var EcranAccueil = function() {
	
	};
	
	EcranAccueil.prototype.init = function() {
		// Initialisation du calendrier
		$("#calendar").fullCalendar({
			firstDay: 0,
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
			height: Math.max(window.innerHeight - 150, 500)
		});
	};

	
	return EcranAccueil;
});