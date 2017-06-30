CREATE TABLE `activity_state` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `activity_type` ENUM(
    'APPLICATION_ASSESSMENT',
    'PROJECT_SETUP_COMPANIES_HOUSE_DETAILS',
    'PROJECT_SETUP_PROJECT_DETAILS',
    'PROJECT_SETUP_MONITORING_OFFICER_ASSIGNMENT',
    'PROJECT_SETUP_BANK_DETAILS',
    'PROJECT_SETUP_FINANCE_CHECKS',
    'PROJECT_SETUP_SPEND_PROFILE',
    'PROJECT_SETUP_GRANT_OFFER_LETTER') NOT NULL,
  `state` ENUM(
    'OPEN',
    'PENDING',
    'READY_TO_SUBMIT',
    'SUBMITTED',
    'ACCEPTED',
    'REJECTED',
    'VERIFIED',
    'NOT_VERIFIED',
    'ASSIGNED',
    'NOT_ASSIGNED') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;