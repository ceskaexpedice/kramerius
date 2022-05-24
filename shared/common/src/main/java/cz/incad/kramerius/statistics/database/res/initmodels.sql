-- materialized views 

-- monograph
create MATERIALIZED view _model_monograph as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'monograph'::text and l.stat_action='READ';

--periodical
create MATERIALIZED view _model_periodical as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'periodical'::text and l.stat_action='READ';

--article 
create MATERIALIZED view _model_article as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'article'::text and l.stat_action='READ';

-- convolute
create MATERIALIZED view _model_convolute as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'convolute'::text and l.stat_action='READ';

-- map
create MATERIALIZED view _model_map as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'map'::text and l.stat_action='READ';

-- graphic
create MATERIALIZED view _model_graphic as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'graphic'::text and l.stat_action='READ';

-- archive
create MATERIALIZED view _model_archive as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'archive'::text and l.stat_action='READ';

-- manuscript
create MATERIALIZED view _model_manuscript as select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'manuscript'::text and l.stat_action='READ';

-- soundrecording
create MATERIALIZED view _model_soundrecording as  select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'soundrecording'::text and l.stat_action='READ';

-- collection
create MATERIALIZED view _model_collection as  select d.pid as pid, l.date as date, d.rights as rights, d.model as model, d.title as title, dnnt_labels as dnnt_labels  from statistics_access_log l JOIN statistic_access_log_detail d USING (record_id) where d.model::text = 'collection'::text and l.stat_action='READ';
