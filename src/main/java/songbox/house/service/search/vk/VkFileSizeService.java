package songbox.house.service.search.vk;

public interface VkFileSizeService {
    long getSizeBytes(String url);

    String getSizeHeader(String url);
}
