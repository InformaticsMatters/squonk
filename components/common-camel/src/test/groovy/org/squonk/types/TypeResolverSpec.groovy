package org.squonk.types

import com.im.lac.types.MoleculeObject
import org.squonk.api.HttpHandler
import org.squonk.api.MimeTypeResolver
import org.squonk.api.VariableHandler
import org.squonk.dataset.Dataset
import spock.lang.Specification

/**
 * Created by timbo on 23/03/2016.
 */
class TypeResolverSpec extends Specification {

    void "resolve types"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def primary = resolver.resolvePrimaryType(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
        def generic  = resolver.resolveGenericType(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)

        then:
        primary == Dataset.class
        generic == MoleculeObject.class
    }

    void "resolve http handler"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def h = resolver.createHttpHandler(Dataset.class)

        then:
        h != null
        h.class == DatasetHandler.class
        h instanceof HttpHandler
    }

    void "resolve variable handler"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def h = resolver.createVariableHandler(Dataset.class)

        then:
        h != null
        h.class == DatasetHandler.class
        h instanceof VariableHandler
    }

}
