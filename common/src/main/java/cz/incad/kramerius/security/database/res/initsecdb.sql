-- creating sequence for column USER_ENTITY.USER_ID --
CREATE SEQUENCE USER_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating sequence for column GROUP_ENTITY.GROUP_ID --
CREATE SEQUENCE GROUP_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating sequence for column GROUP_USER_ASSOC.GROUP_USER_ASSOC_ID --
CREATE SEQUENCE GROUP_USER_ASSOC_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating sequence for column CRITERIUM_PARAM_ENTITY.CRIT_PARAM_ID --
CREATE SEQUENCE CRIT_PARAM_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating sequence for column RIGHTS_CRITERIUM_ENTITY.CRIT_ID --
CREATE SEQUENCE CRIT_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating sequence for column RIGHT_ENTITY.RIGHT_ID --
CREATE SEQUENCE RIGHT_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

-- creating table USER_ENTITY --
CREATE TABLE USER_ENTITY (
   USER_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   NAME VARCHAR(255) NOT NULL,
   SURNAME VARCHAR(255) NOT NULL,
   LOGINNAME VARCHAR(255) NOT NULL,
   PSWD VARCHAR(255),
   EMAIL VARCHAR(255),
   ORGANISATION VARCHAR(255),
   PERSONAL_ADMIN_ID INT, PRIMARY KEY (USER_ID));

CREATE INDEX UNAME_IDX ON USER_ENTITY (NAME);

CREATE INDEX SURNAME_IDX ON USER_ENTITY (SURNAME);

CREATE INDEX LOGINNAME_IDX ON USER_ENTITY (LOGINNAME);

CREATE INDEX PASSWORD_IDX ON USER_ENTITY (PSWD);

-- creating table GROUP_ENTITY --
CREATE TABLE GROUP_ENTITY (
   GROUP_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   GNAME VARCHAR(255) NOT NULL,
   PERSONAL_ADMIN_ID INT,DESCRIPTION VARCHAR(1024), PRIMARY KEY (GROUP_ID));

CREATE UNIQUE INDEX GNAME_IDX ON GROUP_ENTITY (GNAME);

-- creating table GROUP_USER_ASSOC --
CREATE TABLE GROUP_USER_ASSOC (
   GROUP_USER_ASSOC_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   USER_ID INT,
   GROUP_ID INT, PRIMARY KEY (GROUP_USER_ASSOC_ID));

-- creating table CRITERIUM_PARAM_ENTITY --
CREATE TABLE CRITERIUM_PARAM_ENTITY (
   CRIT_PARAM_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   VALS VARCHAR(1024) NOT NULL,
   LONG_DESC VARCHAR(1024) NOT NULL,
   SHORT_DESC VARCHAR(256) NOT NULL, PRIMARY KEY (CRIT_PARAM_ID));

-- creating table RIGHTS_CRITERIUM_ENTITY --
CREATE TABLE RIGHTS_CRITERIUM_ENTITY (
   CRIT_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   TYPE INT,
   QNAME VARCHAR(255) NOT NULL,
   citeriumParam INT, PRIMARY KEY (CRIT_ID));

-- creating table RIGHT_ENTITY --
CREATE TABLE RIGHT_ENTITY (
   RIGHT_ID INT NOT NULL,
   UPDATE_TIMESTAMP TIMESTAMP,
   UUID VARCHAR(255) NOT NULL,
   ACTION VARCHAR(255) NOT NULL,
   RIGHTS_CRIT INT,
   USER_ID INT,
   GROUP_ID INT,
   FIXED_PRIORITY INT, PRIMARY KEY (RIGHT_ID));

CREATE INDEX UUID_IDX ON RIGHT_ENTITY (UUID);

CREATE INDEX ACTION_IDX ON RIGHT_ENTITY (ACTION);

-- creating foreign key constraint GROUP_USER_ASS_USER_ID_FK --
ALTER TABLE GROUP_USER_ASSOC ADD CONSTRAINT GROUP_USER_ASS_USER_ID_FK FOREIGN KEY (USER_ID) REFERENCES USER_ENTITY (USER_ID);

-- creating foreign key constraint GROUP_USER_ASS_GROUP_ID_FK --
ALTER TABLE GROUP_USER_ASSOC ADD CONSTRAINT GROUP_USER_ASS_GROUP_ID_FK FOREIGN KEY (GROUP_ID) REFERENCES GROUP_ENTITY (GROUP_ID);

-- creating foreign key constraint RIGHTS_CRITERI_citeriumPara_FK --
ALTER TABLE RIGHTS_CRITERIUM_ENTITY ADD CONSTRAINT RIGHTS_CRITERI_citeriumPara_FK FOREIGN KEY (citeriumParam) REFERENCES CRITERIUM_PARAM_ENTITY (CRIT_PARAM_ID);

-- creating foreign key constraint RIGHT_ENTITY_RIGHTS_CRIT_FK --
ALTER TABLE RIGHT_ENTITY ADD CONSTRAINT RIGHT_ENTITY_RIGHTS_CRIT_FK FOREIGN KEY (RIGHTS_CRIT) REFERENCES RIGHTS_CRITERIUM_ENTITY (CRIT_ID);

-- creating foreign key constraint RIGHT_ENTITY_user_FK --
ALTER TABLE RIGHT_ENTITY ADD CONSTRAINT RIGHT_ENTITY_user_FK FOREIGN KEY (USER_ID) REFERENCES USER_ENTITY (USER_ID);

-- creating foreign key constraint RIGHT_ENTITY_group_FK --
ALTER TABLE RIGHT_ENTITY ADD CONSTRAINT RIGHT_ENTITY_group_FK FOREIGN KEY (GROUP_ID) REFERENCES GROUP_ENTITY (GROUP_ID);

-- creating foreign key constraint USER_ENTITY_PERSONAL_ADM_FK --
ALTER TABLE USER_ENTITY ADD CONSTRAINT USER_ENTITY_PERSONAL_ADM_FK FOREIGN KEY (PERSONAL_ADMIN_ID) REFERENCES GROUP_ENTITY (GROUP_ID);




-- view pro skupiny jednoho uzivatele
create view user_group_mapping as 
select ge.group_id, ge.gname, ge.personal_admin_id, guass.user_id from group_user_assoc guass
join group_entity ge on (ge.group_id=guass.group_id);

-- view pro vylistovani uzivatelu ve skupine
create view group_users_mapping as 
select ue.user_id,ue.name, ue.surname, ue.loginname, guass.group_id,ge.personal_admin_id, ue.personal_admin_id as uepersonaladmin 
from group_user_assoc guass
join user_entity ue on (ue.user_id=guass.user_id)
join group_entity ge on (ge.group_id=guass.group_id);


-- skupina
insert into group_entity(group_id,gname) 
values(nextval('group_id_sequence'),'common_users'); 


-- skupina k4 admins
insert into group_entity(group_id,gname) 
values(nextval('group_id_sequence'),'k4_admins'); 



-- administrace skupin
-- -- common administruje k4_admins
update group_entity set personal_admin_id=2 where group_id=1;


-- krameriusAdmin
insert into user_entity (user_id,"name", surname,loginname,pswd)
values(nextval('user_id_sequence'), 'kramerius','admin','krameriusAdmin','cqEk6m3f+bpT50XDAha1r5Wa7Q0=');

-- asociace (uzvivatel, skupina)
-- -- krameriusAdmin = k4_admins
insert into group_user_assoc(group_user_assoc_id, user_id, group_id)
values(nextval('group_user_assoc_id_sequence'),1,2);


-- insert into params
-- localhosts
insert into CRITERIUM_PARAM_ENTITY (CRIT_PARAM_ID, VALS, LONG_DESC, SHORT_DESC) 
VALUES(nextval('CRIT_PARAM_ID_SEQUENCE'), '127.*;localhost;0.*', 'Locahosts','Localhosts');

-- prirazeni policy kriteria 
insert into RIGHTS_CRITERIUM_ENTITY(CRIT_ID, QNAME, "type") 
VALUES(nextval('CRIT_ID_SEQUENCE'), 'cz.incad.kramerius.security.impl.criteria.PolicyFlag', 1);

-- prirazeni default ip filtru
insert into RIGHTS_CRITERIUM_ENTITY(CRIT_ID, QNAME,citeriumParam, "type") 
VALUES(nextval('CRIT_ID_SEQUENCE'), 'cz.incad.kramerius.security.impl.criteria.DefaultIPAddressFilter',1, 1);


-- pravo pro policy flag
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, RIGHTS_CRIT,GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','read', 1,1);

-- pravo pro benevoletni ip filter
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, RIGHTS_CRIT,GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','read', 2,1);


-- administratorske akce
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','import',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION,GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','convert',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','replicationrights', 2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','enumerator',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','reindex',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','replikator_periodicals',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','replikator_monographs',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','delete',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','export',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','setprivate',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','setpublic',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','manage_lr_process',2);


insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','editor',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','administrate',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID)
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','export_k4_replications',2);

insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID)
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','import_k4_replications',2);

-- prava pro rights editor 
-- -- k4_admins
insert into RIGHT_ENTITY(RIGHT_ID, UUID,ACTION, GROUP_ID) 
VALUES(nextval('RIGHT_ID_SEQUENCE '), 'uuid:1','rightsadmin',2);



