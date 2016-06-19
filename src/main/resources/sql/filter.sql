CREATE TABLE `filter` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `department` varchar(128) NOT NULL,
  `groupname` varchar(128) NOT NULL,
  `status` smallint(6) NOT NULL default 0,
  `last_update` timestamp(6) NOT NULL,
  `operator` varchar(128),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_department_group_status`(`department`,`groupname`, `status`)
  ) ;

  insert into filter(groupname, department, last_update, operator) values ('a', 'b', now(), 'zzb');