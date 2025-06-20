package ru.practicum.serializer;

import lombok.RequiredArgsConstructor;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
public class CollectorSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder binaryEncoder;

    @Override
    public byte[] serialize(String s, SpecificRecordBase specificRecordBase) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] result = null;
            binaryEncoder = encoderFactory.binaryEncoder(outputStream, binaryEncoder);
            if(specificRecordBase != null) {
                DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(specificRecordBase.getSchema());
                writer.write(specificRecordBase, binaryEncoder);
                binaryEncoder.flush();
                result = outputStream.toByteArray();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
