package ru.nsu.ccfit.zuev.osu.online;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FormDataPostBuilder extends PostBuilder {
    private final MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

    public void addParam(final String key, final String filename, final RequestBody value) {
        multipartBodyBuilder.addFormDataPart(key, filename, value);
    }

    @Override
    protected void addParamInternal(final String key, final String value) {
        multipartBodyBuilder.addFormDataPart(key, value);
    }

    @Override
    protected RequestBody getRequestBody() {
        return multipartBodyBuilder.build();
    }
}
