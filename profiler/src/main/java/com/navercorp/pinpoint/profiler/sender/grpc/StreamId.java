/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class StreamId {
    private static final AtomicLong idAllocator = new AtomicLong();

    private final String name;
    private final long id;

    public static StreamId newStreamId(String name) {
        return new StreamId(name, idAllocator.incrementAndGet());
    }

    private StreamId(String name, long id) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.id = id;
    }

    @Override
    public String toString() {
        return name + "-" + id;
    }
}
