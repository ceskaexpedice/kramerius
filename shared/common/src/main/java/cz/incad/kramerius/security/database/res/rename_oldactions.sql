
-- rename 
update right_entity set "action"='a_read' where "action"='read';

update right_entity set "action"='a_pdf_read' where "action"='pdf_resource';

update right_entity set "action"='a_index' where "action"='reindex';

update right_entity set "action"='a_delete' where "action"='delete';

update right_entity set "action"='a_process_edit' where "action"='manage_lr_process';

update right_entity set "action"='a_import' where "action"='import';

update right_entity set "action"='a_set_accessibility' where "action"='setprivate';

update right_entity set "action"='a_set_accessibility' where "action"='setpublic';

update right_entity set "action"='a_statistics' where "action"='show_statictics';

update right_entity set "action"='a_statistics_edit' where "action"='manage_statistics';



-- delete unused
delete from right_entity where "action"='show_print_menu';
delete from right_entity where "action"='show_client_print_menu';
delete from right_entity where "action"='show_client_pdf_menu';
delete from right_entity where "action"='display_admin_menu';
delete from right_entity where "action"='criteria_rights_manage';
delete from right_entity where "action"='convert';
delete from right_entity where "action"='replicationrights';
delete from right_entity where "action"='enumerator';
delete from right_entity where "action"='replikator_periodicals';
delete from right_entity where "action"='replikator_monographs';
