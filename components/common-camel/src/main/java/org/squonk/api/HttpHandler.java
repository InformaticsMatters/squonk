/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.api;

import org.squonk.http.RequestResponseExecutor;

import java.io.IOException;

/**
 * Created by timbo on 23/03/2016.
 */
public interface HttpHandler<T> extends Handler<T> {

    void prepareRequest(T obj, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException;
    void writeResponse(T obj, RequestResponseExecutor executor, boolean gzip) throws IOException;
    T readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException;

    default void handleGzipHeaders(RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) {
        if (gzipRequest) {
            executor.prepareRequestHeader("Content-Encoding", "gzip");
        }
        if (gzipResponse) {
            executor.prepareRequestHeader("Accept-Encoding", "gzip");
        }
    }

}
