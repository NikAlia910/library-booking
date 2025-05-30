package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ResourceTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Resource getResourceSample1() {
        return new Resource().id(1L).title("title1").author("author1").keywords("keywords1");
    }

    public static Resource getResourceSample2() {
        return new Resource().id(2L).title("title2").author("author2").keywords("keywords2");
    }

    public static Resource getResourceRandomSampleGenerator() {
        return new Resource()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .author(UUID.randomUUID().toString())
            .keywords(UUID.randomUUID().toString());
    }
}
