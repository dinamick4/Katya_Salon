package org.cosmetology.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Предназначен для настройки встроенного веб-сервера Tomcat.
 *
 */
@Configuration
public class CustomWebServerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /**
     * Метод позволяет изменять свойства фабрики веб-сервера.
     * Устанавливает максимальный допустимый размер POST-запросов, отправляемых клиенту. Значение задаётся в байтах.
     *
     * @param factory  представляет собой механизм построения экземпляра сервлет-контейнера Tomcat
     */
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(104857600); // 100MB
        });
    }
}