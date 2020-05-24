package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.entity.Like;
import songbox.house.repository.LikeRepository;
import songbox.house.service.LikeService;

import static lombok.AccessLevel.PRIVATE;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@AllArgsConstructor
public class LikeServiceImpl implements LikeService {

    LikeRepository likeRepository;

    @Override
    @Transactional
    public Like add(Like like) {
        return likeRepository.save(like);
    }
}
