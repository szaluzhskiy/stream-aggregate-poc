package ru.poc.streams.aggregate.streamaggregatepoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;

@SpringBootApplication
public class StreamAggregatePocApplication {
    @Autowired
    private InteractiveQueryService interactiveQueryService;

    public static void main(String[] args) {
        SpringApplication.run(StreamAggregatePocApplication.class, args);
    }

    public static class KafkaStreamsAggregateSampleApplication {
        @Bean
        public Consumer<KStream<String, DomainEvent>> aggregate() {

            ObjectMapper mapper = new ObjectMapper();
            Serde<DomainEvent> domainEventSerde = new JsonSerde<>( DomainEvent.class, mapper );

            return input -> input
                    .groupBy(
                            (s, domainEvent) -> domainEvent.boardUuid,
                            Grouped.with(null, domainEventSerde))
                    .aggregate(
                            String::new,
                            (s, domainEvent, board) -> board.concat(domainEvent.eventType),
                            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("test-events-snapshots")
                                    .withKeySerde(Serdes.String()).
                                    withValueSerde(Serdes.String())
                    );
        }
    }

    @RestController
    public class FooController {
        @RequestMapping("/events")
        public String events() {
            final ReadOnlyKeyValueStore<String, String> topFiveStore =
                    interactiveQueryService.getQueryableStore("test-events-snapshots", QueryableStoreTypes.<String, String>keyValueStore());
            return topFiveStore.get("12345");
        }
    }
}
