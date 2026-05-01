package nodingo.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.user.dto.result.PersonaListResult;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class PersonaListResponse {
    private List<PersonaResponse> contents;

    public static PersonaListResponse from(PersonaListResult result) {
        List<PersonaResponse> responses = result.getContents().stream()
                .map(PersonaResponse::from)
                .toList();
        return new PersonaListResponse(responses);
    }
}