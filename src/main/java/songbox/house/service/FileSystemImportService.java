package songbox.house.service;

public interface FileSystemImportService {
    Integer importCollectionFromFileSystem(final String collectionName, final String root, final Short bitRate);
}
