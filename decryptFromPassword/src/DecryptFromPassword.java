import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Properties;

// https://commons.apache.org/proper/commons-crypto/xref-test/org/apache/commons/crypto/examples/CipherByteBufferExample.html

public class DecryptFromPassword {

    // From https://www.javatips.net/api/prive-android-master/src/info/guardianproject/otr/AES_256_CBC.java
    public static  byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt,
                                           byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (;;) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }

/**    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }
*/
/**    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }
*/
    /**
     * Converts ByteBuffer to String
     *
     * @param buffer input byte buffer
     * @return the converted string
     */
    private static String asString(ByteBuffer buffer) {
        final ByteBuffer copy = buffer.duplicate();
        final byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        final SecretKeySpec key;
        final IvParameterSpec iv;

        //Creates a CryptoCipher instance with the transformation and properties.
        final String transform = "AES/CBC/PKCS5Padding";
//        ByteBuffer outBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(args[1]));
        ByteBuffer outBuffer = ByteBuffer.wrap(
                java.util.Base64.getDecoder().decode(URLDecoder.decode(args[1], "UTF-8")));

        final int bufferSize = 1024;


        Properties properties = new Properties();
        try (CryptoCipher decipher = Utils.getCipherInstance(transform, properties)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = EVP_BytesToKey(32,
                    16, md5, null, args[0].getBytes(), 1);
            key = new SecretKeySpec(keyAndIV[0], "AES");
            iv = new IvParameterSpec(keyAndIV[1]);

            decipher.init(Cipher.DECRYPT_MODE, key, iv);
            ByteBuffer decoded = ByteBuffer.allocateDirect(bufferSize);
            decipher.update(outBuffer, decoded);
            decipher.doFinal(outBuffer, decoded);
            decoded.flip(); // ready for use
            System.out.println("decoded=" + asString(decoded));
        }
    }
}