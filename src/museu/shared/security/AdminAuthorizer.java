package museu.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import museu.shared.exception.ForbiddenException;

public final class AdminAuthorizer {

    private final Optional<String> adminKey;

    public AdminAuthorizer(Optional<String> adminKey) {
        this.adminKey = adminKey.filter(key -> !key.isBlank());
    }

    public void authorize(String providedKey) {
        String expectedKey = adminKey.orElseThrow(
                () -> new ForbiddenException("Remoção desabilitada: defina a variável de ambiente MUSEU_ADMIN_KEY."));
        boolean matches = MessageDigest.isEqual(
                expectedKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            throw new ForbiddenException("Chave de administração inválida.");
        }
    }
}
