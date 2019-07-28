package songbox.house.service.search.vk;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import songbox.house.client.VkClient;
import songbox.house.domain.dto.request.vk.VkSearchAudioRequestDto;
import songbox.house.domain.entity.VkAudio;
import songbox.house.task.LoadVkAudioTask;
import songbox.house.util.Configuration;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.StringUtils.join;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;
import static songbox.house.util.ExecutorUtil.createExecutorService;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VkAudioLoader {

    VkClient vkClient;
    Configuration configuration;
    Integer maxResultsPerOneSearch;
    Integer loadRetries;
    ExecutorService executorService;

    ScriptEngine scriptEngine;
    Invocable script;

    @Autowired
    public VkAudioLoader(final VkClient vkClient, final Configuration configuration,
            @Value("${songbox.house.vk.load.max_result_per_one}") final Integer maxResultsPerOneSearch,
            @Value("${songbox.house.vk.load.threads}") final Integer threads,
            @Value("${songbox.house.vk.load.retries}") final Integer retries) {
        this.vkClient = vkClient;
        this.configuration = configuration;
        this.maxResultsPerOneSearch = maxResultsPerOneSearch;
        this.loadRetries = retries;
        this.scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        this.script = loadDecryptionScript();
        this.executorService = createExecutorService(threads);
    }

    public List<VkAudio> load(final VkSearchAudioRequestDto vkSearchAudioRequestDto, final boolean loadAll) {
        final long loadingStarted = currentTimeMillis();
        final List<VkAudio> result = new ArrayList<>();

        final List<String> contentIdsForLoading = prepareContentIdsForLoad(vkSearchAudioRequestDto.getContentIds(), loadAll);

        if (!contentIdsForLoading.isEmpty()) {
            final CompletionService<List<VkAudio>> completionService = new ExecutorCompletionService<>(executorService);

            for (String contentIds : contentIdsForLoading) {
                completionService.submit(new LoadVkAudioTask(contentIds, configuration.getVk().getUserId(), script, vkClient, loadRetries));
            }
            for (int tasksHandled = 0; tasksHandled < contentIdsForLoading.size(); tasksHandled++) {
                try {
                    Future<List<VkAudio>> future = completionService.take();
                    result.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Can't get the future result", e);
                }
            }
        }

        log.info(PERFORMANCE_MARKER, "Loaded {}ms", currentTimeMillis() - loadingStarted);

        return result;
    }

    private Invocable loadDecryptionScript() {
        final String scriptWithPlaceholders = readScript();
        final String script = scriptWithPlaceholders.replace("${vkId}", configuration.getVk().getUserId().toString()); // TODO: replace with bindings

        try {
            scriptEngine.eval(script);
        } catch (ScriptException e) {
            log.error("Exception: ", e);
        }

        return (Invocable) scriptEngine;
    }

    private String readScript() {
        final StringBuilder sb = new StringBuilder();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("decrypt.js")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Exception: ", e);
        }
        return sb.toString();
    }

    private List<String> prepareContentIdsForLoad(final List<String> contentIds, final boolean loadAll) {
        final List<String> result = newArrayList();

        final int size = loadAll ? contentIds.size() : min(contentIds.size(), maxResultsPerOneSearch);
        if (size > 0) {
            int fromIndex = 0;
            int toIndex = min(fromIndex + 10, size);

            while (fromIndex != toIndex) {
                result.add(join(contentIds.subList(fromIndex, toIndex), ","));
                fromIndex = toIndex;
                toIndex = min(fromIndex + 10, size);
            }
        }

        return result;
    }
}
