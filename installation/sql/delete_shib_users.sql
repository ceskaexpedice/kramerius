-- deleting mapping table
delete from group_user_assoc where user_id in (select user_id from user_entity where loginname like '_shi%');

-- deletinf from user_entity
delete from user_entity where loginname like '_shi%';

-- delete from active users mapping table
delete from active_users_2_roles where active_users_id in (select active_users_id from active_users where loginname like '_shi%');

-- delete from profiles
delete from profiles where active_users_id in (select active_users_id from active_users where loginname like '_shi%');

-- delete from active_users
delete from active_users  where loginname like '_shi%';
