package nodingo.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.user.dto.result.PersonaResult;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponse {
    private String name;
    private String description;

    public static PersonaResponse from(PersonaResult result) {
        return new PersonaResponse(result.getName(), result.getDescription());
    }
}