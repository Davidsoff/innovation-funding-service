UPDATE form_validator SET
  clazz_name = 'org.innovateuk.ifs.validator.SingedLongIntegerValidator',
  title = 'SignedLongIntegerValidator'
WHERE
  clazz_name = 'org.innovateuk.ifs.validator.IntegerValidator';

UPDATE form_validator SET
  clazz_name = 'org.innovateuk.ifs.validator.NonNegativeLongIntegerValidator',
  title = 'NonNegativeLongIntegerValidator'
WHERE
  clazz_name = 'org.innovateuk.ifs.validator.NonNegativeIntegerValidator';
