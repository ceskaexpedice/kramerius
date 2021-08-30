CREATE SEQUENCE statistic_access_log_detail_publishers_ID_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;
create table statistic_access_log_detail_publishers(
   PUBLISHER_ID int not null,
   PUBLISHER_NAME TEXT,
   DETAIL_ID INT NOT NULL REFERENCES statistic_access_log_detail(detail_id),
   RECORD_ID INT NOT NULL REFERENCES statistics_access_log(record_ID),
   PRIMARY KEY(PUBLISHER_ID)
);




