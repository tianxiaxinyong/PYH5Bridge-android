package com.pycredit.h5sdk.utils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 带进度的请求体
 *
 * @author huangx
 * @date 2018/3/9
 */

public class ProgressRequestBody extends RequestBody {

    private final RequestBody origin;

    private BufferedSink bufferedSinkWrap;

    public ProgressRequestBody(RequestBody origin) {
        this.origin = origin;
    }

    @Override
    public MediaType contentType() {
        return origin.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return origin.contentLength();
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        if (bufferedSinkWrap == null) {
            bufferedSinkWrap = Okio.buffer(sink(bufferedSink));
        }
        origin.writeTo(bufferedSinkWrap);
        bufferedSinkWrap.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {

            private long current;
            private long total;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (total == 0) {
                    total = contentLength();
                }
                current += byteCount;
                onProgress(current, total, total == current);
            }
        };
    }

    protected void onProgress(long current, long total, boolean done) {

    }
}
