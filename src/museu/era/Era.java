package museu.era;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import museu.json.JsonSerializable;
import museu.shared.exception.ValidationException;

public enum Era implements JsonSerializable {
    SEVENTIES_OR_BEFORE("anos70", "Anos 70 ou antes"),
    EIGHTIES("anos80", "Anos 80"),
    NINETIES("anos90", "Anos 90"),
    TWO_THOUSANDS("anos2000", "Anos 2000"),
    TWENTY_TENS("anos2010", "Anos 2010"),
    TWENTY_TWENTIES("anos2020", "Anos 2020");

    private final String code;
    private final String label;

    Era(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static Era parse(String code) {
        return Arrays.stream(values())
                .filter(era -> era.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Época inválida: " + code));
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("code", code);
        json.put("label", label);
        return json;
    }
}
