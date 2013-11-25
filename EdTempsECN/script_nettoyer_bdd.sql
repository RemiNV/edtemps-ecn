﻿DROP SEQUENCE IF EXISTS edt.salle_salle_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.materiel_materiel_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.typecalendrier_typecal_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.groupeparticipant_groupeparticipant_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.evenement_eve_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.matiere_matiere_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.calendrier_cal_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.utilisateur_utilisateur_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.typeutilisateur_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.droits_droits_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edt.administrateurs_admin_id_seq CASCADE;

DROP TABLE IF EXISTS edt.salle CASCADE;
DROP TABLE IF EXISTS edt.materiel CASCADE;
DROP TABLE IF EXISTS edt.contientmateriel CASCADE;
DROP TABLE IF EXISTS edt.typecalendrier CASCADE;
DROP TABLE IF EXISTS edt.groupeparticipant CASCADE;
DROP TABLE IF EXISTS edt.evenement CASCADE;
DROP TABLE IF EXISTS edt.alieuensalle CASCADE;
DROP TABLE IF EXISTS edt.necessitemateriel CASCADE; -- Table supprimée plus tard du schéma
DROP TABLE IF EXISTS edt.matiere CASCADE;
DROP TABLE IF EXISTS edt.calendrier CASCADE;
DROP TABLE IF EXISTS edt.calendrierappartientgroupe CASCADE;
DROP TABLE IF EXISTS edt.evenementappartient CASCADE;
DROP TABLE IF EXISTS edt.utilisateur CASCADE;
DROP TABLE IF EXISTS edt.proprietairegroupeparticipant CASCADE;
DROP TABLE IF EXISTS edt.abonnegroupeparticipant CASCADE;
DROP TABLE IF EXISTS edt.responsableevenement CASCADE;
DROP TABLE IF EXISTS edt.intervenantevenement CASCADE;
DROP TABLE IF EXISTS edt.proprietairecalendrier CASCADE;
DROP TABLE IF EXISTS edt.proprietairematiere CASCADE;
DROP TABLE IF EXISTS edt.typeutilisateur CASCADE;
DROP TABLE IF EXISTS edt.estdetype CASCADE;
DROP TABLE IF EXISTS edt.droits CASCADE;
DROP TABLE IF EXISTS edt.aledroitde CASCADE;
DROP TABLE IF EXISTS edt.administrateurs CASCADE;

DROP FUNCTION IF EXISTS edt.update_groupeparticipant_aparentcours();
DROP FUNCTION IF EXISTS edt.set_groupeparticipant_aparentcours();