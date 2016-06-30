CREATE TABLE `filter` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `app` varchar(128) NOT NULL,
  `env` varchar(128) NOT NULL,
  `status` smallint(6) NOT NULL default 0,
  `last_update` timestamp(6) NOT NULL,
  `operator` varchar(128),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_department_group_status`(`app`,`env`, `status`)
  )  ENGINE=InnoDB DEFAULT CHARSET=utf8;;

  insert into filter(app, env, last_update, operator) values ('a', 'b', now(), 'zzb');