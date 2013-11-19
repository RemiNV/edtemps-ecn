DROP SEQUENCE edt.salle_salle_id_seq CASCADE;
DROP SEQUENCE edt.materiel_materiel_id_seq CASCADE;
DROP SEQUENCE edt.typecalendrier_typecal_id_seq CASCADE;
DROP SEQUENCE edt.groupeparticipant_groupeparticipant_id_seq CASCADE;
DROP SEQUENCE edt.evenement_eve_id_seq CASCADE;
DROP SEQUENCE edt.matiere_matiere_id_seq CASCADE;
DROP SEQUENCE edt.calendrier_cal_id_seq CASCADE;
DROP SEQUENCE edt.utilisateur_utilisateur_id_seq CASCADE;
DROP SEQUENCE edt.typeutilisateur_type_id_seq CASCADE;
DROP SEQUENCE edt.droits_droits_id_seq CASCADE;
DROP SEQUENCE edt.administrateurs_admin_id_seq CASCADE;

DROP TABLE edt.salle CASCADE;
DROP TABLE edt.materiel CASCADE;
DROP TABLE edt.contientmateriel CASCADE;
DROP TABLE edt.typecalendrier CASCADE;
DROP TABLE edt.groupeparticipant CASCADE;
DROP TABLE edt.evenement CASCADE;
DROP TABLE edt.alieuensalle CASCADE;
DROP TABLE IF EXISTS edt.necessitemateriel CASCADE; -- Table supprimée plus tard du schéma
DROP TABLE edt.matiere CASCADE;
DROP TABLE edt.calendrier CASCADE;
DROP TABLE edt.calendrierappartientgroupe CASCADE;
DROP TABLE edt.evenementappartient CASCADE;
DROP TABLE edt.utilisateur CASCADE;
DROP TABLE edt.proprietairegroupeparticipant CASCADE;
DROP TABLE edt.abonnegroupeparticipant CASCADE;
DROP TABLE edt.responsableevenement CASCADE;
DROP TABLE edt.intervenantevenement CASCADE;
DROP TABLE edt.proprietairecalendrier CASCADE;
DROP TABLE edt.proprietairematiere CASCADE;
DROP TABLE edt.typeutilisateur CASCADE;
DROP TABLE edt.estdetype CASCADE;
DROP TABLE edt.droits CASCADE;
DROP TABLE edt.aledroitde CASCADE;
DROP TABLE edt.administrateurs CASCADE;

DROP FUNCTION edt.update_groupeparticipant_aparentcours();
DROP FUNCTION edt.set_groupeparticipant_aparentcours();