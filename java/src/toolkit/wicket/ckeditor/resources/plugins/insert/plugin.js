(function(){
    var a= {
            exec:function(editor) {
                    alert('Here');
            }
    },

    b='insert';
    CKEDITOR.plugins.add(b,{
        init:function(editor){
            editor.addCommand(b,a);
            editor.ui.addButton('insert',{
            label:'Insert resource',
            command:b
        });
    }
    });
})();