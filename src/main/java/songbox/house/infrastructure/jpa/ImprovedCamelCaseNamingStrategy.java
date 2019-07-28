package songbox.house.infrastructure.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImprovedCamelCaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private CamelCaseToUpperSnakeCaseConverter camelCaseToUpperSnakeCaseConverter = new CamelCaseToUpperSnakeCaseConverter();

    public ImprovedCamelCaseNamingStrategy() {
    }

    @Autowired
    public ImprovedCamelCaseNamingStrategy(CamelCaseToUpperSnakeCaseConverter camelCaseToUpperSnakeCaseConverter) {
        this.camelCaseToUpperSnakeCaseConverter = camelCaseToUpperSnakeCaseConverter;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        String nameStr = name.getText();
        String result = camelCaseToUpperSnakeCaseConverter.convert(nameStr);
        return new Identifier(result, true);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String nameStr = name.getText();
        String result = camelCaseToUpperSnakeCaseConverter.convert(nameStr);
        return new Identifier(result, true);
    }
}
