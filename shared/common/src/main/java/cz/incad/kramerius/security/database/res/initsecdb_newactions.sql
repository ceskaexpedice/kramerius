-- roles
-- kramerius-admin
INSERT INTO group_entity (group_id, gname) SELECT nextval('group_id_sequence'), 'kramerius_admin' WHERE NOT EXISTS ( SELECT group_id FROM group_entity WHERE gname = 'kramerius_admin');


-- akce pro kramerius_admin
-- a_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_pdf_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_pdf_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_import
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_import', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_delete
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_delete', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_process_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_process_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_process_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_process_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_owner_process_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_owner_process_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_index
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_index', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';
 
-- a_rebuild_processing_index   
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_rebuild_processing_index', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_import
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_import', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';
    
-- a_accessibility
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','a_set_accessibility', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_statistics
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_statistics', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_statistics_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_statistics_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';
    
-- a_export_replications
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_export_replications', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_import_replications
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_import_replications', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';
    
-- a_rights_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_rights_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_criteria_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_criteria_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- collections_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_collections_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- collections_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_collections_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_able_tobe_part_of_collections
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_able_tobe_part_of_collections', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_admin_read
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_admin_read', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- generate nkplogs
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_generate_nkplogs', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_roles_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_roles_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- a_users_edit
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_users_edit', 'kramerius_admin',group_id  from group_entity WHERE gname = 'kramerius_admin';

-- statistics 
-- common_users - read statistics
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,  "role",GROUP_ID) SELECT nextval('RIGHT_ID_SEQUENCE'), 'uuid:1','a_statistics', 'common_users',group_id  from group_entity WHERE gname = 'common_users';


