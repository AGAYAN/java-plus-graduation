package ru.practicum.deserializer;

import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.IOException;

public class UserAvroDeserializer implements Deserializer<UserActionAvro> {

    private final SpecificDatumReader<UserActionAvro> reader;

    public UserAvroDeserializer() {
        this.reader = new SpecificDatumReader<>(UserActionAvro.class);
    }

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Error deserializing Avro message", e);
        }
    }
}
