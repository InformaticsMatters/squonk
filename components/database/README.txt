Manual migrations

1. For moving MoleculeObject and BasicObject to the org.squonk.types package:

alter table users.nb_variable add column val_text_old text;
update users.nb_variable set val_text_old = val_text;
update users.nb_variable set val_text = replace(val_text, 'com.im.lac.', 'org.squonk.');



2. fixes for jobstatus

alter table users.jobstatus add column definition_old jsonb;
update users.jobstatus set definition_old = definition;
update users.jobstatus set definition = replace(definition::text, 'com.im.lac.job.jobdef.', 'org.squonk.jobdef.')::jsonb;