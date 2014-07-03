package toolkit.wicket.ckeditor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CKEditorDefaultBasePathProvider implements CKEditorBasePathProvider {

    @Override
    public String getCKEditorBasePath() {
        return "window.CKEDITOR_BASEPATH = '/wicket/resource/toolkit.wicket.ckeditor.CKEditor/resources/';";
    }
}
