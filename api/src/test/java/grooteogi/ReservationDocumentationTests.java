package grooteogi;

import static grooteogi.ApiDocumentUtils.getDocumentRequest;
import static grooteogi.ApiDocumentUtils.getDocumentResponse;
import static grooteogi.ApiDocumentUtils.getPost;
import static grooteogi.ApiDocumentUtils.getPostHashtags;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import grooteogi.config.UserInterceptor;
import grooteogi.controller.ReservationController;
import grooteogi.domain.Reservation;
import grooteogi.domain.Schedule;
import grooteogi.domain.User;
import grooteogi.dto.ReservationDto;
import grooteogi.mapper.ReservationMapper;
import grooteogi.service.ReservationService;
import grooteogi.utils.JwtProvider;
import grooteogi.utils.Session;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(ReservationController.class)
public class ReservationDocumentationTests {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private ReservationService reservationService;
  @MockBean
  private UserInterceptor userInterceptor;
  @MockBean
  private JwtProvider jwtProvider;
  @MockBean
  private PasswordEncoder passwordEncoder;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private Session session;
  @MockBean
  private Authentication authentication;
  @MockBean
  private SecurityContext securityContext;

  Schedule schedule;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(
        documentationConfiguration(restDocumentation).operationPreprocessors()
            .withRequestDefaults(prettyPrint()).withResponseDefaults(prettyPrint())).build();

    // domain for test
    schedule = ApiDocumentUtils.getSchedule();
    schedule.setPost(ApiDocumentUtils.getPost());

  }

  @Test
  @DisplayName("예약조회")
  public void getReservation() throws Exception {
    // given
    given(reservationService.getReservation(1)).willReturn(getResponse());

    ResultActions result = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/reservation/{reservationId}", 1)
            .characterEncoding("utf-8")
            .accept(MediaType.APPLICATION_JSON));
    result.andExpect(status().isOk())
        .andDo(print()).andDo(
            document("get-reservation", getDocumentRequest(), getDocumentResponse(),
                responseFields(fieldWithPath("status").description("결과 코드"),
                    fieldWithPath("data.reservationId").description("아이디"),
                    fieldWithPath("data.imageUrl").type(JsonFieldType.STRING)
                        .description("이미지 주소"),
                    fieldWithPath("data.title").type(JsonFieldType.STRING)
                        .description("포스트 제목"),
                    fieldWithPath("data.hashtags").type(JsonFieldType.ARRAY)
                        .description("포스트 해시태그"),
                    fieldWithPath("data.date").type(JsonFieldType.STRING)
                        .description("약속 날짜"),
                    fieldWithPath("data.startTime").type(JsonFieldType.STRING)
                        .description("약속 시작 시간"),
                    fieldWithPath("data.endTime").type(JsonFieldType.STRING)
                        .description("약속 끝 시간"),
                    fieldWithPath("data.place").type(JsonFieldType.STRING)
                        .description("약속 장소")))
        );

    verify(reservationService).getReservation(1);
  }

  @Test
  @DisplayName("호스트유저 예약조회")
  public void getHostReservation() throws Exception {
    // given
    List<ReservationDto.Response> responses = new ArrayList<>();
    ReservationDto.Response response = getResponse();
    responses.add(response);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(session);

    // when
    given(reservationService.getHostReservation(anyInt())).willReturn(responses);

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/reservation/host").characterEncoding("uft-8")
            .accept(MediaType.APPLICATION_JSON)
    );

    // then
    resultActions.andExpect(status().isOk()).andDo(print()).andDo(
        document("get-host-reservation", getDocumentRequest(), getDocumentResponse(),
            responseFields(fieldWithPath("status").description("결과 코드"),
                fieldWithPath("data.[].reservationId").description("아이디"),
                fieldWithPath("data.[].imageUrl").type(JsonFieldType.STRING)
                    .description("이미지 주소"),
                fieldWithPath("data.[].title").type(JsonFieldType.STRING)
                    .description("포스트 제목"),
                fieldWithPath("data.[].hashtags").type(JsonFieldType.ARRAY)
                    .description("포스트 해시태그"),
                fieldWithPath("data.[].date").type(JsonFieldType.STRING)
                    .description("약속 날짜"),
                fieldWithPath("data.[].startTime").type(JsonFieldType.STRING)
                    .description("약속 시작 시간"),
                fieldWithPath("data.[].endTime").type(JsonFieldType.STRING)
                    .description("약속 끝 시간"),
                fieldWithPath("data.[].place").type(JsonFieldType.STRING)
                    .description("약속 장소")))
    );
    verify(reservationService).getHostReservation(anyInt());

  }

  @Test
  @DisplayName("참가자유저 예약조회")
  public void getUserReservation() throws Exception {
    // given
    List<ReservationDto.Response> responses = new ArrayList<>();
    ReservationDto.Response response = getResponse();
    responses.add(response);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(session);

    // when
    given(reservationService.getUserReservation(anyInt())).willReturn(responses);

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/reservation/apply").characterEncoding("uft-8")
            .accept(MediaType.APPLICATION_JSON)
    );

    // then
    resultActions.andExpect(status().isOk()).andDo(print()).andDo(
        document("get-participate-reservation", getDocumentRequest(), getDocumentResponse(),
            responseFields(fieldWithPath("status").description("결과 코드"),
                fieldWithPath("data.[].reservationId").description("아이디"),
                fieldWithPath("data.[].imageUrl").type(JsonFieldType.STRING)
                    .description("이미지 주소"),
                fieldWithPath("data.[].title").type(JsonFieldType.STRING)
                    .description("포스트 제목"),
                fieldWithPath("data.[].hashtags").type(JsonFieldType.ARRAY)
                    .description("포스트 해시태그"),
                fieldWithPath("data.[].date").type(JsonFieldType.STRING)
                    .description("약속 날짜"),
                fieldWithPath("data.[].startTime").type(JsonFieldType.STRING)
                    .description("약속 시작 시간"),
                fieldWithPath("data.[].endTime").type(JsonFieldType.STRING)
                    .description("약속 끝 시간"),
                fieldWithPath("data.[].place").type(JsonFieldType.STRING)
                    .description("약속 장소")))
    );

    verify(reservationService).getUserReservation(anyInt());
  }

  @Test
  @DisplayName("예약생성")
  public void createReservation() throws Exception {
    // given
    ReservationDto.Request request = getRequest();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(session);

    given(reservationService.createReservation(eq(request), anyInt())).willReturn(getResponse());

    String json = objectMapper.writeValueAsString(request);

    // when
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/reservation").characterEncoding("utf-8")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    // then
    resultActions.andExpect(status().isOk())
        .andDo(print()).andDo(
            document("create-reservation", getDocumentRequest(), getDocumentResponse(),
                requestFields(fieldWithPath("scheduleId").description("스케쥴 아이디"),
                    fieldWithPath("message").description("메세지")),
                responseFields(fieldWithPath("status").description("결과 코드"),
                    fieldWithPath("data.reservationId").description("아이디"),
                    fieldWithPath("data.imageUrl").type(JsonFieldType.STRING)
                        .description("이미지 주소"),
                    fieldWithPath("data.title").type(JsonFieldType.STRING)
                        .description("포스트 제목"),
                    fieldWithPath("data.hashtags").type(JsonFieldType.ARRAY)
                        .description("포스트 해시태그"),
                    fieldWithPath("data.date").type(JsonFieldType.STRING)
                        .description("약속 날짜"),
                    fieldWithPath("data.startTime").type(JsonFieldType.STRING)
                        .description("약속 시작 시간"),
                    fieldWithPath("data.endTime").type(JsonFieldType.STRING)
                        .description("약속 끝 시간"),
                    fieldWithPath("data.place").type(JsonFieldType.STRING)
                        .description("약속 장소")))
        );

  }

  @Test
  @DisplayName("예약삭제")
  public void deleteReservation() throws Exception {
    //given
    int reservationId = anyInt();

    //when
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .delete("/reservation/{reservationId}", reservationId)
            .characterEncoding("utf-8")
            .accept(MediaType.APPLICATION_JSON)
    );

    //then
    resultActions.andExpect(status().isOk())
        .andDo(print()).andDo(
            document("delete-reservation", getDocumentRequest(), getDocumentResponse(),
                responseFields(fieldWithPath("status").description("결과 코드"),
                    fieldWithPath("message").description("응답 메세지")))
        );
    verify(reservationService).deleteReservation(reservationId);
  }

  private ReservationDto.Request getRequest() {
    return ReservationDto.Request
        .builder()
        .message("msg")
        .scheduleId(1)
        .build();
  }

  private ReservationDto.Response getResponse() {
    List<String> stringTags = new ArrayList<>();
    getPostHashtags().forEach(postHashtag -> stringTags.add(postHashtag.getHashTag().getTag()));
    ReservationDto.Response response = ReservationMapper
        .INSTANCE.toResponseDto(getEntity(), getPost(), schedule);
    response.setHashtags(stringTags);
    return response;
  }

  private Reservation getEntity() {
    return Reservation.builder()
        .schedule(schedule)
        .hostUser(User.builder()
            .build())
        .participateUser(User.builder()
            .build())
        .message("msg")
        .build();
  }
}
