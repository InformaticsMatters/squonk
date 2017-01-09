ALTER TABLE users.service_descriptors ADD COLUMN java_class TEXT;

UPDATE users.service_descriptors SET java_class = 'org.squonk.core.HttpServiceDescriptor';

ALTER TABLE users.service_descriptors ALTER COLUMN java_class SET NOT NULL;