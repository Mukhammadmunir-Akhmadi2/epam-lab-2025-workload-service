package com.epam.infrastructure.mappers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommonMapperTest {

    @Test
    void uuidToString_null_returnsNull() {
        assertNull(CommonMapper.uuidToString(null));
    }

    @Test
    void uuidToString_validUuid_returnsString() {
        UUID id = UUID.randomUUID();
        assertEquals(id.toString(), CommonMapper.uuidToString(id));
    }

    @Test
    void stringToUuid_null_returnsNull() {
        assertNull(CommonMapper.stringToUuid(null));
    }

    @Test
    void stringToUuid_validString_returnsUuid() {
        UUID id = UUID.randomUUID();
        assertEquals(id, CommonMapper.stringToUuid(id.toString()));
    }

    @Test
    void stringToUuid_invalidString_throws() {
        assertThrows(IllegalArgumentException.class, () -> CommonMapper.stringToUuid("not-a-uuid"));
    }
}
