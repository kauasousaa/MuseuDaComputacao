package museu.memory;

import java.util.List;

public interface MemoryRepository {

    List<MemoryEntry> findAll();

    void save(MemoryEntry entry);

    boolean deleteById(String id);
}
