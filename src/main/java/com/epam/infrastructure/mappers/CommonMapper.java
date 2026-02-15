package com.epam.infrastructure.mappers;

import org.mapstruct.Named;
import java.util.UUID;

public class CommonMapper {
    @Named("uuidToString")
    public static String uuidToString(UUID id) {
        return id != null ? id.toString() : null;
    }

    @Named("stringToUuid")
    public static UUID stringToUuid(String id) {
        return id != null ? UUID.fromString(id) : null;
    }

}
