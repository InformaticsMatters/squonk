Manual migrations

1. For moving MolecueObject and BasicObject to the org.squonk.types package:

alter table users.nb_variable add column val_text_old text;
update users.nb_variable set val_text_old = val_text;
update users.nb_variable set val_text = replace(val_text, 'com.im.lac.', 'org.squonk.');