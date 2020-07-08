package songbox.house.service.impl

import songbox.house.domain.entity.user.UserProperty
import songbox.house.repository.UserInfoRepository
import songbox.house.repository.UserPropertyRepository
import songbox.house.service.MusicCollectionService
import songbox.house.service.UserService
import spock.lang.Specification
import spock.lang.Unroll

import static org.assertj.core.util.Lists.newArrayList
import static songbox.house.domain.entity.user.UserPropertyMask.AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS
import static songbox.house.domain.entity.user.UserPropertyMask.SEARCH_REPROCESS_ENABLED

class UserPropertyServiceImplSpec extends Specification {

    def userInfoRepository = Mock(UserInfoRepository)
    def userPropertyRepository = Mock(UserPropertyRepository)
    def userService = Mock(UserService)
    def musicCollectionService = Mock(MusicCollectionService)

    def service = new UserPropertyServiceImpl(userInfoRepository, userPropertyRepository, userService,
            musicCollectionService)

    @Unroll
    def "should remove mask"() {
        given:
        def userProperty = new UserProperty()
        userProperty.mask = dbMask

        when:
        service.removeMasks(userProperty, newArrayList(mask))

        then:
        1 * userPropertyRepository.save({
            resultMask == it.mask && searchReprocess == it.isSearchReprocessEnabled() &&
                    autoUseFullSearch == it.isUseFullSearchIfFastFailsEnabled()
        } as UserProperty)

        where:
        dbMask | mask                                     || resultMask || searchReprocess || autoUseFullSearch
        0      | SEARCH_REPROCESS_ENABLED                 || 0          || false           || true
        0      | AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS || 8          || false           || false
        8      | SEARCH_REPROCESS_ENABLED                 || 8          || false           || false
        8      | AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS || 8          || false           || false
    }

    @Unroll
    def "should set mask"() {
        given:
        def userProperty = new UserProperty()
        userProperty.mask = dbMask

        when:
        service.setMasks(userProperty, newArrayList(mask))

        then:
        1 * userPropertyRepository.save({
            resultMask == it.mask && searchReprocess == it.isSearchReprocessEnabled() &&
                    autoUseFullSearch == it.isUseFullSearchIfFastFailsEnabled()
        } as UserProperty)

        where:
        dbMask | mask                                     || resultMask || searchReprocess || autoUseFullSearch
        0      | SEARCH_REPROCESS_ENABLED                 || 1          || true            || true
        0      | AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS || 0          || false           || true
        8      | SEARCH_REPROCESS_ENABLED                 || 9          || true            || false
        8      | AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS || 0          || false           || true
    }
}
