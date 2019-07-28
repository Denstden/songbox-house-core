package songbox.house.service;

public interface FileSystemExportService {
    Integer exportCollection(final String collectionName, final boolean createCopyInEveryGenre, boolean only320);
}
