-- Added Schema for Department Table
-- Added by Naveen on 14 November 2019

USE user_service;

CREATE TABLE IF NOT EXISTS `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `department_name` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_departments_uuid` (`uuid`),
  UNIQUE KEY `UK_departments_name` (`department_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


ALTER TABLE `user_service_preprod`.`user_profile` 
ADD COLUMN `arrival_date` DATE NULL DEFAULT NULL AFTER `nationality`,
ADD COLUMN `next_destination` VARCHAR(100) NULL DEFAULT NULL AFTER `arrival_date`;