package nodingo.core.user.service;

import lombok.RequiredArgsConstructor;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.user.domain.InterestLevel;
import nodingo.core.user.domain.UserPersona;
import nodingo.core.user.dto.result.KeywordListResult;
import nodingo.core.user.dto.result.KeywordResult;
import nodingo.core.user.dto.result.PersonaListResult;
import nodingo.core.user.dto.result.PersonaResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingQueryService {

    private final KeywordRepository keywordRepository;

    public PersonaListResult getPersonas(Long userId) {
        List<PersonaResult> results = Arrays.stream(UserPersona.values())
                .map(PersonaResult::from)
                .toList();

        return PersonaListResult.from(results);
    }

    public KeywordListResult getMacroKeywords(Long userId, UserPersona persona) {
        List<KeywordResult> results = keywordRepository.findAllByPersonaAndLevel(persona, InterestLevel.MACRO).stream()
                .map(KeywordResult::from)
                .toList();

        return KeywordListResult.from(results);
    }

    public KeywordListResult getSpecificKeywords(Long userId, Long macroId) {
        List<KeywordResult> results = keywordRepository.findAllByParentIdAndLevel(macroId, InterestLevel.SPECIFIC).stream()
                .map(KeywordResult::from)
                .toList();

        return KeywordListResult.from(results);
    }
}