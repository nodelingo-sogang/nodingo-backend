package nodingo.core.notification.service.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.google.firebase.messaging.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @InjectMocks
    private FcmService fcmService;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    private MockedStatic<FirebaseMessaging> mockedFirebaseMessaging;

    @BeforeEach
    void setUp() {
        mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class);
        mockedFirebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
    }

    @AfterEach
    void tearDown() {
        mockedFirebaseMessaging.close();
    }

    @Test
    @DisplayName("단건 푸시 알림 전송 - 성공")
    void sendTestMessage_Success() throws Exception {
        // given
        String targetToken = "valid-token";
        given(firebaseMessaging.send(any(Message.class))).willReturn("projects/test/messages/1234");

        // when
        fcmService.sendTestMessage(targetToken);

        // then
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("단건 푸시 알림 전송 - 실패 (예외 발생 시 로그 출력하고 종료)")
    void sendTestMessage_Failure() throws Exception {

        // given
        String targetToken = "invalid-token";
        given(firebaseMessaging.send(any(Message.class))).willThrow(new RuntimeException("Firebase Exception"));

        // when
        fcmService.sendTestMessage(targetToken);

        // then
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("다건 푸시 알림 전송 - 리스트가 비어있으면 전송하지 않음")
    void sendMessages_EmptyList() throws Exception {
        // when
        fcmService.sendMessages(Collections.emptyList());

        // then
        verify(firebaseMessaging, never()).sendEach(anyList());
    }

    @Test
    @DisplayName("다건 푸시 알림 전송 - 전체 성공")
    void sendMessages_AllSuccess() throws Exception {

        // given
        Message msg1 = Message.builder().setTopic("topic1").build();
        Message msg2 = Message.builder().setTopic("topic2").build();
        List<Message> messages = List.of(msg1, msg2);

        BatchResponse batchResponse = mock(BatchResponse.class);
        given(batchResponse.getFailureCount()).willReturn(0);
        given(batchResponse.getSuccessCount()).willReturn(2);

        given(firebaseMessaging.sendEach(messages)).willReturn(batchResponse);

        // when
        fcmService.sendMessages(messages);

        // then
        verify(firebaseMessaging, times(1)).sendEach(messages);
    }

    @Test
    @DisplayName("다건 푸시 알림 전송 - 일부 실패 (로그 확인용)")
    void sendMessages_PartialFailure() throws Exception {
        // given
        Message msg1 = Message.builder().setTopic("topic1").build();
        List<Message> messages = List.of(msg1);

        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse sendResponse = mock(SendResponse.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);

        given(batchResponse.getFailureCount()).willReturn(1);
        given(batchResponse.getSuccessCount()).willReturn(0);
        given(batchResponse.getResponses()).willReturn(List.of(sendResponse));

        given(sendResponse.isSuccessful()).willReturn(false);
        given(sendResponse.getException()).willReturn(exception);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.INVALID_ARGUMENT);
        given(exception.getMessage()).willReturn("Invalid token format");

        given(firebaseMessaging.sendEach(messages)).willReturn(batchResponse);

        // when
        fcmService.sendMessages(messages);

        // then
        verify(firebaseMessaging, times(1)).sendEach(messages);
    }
}