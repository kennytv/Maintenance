package eu.kennytv.maintenance.core.proxy.redis;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.ToByteBufEncoder;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RedisSerializer implements RedisCodec<String, Object>, ToByteBufEncoder<String, Object> {

    private static final Charset charset = StandardCharsets.UTF_8;

    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
        return charset.decode(byteBuffer).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer byteBuffer) {
        byte[] serializedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(serializedBytes);
        try(ByteArrayInputStream ais = new ByteArrayInputStream(serializedBytes); ObjectInputStream is = new ObjectInputStream(ais)){
            return is.readObject();
        }catch (Exception ex){
            ex.printStackTrace();
            ex.fillInStackTrace();
            return null;
        }
    }

    @Override
    public ByteBuffer encodeKey(String s) {
        return charset.encode(s);
    }

    @Override
    public ByteBuffer encodeValue(Object object) {
        try(ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bytes)) {
            os.writeObject(object);
            return ByteBuffer.wrap(bytes.toByteArray());
        }catch (Exception ex){
            ex.printStackTrace();
            return ByteBuffer.wrap(new byte[0]);
        }
    }

    @Override
    public void encodeKey(String s, ByteBuf byteBuf) {
        byteBuf.writeBytes(s.getBytes(charset));
    }

    @Override
    public void encodeValue(Object object, ByteBuf byteBuf) {
        byte[] serializedBytes;
        try(ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bytes)) {
            os.writeObject(object);
            serializedBytes = bytes.toByteArray();
        }catch (Exception ex){
            ex.printStackTrace();
            serializedBytes = new byte[0];
        }
        byteBuf.writeBytes(serializedBytes);
    }

    @Override
    public int estimateSize(Object o) {
        return encodeValue(o).remaining();
    }
}