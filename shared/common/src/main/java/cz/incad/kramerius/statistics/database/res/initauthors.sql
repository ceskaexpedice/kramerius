-- _authors view
create MATERIALIZED view _authors as select author_name as author_name, l.date as date,d.rights as rights, l.dnnt_labels as dnnt_labels from statistics_access_log l join statistic_access_log_detail_authors ad using(record_id)  join statistic_access_log_detail d using(record_id);
