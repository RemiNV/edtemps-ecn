define(["lib/fullcalendar.translated.min"], function() {
	
	// Constructeur
	var EcranAccueil = function() {
		this._idVue = EcranAccueil.ID_VUE_MES_ABONNEMENTS;
	};
	
	EcranAccueil.prototype.init = function() {
	
		var date = new Date();
		var d = date.getDate();
		var m = date.getMonth();
		var y = date.getFullYear();

		var jqCalendar = $("#calendar");
		// Initialisation du calendrier
		jqCalendar.fullCalendar({
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
				jqCalendar.fullCalendar("option", "height", Math.max(window.innerHeight - 150, 500));
			},
			events: [{
						title: 'Meeting',
						start: new Date(y, m, d, 10, 30),
						allDay: false
					},
					{
						title: 'Repas',
						start: new Date(y, m, d, 12, 0),
						end: new Date(y, m, d, 14, 0),
						allDay: false
					},
					{
						title: 'Cours de GELOL',
						start: new Date(y, m, d+1, 19, 0),
						end: new Date(y, m, d+1, 22, 30),
						allDay: false
					}]
		});
		
		// Initialisation des listeners
		$("#btn_gerer_agendas").click(function(e) {
			Davis.location.assign("parametres");
		});
		
		this.setVue("mes_abonnements");
	};
	
	EcranAccueil.prototype.setVue = function(vue) {
	
		// Sélection de l'onglet
		$("#nav_vue_agenda li").removeClass("selected");
		switch(vue) {
		case "vue_groupe":
			$("#nav_vue_agenda #tab_vue_groupe").addClass("selected");
			break;
		case "vue_salle":
			$("#nav_vue_agenda #tab_vue_salle").addClass("selected");
			break;
		
		case "mes_evenements":
			$("#nav_vue_agenda #tab_mes_evenements").addClass("selected");
			break;
			
		case "mes_abonnements":
		default:
			$("#nav_vue_agenda #tab_mes_abonnements").addClass("selected");
			break;
		}
	};

	
	return EcranAccueil;
});