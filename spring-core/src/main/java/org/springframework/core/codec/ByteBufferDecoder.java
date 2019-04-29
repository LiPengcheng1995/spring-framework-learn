/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.codec;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Decoder for {@link ByteBuffer}s.
 *
 * @author Sebastien Deleuze
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public class ByteBufferDecoder extends AbstractDataBufferDecoder<ByteBuffer> {

	public ByteBufferDecoder() {
		super(MimeTypeUtils.ALL);
	}


	@Override
	public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
		Class<?> clazz = elementType.getRawClass();
		return (clazz != null && ByteBuffer.class.isAssignableFrom(clazz) && super.canDecode(elementType, mimeType));
	}

	@Override
	protected ByteBuffer decodeDataBuffer(DataBuffer dataBuffer, ResolvableType elementType,
										  @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

		ByteBuffer copy = ByteBuffer.allocate(dataBuffer.readableByteCount());
		copy.put(dataBuffer.asByteBuffer());
		copy.flip();
		DataBufferUtils.release(dataBuffer);
		return copy;
	}

}
