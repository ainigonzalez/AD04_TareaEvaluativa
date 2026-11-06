package eus.birt.dam.ad04.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class JpaUtil {
    private JpaUtil() { }

    public static EntityManagerFactory crearFactory() {
        String clave = System.getenv("DB_PASSWORD");
        if (clave == null || clave.isBlank()) {
            throw new IllegalStateException("Define DB_PASSWORD antes de ejecutar.");
        }
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.password", clave);
        copiarVariable(propiedades, "DB_URL", "jakarta.persistence.jdbc.url");
        copiarVariable(propiedades, "DB_USER", "jakarta.persistence.jdbc.user");
        return Persistence.createEntityManagerFactory("activa-pu", propiedades);
    }

    private static void copiarVariable(Map<String, Object> destino,
                                       String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            destino.put(propiedad, valor);
        }
    }
}
