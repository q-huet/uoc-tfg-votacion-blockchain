package es.tfg.votacion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración principal que habilita las propiedades de configuración
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Configuration
@EnableConfigurationProperties({
    FabricProperties.class,
    StorageProperties.class,
    AuthProperties.class,
    ElectionProperties.class
})
public class AppProperties {

    // Esta clase coordina la configuración de todos los módulos
    // Las propiedades específicas están en clases separadas
}