
/* Fonction d'entrée du programme. 
 * Le plugin davis est appelé par le mot-clé "davis" (configuré dans index.html) */
require(["lib/davis.min", "jquery"], function(Davis) {
	/* Davis est chargé de manière locale avec le mot-clé "Davis" dans cette fonction (passé en argument) : 
	 * le plugin est configuré pour être chargé de cette manière dans le index.html
	 * 
	 * jquery est accessible de manière globale par $ (mais il faut tout de même préciser la dépendance
	 * dans les arguments de require() !), pour ne pas avoir de problème de dépendances (avec jQuery UI notamment) */
	
	
	return "test";
});