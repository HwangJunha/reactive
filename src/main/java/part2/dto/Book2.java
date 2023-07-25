package part2.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Book2 {

    private String bookName;
    private String authorName;
    private String penName;
    private int price;
    private int stockQuantity;
}

