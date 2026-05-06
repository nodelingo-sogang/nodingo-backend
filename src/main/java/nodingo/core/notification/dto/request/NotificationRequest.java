package nodingo.core.notification.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequest {

    @Min(1) @Max(24)
    @JsonProperty("notifyHour")
    private int notifyHour;

    @NotBlank
    @JsonProperty("fcmToken")
    private String fcmToken;
}