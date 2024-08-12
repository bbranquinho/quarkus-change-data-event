package io.bbranquinho.quarkus.change.data.event.deployment;

import io.bbranquinho.change.data.event.dynamo.DynamoEvent;
import io.bbranquinho.change.data.event.dynamo.DynamoEventData;
import io.bbranquinho.change.data.event.repository.EventStoreMapper;
import io.bbranquinho.quarkus.change.data.event.runtime.ChangeDataEventProducer;
import io.bbranquinho.quarkus.change.data.event.runtime.ChangeDataEventRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AutoAddScopeBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChangeDataEventProcessor {

    private static final String FEATURE = "change-data-event";

    public static final DotName EVENT_STORE_MAPPER_CLASS = DotName.createSimple(EventStoreMapper.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
                       BuildProducer<AutoAddScopeBuildItem> autoScopes,
                       BuildProducer<UnremovableBeanBuildItem> unRemovableBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                                                       .addBeanClasses(ChangeDataEventProducer.class)
                                                       .setUnremovable()
                                                       .build());

        autoScopes.produce(AutoAddScopeBuildItem.builder()
                                                .defaultScope(BuiltinScope.SINGLETON)
                                                .match((clazz, annotations, index) -> getAllInterfaces(clazz, index).contains(EVENT_STORE_MAPPER_CLASS))
                                                .build());

        unRemovableBeans.produce(UnremovableBeanBuildItem.beanTypes(EVENT_STORE_MAPPER_CLASS));
    }

    @BuildStep(onlyIf = NativeBuild.class)
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(DynamoEvent.class).methods().fields().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(DynamoEventData.class).methods().fields().build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void setupChangeDataEvent(ChangeDataEventRecorder recorder, BeanContainerBuildItem beanContainerBuildItem) {
        recorder.initChangeDataEvent();
    }

    private static Set<DotName> getAllInterfaces(final ClassInfo cls, final IndexView indexView) {
        if (cls == null) {
            return Collections.emptySet();
        }

        final HashSet<DotName> interfacesFound = new HashSet<>();
        getAllInterfaces(cls, indexView, interfacesFound);

        return interfacesFound;
    }

    private static void getAllInterfaces(ClassInfo cls, final IndexView indexView, final HashSet<DotName> interfacesFound) {
        while (cls != null) {
            final List<DotName> interfaces = cls.interfaceNames();

            for (final DotName i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(indexView.getClassByName(i), indexView, interfacesFound);
                }
            }

            DotName superName = cls.superName();
            cls = superName != null && !superName.equals(DotNames.OBJECT) ? indexView.getClassByName(superName) : null;
        }
    }

}
