var uploader = new ss.SimpleUpload({
    button: 'btnUpload', // HTML element used as upload button
    url: $.kbase.ctx + '/driver', // URL of server-side upload handler
    name: 'file', // Parameter name of the uploaded file
    responseType: 'json',
    onSubmit: function (filename, extension) {
        this.setFileSizeBox($('#sizeBox')[0]); // designate this element as file size container
        this.setProgressBar($('#progress')[0]); // designate as progress bar
    },
    onComplete: function (filename, response, uploadBtn, fileSize) {
        if (!response) {
            alert(filename + ' 上传失败');
            return false;
        }
        alert(filename + ' 上传成功');

        $('#textPanel').addClass('panel').text(response.text);
        $('#summaryPanel').addClass('panel').text(response.summary);
        $('#keywordPanel').addClass('panel').text(response.keyword);

        if (response.isVideo) {
            $('img').hide();
            $('video').attr('controls', 'controls').attr('src', $.kbase.ctx + '/ocr/loadVideo?path=' + response.img).show();
        } else {
            $('video').hide();
            $('img').attr('src', $.kbase.ctx + '/ocr/loadImg?path=' + response.img).show();
        }
    }
});