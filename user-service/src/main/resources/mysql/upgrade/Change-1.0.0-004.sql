CREATE TABLE `user_service`.`blacklist_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `mobile` varchar(15) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_blacklist_user_uuid` (`uuid`),
  KEY `Key_blacklist_user_mobile` (`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
