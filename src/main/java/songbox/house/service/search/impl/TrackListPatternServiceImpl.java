package songbox.house.service.search.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.TrackListPattern;
import songbox.house.exception.ParsingException;
import songbox.house.repository.TrackListPatternRepository;
import songbox.house.service.search.TrackListPatternService;

import static java.text.MessageFormat.format;
import static songbox.house.util.parser.TrackListParser.ARTISTS_LABEL;
import static songbox.house.util.parser.TrackListParser.TITLE_LABEL;
import static songbox.house.util.parser.TrackListParser.canParse;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Transactional
@Slf4j
public class TrackListPatternServiceImpl implements TrackListPatternService {

    TrackListPatternRepository repository;

    @Override
    //TODO create init patterns
    public TrackListPattern create(final String regex, final String example) {

        if (canParse(regex, example)) {
            final TrackListPattern pattern = createPattern(regex, example);
            return repository.save(pattern);
        } else {
            throw new ParsingException(format("Can't parse \"{0}\" by regex \"{1}\". " +
                    "Pattern can't be compiled or don't have labels {2} or {3}", example, regex, ARTISTS_LABEL, TITLE_LABEL));
        }
    }

    private TrackListPattern createPattern(final String regex, final String example) {
        final TrackListPattern pattern = new TrackListPattern();
        pattern.setExample(example);
        pattern.setValue(regex);
        return pattern;
    }
}
