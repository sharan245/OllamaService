package com.ollamaService.helper;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 16/10/25
 */
public class FluxRecorder<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Subscribes to a Flux and records emitted items.
     * @param flux the Flux to record
     * @return a Mono that completes with the list of all recorded items
     */
    public Flux<T> record(Flux<T> flux) {
        return flux.doOnNext(items::add);
    }

    public List<T> getRecordedTillNow() {
        return new ArrayList<>(items);
    }

    public String getRecordedTillNowAsString(Function<T, String> mapper) {
        return items
                .stream()
                .map(mapper)
                .reduce("", String::concat);
    }
}
