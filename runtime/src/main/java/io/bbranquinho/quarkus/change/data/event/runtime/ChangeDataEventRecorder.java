package io.bbranquinho.quarkus.change.data.event.runtime;

import io.bbranquinho.change.data.event.repository.EventStoreMapper;
import io.bbranquinho.change.data.event.repository.EventStoreMapperRepository;
import io.bbranquinho.change.data.event.repository.EventStoreRepository;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.inject.spi.CDI;
import org.jboss.logging.Logger;

@Recorder
public class ChangeDataEventRecorder {

    private static final Logger LOGGER = Logger.getLogger(ChangeDataEventRecorder.class);

    public void initChangeDataEvent() {
        LOGGER.info("Initialize Change Data Event.");

        CDI<Object> cdi = CDI.current();

        EventStoreRepository eventStoreRepository = cdi.select(EventStoreRepository.class).get();

        cdi.getBeanManager()
           .getBeans(EventStoreMapperRepository.class)
           .forEach(bean -> eventStoreRepository.registerMapper((EventStoreMapper) cdi.select(bean.getBeanClass()).get()));

        LOGGER.info("Completed Change Data Event initialization.");
    }

}
