package com.around.reactive.mapper.v2;

import com.around.reactive.dto.Book;
import com.around.reactive.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import reactor.core.publisher.Mono;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        implementationName = "bookMapperV2")
public interface BookMapper {
    Book bookPostToBook(BookDto.Post requestBody);
    Book bookPatchToBook(BookDto.Patch requestBody);
    BookDto.Response bookToResponse(Book book);
}
