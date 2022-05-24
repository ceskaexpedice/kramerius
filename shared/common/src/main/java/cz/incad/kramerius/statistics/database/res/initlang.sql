-- not null language
create MATERIALIZED view _statistic_access_log_detail_lang_not_null as  select * from statistic_access_log_detail where lang is not null;

-- _lang view
create MATERIALIZED view _lang as  select l.date as date, d.lang as lang, d.rights as rights, l.dnnt_labels as dnnt_labels   from statistics_access_log l JOIN _statistic_access_log_detail_lang_not_null d USING (record_id) where l.stat_action='READ';
