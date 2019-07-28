package songbox.house.infrastructure.jpa;

import org.springframework.stereotype.Component;

@Component
public class CamelCaseToUpperSnakeCaseConverter {

    public String convert(String name) {
        StringBuilder result = new StringBuilder();

        int i = 0;
        char currentCharacter;
        char lastAdded = 'A'; //init as upper to not add _ in the start

        while (i < name.length()) {
            currentCharacter = name.charAt(i);
            if (Character.isUpperCase(currentCharacter)) {
                if (i > 1) {
                    lastAdded = name.charAt(i - 1);
                }
                if (Character.isUpperCase(lastAdded)) {
                    result.append(currentCharacter);
                } else {
                    result.append("_").append(Character.toUpperCase(currentCharacter));
                }
            } else {
                result.append(Character.toUpperCase(currentCharacter));
            }
            i++;
        }

        return result.toString();
    }
}
