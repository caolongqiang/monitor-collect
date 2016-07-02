CREATE TABLE `filter` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `app` varchar(128) NOT NULL,
  `env` varchar(128) NOT NULL,
  `status` smallint(3) NOT NULL default 0,
  `last_update` timestamp default now() NOT NULL,
  `operator` varchar(128),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_department_group_status`(`app`,`env`)
  )  ENGINE=InnoDB DEFAULT CHARSET=utf8;

  CREATE INDEX status_idx ON filter (status);

  insert into filter(app, env, last_update, operator) values ('a', 'b', now(), 'zzb');