-- issn column --
alter table statistic_access_log_detail add column issn text[];

-- isbn column --
alter table statistic_access_log_detail add column isbn text[];

-- ccnb column --
alter table statistic_access_log_detail add column ccnb text[];
