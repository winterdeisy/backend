package grooteogi.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class ReviewDto {

  @Data
  @Builder
  public static class Request {

    @NotNull
    private Integer postId;

    @NotNull
    private Integer reservationId;

    @NotNull
    private String text;

    @NotNull
    private Long score;
  }

  @Data
  @Builder
  public static class Response {

  }
}