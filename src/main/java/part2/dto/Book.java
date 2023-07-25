package part2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Book {

    private String isbn;
    private String bookName;
    private String author;
}
