package nodingo.core.user.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nodingo.core.user.domain.UserPersona;

@Getter
@AllArgsConstructor
public class PersonaResult {
    private String name;
    private String description;

    public static PersonaResult from(UserPersona persona) {
        return new PersonaResult(persona.name(), persona.getDescription());
    }
}