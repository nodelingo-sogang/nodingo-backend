package nodingo.core.user.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PersonaListResult {
    private List<PersonaResult> contents;

    public static PersonaListResult from(List<PersonaResult> contents) {
        return new PersonaListResult(contents);
    }
}