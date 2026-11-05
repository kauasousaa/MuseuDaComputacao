package museu.content;

import java.util.List;

public interface ContentRepository<T> {

    List<T> findAll();
}
