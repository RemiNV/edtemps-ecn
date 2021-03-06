// Utilisation d'une syntaxe permettant le chargement de la config avant requirejs
var require = {	

	baseUrl: "scripts/",
	
	/* Configuration des bibliothèques compatibles AMD (Asynchronous Module Definition, pour requirejs entre autres) */
	paths: {
		jquery: "lib/jquery-1.10.2.min",
		jqueryui: "lib/jquery-ui-1.10.3.notheme.min",
		jqueryrotate: "lib/jquery-rotate.2.3.min",
		jquerymultiselect: "lib/jquery.multi-select",
		jquerymaskedinput: "lib/jquery.maskedinput.min",
		text: "lib/text",
		underscore: "lib/underscore-min",
		davis: "lib/davis.min",
		underscore: "lib/underscore-min",
		jqueryquicksearch: "lib/jquery.quicksearch", // Bibliothèque effectuant le tri dans le jquerymultiselect
		jquerycombobox: "lib/jquery.ui.combobox",
		datepicker: "lib/datepicker",
		moment: "lib/moment.min",
		moment_fr: "lib/moment.fr"
	},

	/* Configuration des bibliothèques non AMD (non compatibles avec 
	 * les gestionnaires de dépendances tels que requirejs)
	 * Voir http://requirejs.org/docs/api.html#config-shim (11/10/2013) */
	shim: {
		/* Bibliothèque Davis (plugin hashrouting nécessitant Davis)
		 * Utilisation future : require(["davis"], function(Davis) { ... }); */
		"davis": {
			deps: ["jquery"], // jquery nécessaire pour davis.js, et bibliothèque de base
			exports: "Davis" // Utilisation du nom "Davis" en argument de la fonction le récupérant
		},
		
		"lib/davis.hashrouting": {
			deps: ["davis"], // jquery nécessaire pour davis.js, et bibliothèque de base
			exports: "Davis" // Utilisation du nom "Davis" en argument de la fonction le récupérant
		},
		
		"jqueryquicksearch": {
			deps: ["jquery"]
		},
		
		"jqueryui": {
			deps: ["jquery"]
		},
		
		"lib/fullcalendar.translated.min": {
			deps: ["jqueryui"]
		},
				
		"jquerymultiselect": {
			deps: ["jquery"]
		},
		
		"jquerymaskedinput": {
			deps: ["jquery"]
		},
		
		"jquerycombobox": {
			deps: ["jqueryui"]
		},
		
		"underscore": {
			exports: "_"
		},
		
		"datepicker": {
			deps: ["jquery"]
		}
	}

};