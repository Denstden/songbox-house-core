package songbox.house.converter;

import java.util.ArrayList;
import java.util.List;

public interface Converter<E, D> {
    E toEntity(D dto);

    D toDto(E entity);

    default Iterable<D> toDtos(final Iterable<E> entities) {
        final List<D> dtos = new ArrayList<>();

        if (entities != null) {
            entities.forEach(entity -> dtos.add(toDto(entity)));
        }

        return dtos;
    }
}
