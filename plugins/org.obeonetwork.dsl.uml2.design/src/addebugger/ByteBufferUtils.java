package addebugger;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferUtils {

	public static void addString(ByteBuffer byteBuffer, String string) {
		final char[] chars = string.toCharArray();
		byteBuffer.putInt(chars.length);
		for (final char character : chars) {
			byteBuffer.putChar(character);
		}
	}

	public static String readString(ByteBuffer byteBuffer) {
		try {
			final char[] chars = new char[byteBuffer.getInt()];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = byteBuffer.getChar();
			}
			return new String(chars);
		} catch (final BufferUnderflowException e) {
			return "";
		}
	}
}
