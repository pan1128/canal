CREATE TABLE IF NOT EXISTS `meta_snapshot` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `destination` varchar(128) DEFAULT NULL COMMENT '通道名称',
  `binlog_file` varchar(64) DEFAULT NULL COMMENT 'binlog文件名',
  `binlog_offest` bigint(20) DEFAULT NULL COMMENT 'binlog偏移量',
  `binlog_master_id` varchar(64) DEFAULT NULL COMMENT 'binlog节点id',
  `binlog_timestamp` bigint(20) DEFAULT NULL COMMENT 'binlog应用的时间戳',
  `data` longtext DEFAULT NULL COMMENT '表结构数据',
  `extra` text DEFAULT NULL COMMENT '额外的扩展信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY meta_snapshot_binlog_file_offest(`destination`,`binlog_master_id`,`binlog_file`,`binlog_offest`),
  KEY `meta_snapshot_destination` (`destination`),
  KEY `meta_snapshot_destination_timestamp` (`destination`,`binlog_timestamp`),
  KEY `meta_snapshot_gmt_modified` (`gmt_modified`)
);