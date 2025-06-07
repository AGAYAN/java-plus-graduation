package ru.practicum.deserializer;

import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class UserAvroDeserializer implements Deserializer<UserActionAvro> {
    @Override
    public UserActionAvro deserialize(String s, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            SpecificDatumReader<UserActionAvro> reader = new SpecificDatumReader<>(UserActionAvro.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
