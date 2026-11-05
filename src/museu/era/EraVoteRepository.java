package museu.era;

import java.util.Map;

public interface EraVoteRepository {

    Map<Era, Integer> countAll();

    void increment(Era era);
}
