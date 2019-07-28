package songbox.house.task;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import songbox.house.client.VkClient;
import songbox.house.domain.entity.VkAudio;
import songbox.house.util.JsonUtils;

import javax.script.Invocable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static songbox.house.util.Constants.JSON_DELIMITER;
import static songbox.house.util.RetryUtil.getOptionalWithRetries;

@Slf4j
public class LoadVkAudioTask implements Callable<List<VkAudio>> {

    private final String ids;
    private final Long vkUserId;
    private final Invocable decryptionScript;
    private final VkClient vkClient;
    private final Integer retries;

    public LoadVkAudioTask(String ids, Long vkUserId, Invocable decryptionScript, VkClient vkClient, Integer retries) {
        this.ids = ids;
        this.vkUserId = vkUserId;
        this.decryptionScript = decryptionScript;
        this.vkClient = vkClient;
        this.retries = retries;
    }

    @Override
    public List<VkAudio> call() throws InterruptedException {
        return getOptionalWithRetries(this::reloadIdsResponse, ids, retries, "load_vk_audio")
                .map(this::processResponse)
                .orElse(emptyList());
    }

    private List<VkAudio> processResponse(String responseBody) {
        final List<List> vkTracksMetadata = JsonUtils.responseToObject(responseBody, List.class);

        return vkTracksMetadata.stream()
                .map(this::createVkAudio)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<VkAudio> createVkAudio(final List metadata) {
        final Long id = ((Number) metadata.get(0)).longValue();
        final String url = (String) metadata.get(2);
        final String title = (String) metadata.get(3);
        final String authors = (String) metadata.get(4);
        final Integer duration = (Integer) metadata.get(5);

        VkAudio track = null;
        if (url != null && !url.isEmpty()) {
            track = new VkAudio(id, vkUserId, authors, title, duration);
            final String urlDecrypted = decrypt(url);
            track.setUrl(urlDecrypted);
        } else {
            log.info("Url for track {} is empty, skipping processing", authors + " - " + title);
        }
        return ofNullable(track);
    }

    @SneakyThrows
    private String decrypt(final String url) {
        return (String) decryptionScript.invokeFunction("decode", url);
    }

    private Optional<String> reloadIdsResponse(String ids) {
        final Response response = vkClient.reload(ids);

        if (!response.body().contains(JSON_DELIMITER)) {
            return empty();
        }

        return ofNullable(response.body());
    }

}
