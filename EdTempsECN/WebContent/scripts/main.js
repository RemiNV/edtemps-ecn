
/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html)
 * Placer jquery en dernier dans la liste (ne se charge pas dans les arguments de la fonction) */
require(["lib/davis.min", "RestManager", "jquery"], function(Davis, RestManager) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */
	
	restManager = new RestManager();
	
	if(!restManager.isConnected()) {
		// Chargement de l'interface de connexion
		require(["text!../templates/formulaire_connexion.html"], function(htmlFormulaireConnexion) {
			
			$(htmlFormulaireConnexion).appendTo($("#main_content").empty());
		});
	}
	else {
		// TODO : chargement des autres interfaces
	}
	
	return "test";
});